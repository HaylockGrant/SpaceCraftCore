package mc.Spacecraft.Core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.fasterxml.jackson.databind.JsonNode;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener {
	private FileConfiguration config = getConfig();
	private PlayerHome playerHome;
	private RankFile rankFile;
	private MetaEditor metaEditor;
	private DataBaseManager dataBase;
	private WaypointManager waypoints;
	private String pluginVersion = "V" + this.getDescription().getVersion();
	private TelegramViaNodeRed telegram;
	JavaPlugin plugin = this;
	@Override
	public void onEnable() {
		config.addDefault("DBlocation", "localhost");
		config.addDefault("DBport", 3306);
		config.addDefault("DBdatabase", "default");
		config.addDefault("DBusername", "root");
		config.addDefault("DBpassword", "");
		config.addDefault("API","http://localhost:6900/api/");
		config.addDefault("VPN_Api_Key", "F9J3K1V02MFO1C93KA7B");
		config.options().copyDefaults(true);
        saveConfig();
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		telegram = new TelegramViaNodeRed(config.getString("API"));
		dataBase = new DataBaseManager(config.getString("DBlocation"),config.getString("DBport"),config.getString("DBdatabase"),config.getString("DBusername"),config.getString("DBpassword"),telegram);
		dataBase.connect();
		this.metaEditor = new MetaEditor(this);
		playerHome = new PlayerHome(plugin,dataBase);
		waypoints = new WaypointManager(plugin,dataBase,metaEditor);
		this.rankFile = new RankFile(this);
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("tpa").setTabCompleter(new TabFinisher(waypoints));
		getCommand("tpaccept").setTabCompleter(new TabFinisher(waypoints));
		getCommand("home").setTabCompleter(new TabFinisher(waypoints));
		getCommand("sethome").setTabCompleter(new TabFinisher(waypoints));
		getCommand("wp").setTabCompleter(new TabFinisher(waypoints));
		getCommand("swp").setTabCompleter(new TabFinisher(waypoints));
		getCommand("dwp").setTabCompleter(new TabFinisher(waypoints));
		getCommand("setwaypoint").setTabCompleter(new TabFinisher(waypoints));
		getCommand("waypoint").setTabCompleter(new TabFinisher(waypoints));
		getCommand("deletewaypoint").setTabCompleter(new TabFinisher(waypoints));
		System.out.println("Space Core Activated!");
		for (Object p : Bukkit.getOnlinePlayers().toArray()) {
			waypoints.playerJoin(((Player) p));
			((Player) p).sendMessage(ChatColor.DARK_PURPLE + "Space Core Activated!");
			((Player) p).sendTitle(ChatColor.DARK_PURPLE + "SpaceCore", ChatColor.GOLD + pluginVersion, 10, 50, 10);
		}
	}

	@Override
	public void onDisable() {
		//playerHome.save();
		rankFile.save();
		dataBase.close();
		System.out.println("Space Core Deactived.");
	}

	@EventHandler
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event)
	{
		String uuid = event.getUniqueId().toString();
		String username = event.getName();
        String ipAddress = event.getAddress().getHostAddress().toString();
        String telegramMessage;
        final boolean allowed;
        switch (event.getLoginResult())
        {
        	case ALLOWED:
				org.bukkit.Server s = getServer();
				boolean isWhiteListed = false;
				boolean isBanned = false;
				boolean serverFull = false;
				if(s.hasWhitelist())
				{
					for (OfflinePlayer o : s.getWhitelistedPlayers())
					{
						if(o.getUniqueId().toString().equalsIgnoreCase(uuid))
						{
							isWhiteListed = true;
							break;
						}
					}
				}
				else
				{
					isWhiteListed = true;
				}
				for(OfflinePlayer o:s.getBannedPlayers())
				{
					if(o.getUniqueId().toString().equalsIgnoreCase(uuid))
					{
						isBanned = true;
						break;
					}
				}
				for(String i:s.getIPBans())
				{
					if(i.equals(ipAddress))
					{
						isBanned = true;
						break;
					}
				}
				if(s.getOnlinePlayers().size() >= s.getMaxPlayers())
				{
					serverFull = true;
				}
				if(isWhiteListed && !isBanned && !serverFull)
				{
					String apiResults;
					Boolean wasInDataBase = false;
					try
					{
						apiResults = dataBase.getVPNAPI(ipAddress);
						if(!apiResults.isEmpty() && !apiResults.isBlank())
						{
							wasInDataBase = true;
						}
					}
					catch (Exception e)
					{
						if(e.getMessage().equalsIgnoreCase("row count 0"))
						{
							System.out.println("No current VPNapi logs");
						}
						else
						{
							e.printStackTrace();
						}
						apiResults = URLConnectionReader.getDatas("https://vpnapi.io/api/" + ipAddress + "?key=" + config.getString("VPN_Api_Key"));
					}
					if(apiResults.isEmpty())
					{
						allowed = true;
						telegramMessage = (username + " was allowed into the server with the IP " + ipAddress + ", however the VPN api failed");
						break;
					}
					else
					{
						JsonNode node = JSON.parse(apiResults);
						if(node != null && node.hasNonNull("security") && node.hasNonNull("network") && node.hasNonNull("location"))
						{
							if(!wasInDataBase) {
								dataBase.logVPNAPI(ipAddress,apiResults);
							}
							boolean vpn;
							String continentalCode;
							String lowercaseOrginization;
							if(node.get("security").get("vpn").asBoolean() || node.get("security").get("proxy").asBoolean() || node.get("security").get("tor").asBoolean() || node.get("security").get("relay").asBoolean())
							{
								vpn = true;
							}
							else
							{
								vpn = false;
							}
							continentalCode = node.get("location").get("continent_code").asText();
							lowercaseOrginization = node.get("network").get("autonomous_system_organization").asText().toLowerCase();
							if(vpn)
							{
								event.disallow(Result.KICK_BANNED, ChatColor.RED + "VPN detected, please reconect without it");
								allowed = false;
								telegramMessage = (username + " was rejected from the server for using a vpn with the IP " + ipAddress);
								break;
							}
							else
							{
								if(!continentalCode.equalsIgnoreCase("NA"))
								{
									event.disallow(Result.KICK_BANNED, ChatColor.RED + "You've connected from a banned country, if you believe this is an error please contact the server admin");
									allowed = false;
									telegramMessage = (username + " was rejected from the server for connecting from a banned country with the IP " + ipAddress);
									break;
								}
								else
								{
									String[] bannedOrgs = {"amazon","google","microsoft","azure","cloud","aws","gcp","ibm","linux","computing","node","hosting","service","packet","hub","code","data","base","choopa","leaseweb"};
									Boolean foundOrg = false;
									for(String i:bannedOrgs)
									{
										if(lowercaseOrginization.contains(i))
										{
											foundOrg = true;
											break;
										}
									}
									if(foundOrg)
									{
										event.disallow(Result.KICK_BANNED, ChatColor.RED + "You've connected from a banned ISP, if you believe this is an error please contact the server admin");
										allowed = false;
										telegramMessage = (username + " was rejected from the server for connecting from a banned orginzation with the IP " + ipAddress + " and orginization " + node.get("network").get("autonomous_system_organization").asText());
										break;
									}
									else
									{
										allowed = true;
										telegramMessage = (username + " was allowed into the server with the IP " + ipAddress);
										break;
									}
								}
							}
						}
						else
						{
							allowed = true;
							telegramMessage = (username + " was allowed into the server with the IP " + ipAddress + ", however the VPN api parse failed");
							break;
						}
					}
				}
				else
				{
					allowed = false;
					if(isBanned)
					{
						event.disallow(Result.KICK_BANNED,ChatColor.RED + "You've been banned, go away");
						telegramMessage = (username + " was rejected from the server for connecting with a banned account or IP with the IP " + ipAddress);
						break;
					}
					else if(!isWhiteListed)
					{
						event.disallow(Result.KICK_WHITELIST,ChatColor.RED + "You are not on the whitelist. Please contact the server admin if you believe this is an error");
						telegramMessage = (username + " was rejected from the server for not being on the whitelist with the IP " + ipAddress);
						break;
					}
					else if(serverFull)
					{
						event.disallow(Result.KICK_FULL,ChatColor.RED + "The server is full, please contact server admin if you believe this is an error");
						telegramMessage = (username + " was rejected from the server because the server was full with the IP " + ipAddress);
						break;
					}
					else
					{
						telegramMessage = (username + " was rejected from the server for some reason, Error 879 with IP " + ipAddress);
						System.out.println("Error 879: In Main class. Somehow isBanned !isWhiteListed or serverFull variables were changed or were intitilized or checked wrong");
						break;
					}
				}
        	case KICK_WHITELIST:
        		allowed = false;
        		event.setKickMessage(ChatColor.RED + "You are not on the whitelist. Please contact the server admin if you believe this is an error");
        		telegramMessage = (username + " was rejected from the server for not being on the whitelist with the IP " + ipAddress);
        		break;
        	case KICK_BANNED:
        		if(this.getServer().getIPBans().contains(ipAddress))
                {
                	event.setKickMessage(ChatColor.RED + "Banned IP detected, reconect without a vpn");
                	telegramMessage = (username + " was rejected from the server for connecting with a banned IP with the IP " + ipAddress);
                }
        		else
        		{
        			event.setKickMessage(ChatColor.RED + "You've been banned, go away");
        			telegramMessage = (username + " was rejected from the server for connecting with a banned account with the IP " + ipAddress);
        		}
        		allowed = false;
        		break;
        	case KICK_FULL:
        		event.setKickMessage(ChatColor.RED + "The server is full, please contact server admin if you believe this is an error");
        		telegramMessage = (username + " was rejected from the server because the server was full with the IP " + ipAddress);
        		allowed = false;
        		break;
        	default:
        		event.disallow(Result.KICK_OTHER, "Unknown error");
        		telegramMessage = (username + " was rejected from the server because the server for an unknown reason with the IP " + ipAddress);
        		allowed = false;
        		break;
        }
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable()
        {
            @Override
            public void run() 
            {
            	telegram.sendApiMessage(telegramMessage);
                dataBase.logConnection(ipAddress, event.getUniqueId().toString(), allowed);
            }
        });
    }
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) 
	{
		Player p = e.getPlayer();
		playerHome.newPlayer(p);
		try	{
			rankFile.newPlayer(p);
		}
		catch(Exception x){
			System.out.println("RankFileException");
		}
		
		TextComponent rankandname = rankFile.getTitleAndName(p);
		BaseComponent[] restofthemessage = new ComponentBuilder(" has joined the server").color(ChatColor.DARK_GRAY).bold(false)
				.create();
		rankandname.addExtra(restofthemessage[0]);
		e.setJoinMessage("");
		Bukkit.spigot().broadcast(rankandname);
		waypoints.playerJoin(p);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		e.setQuitMessage("");
		TextComponent rankandname = rankFile.getTitleAndName(e.getPlayer());
		BaseComponent[] restofthemessage = new ComponentBuilder(" has left the server").color(ChatColor.DARK_GRAY).bold(false)
				.create();
		rankandname.addExtra(restofthemessage[0]);
		Bukkit.spigot().broadcast(rankandname);
		waypoints.playerDisconects(e.getPlayer());
	}
	
	@EventHandler 
	public void onPlayerDeath(PlayerDeathEvent e)
	{
		if(e.getEntity().getUniqueId().toString().equalsIgnoreCase("14efc345-b555-4611-9555-9881ca369f5b"))
		{
			e.setKeepInventory(false);
			e.setKeepLevel(false);
		}
		else
		{
			e.setKeepInventory(true);
			e.setKeepLevel(true);
		}
		Bukkit.getConsoleSender().sendMessage(e.getDeathMessage());
	}

	@EventHandler
	public void onWorldSave(WorldSaveEvent e) 
	{
		rankFile.save();
	}
	
	@EventHandler
	public void onCreaperExploition(ExplosionPrimeEvent e)
	{
		if(e.getEntityType().equals(EntityType.CREEPER))
		{
			e.setRadius(0);
			e.setFire(false);
		}
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) 
	{
		
	}
	
	@EventHandler
	public void onChatEvent(AsyncPlayerChatEvent e) {
		e.setCancelled(true);
		TextComponent thename = rankFile.getTitleAndName(e.getPlayer());
		TextComponent theSpacer = new TextComponent(
				new ComponentBuilder(" > ").bold(true).color(ChatColor.DARK_GRAY).create()[0]);
		theSpacer.setBold(true);
		TextComponent thetext;
		if (rankFile.getRankValue(e.getPlayer()) == 0) {
			thetext = new TextComponent(ChatColor.GRAY + e.getMessage());
			thename.addExtra(theSpacer);
			thename.addExtra(thetext);
		} else {
			thetext = new TextComponent(ChatColor.WHITE + e.getMessage());
			thename.addExtra(theSpacer);
			thename.addExtra(thetext);
		}
		//TODO implement global / local chat features and make a hashmap to manage it
		for (Object p : Bukkit.getOnlinePlayers().toArray()) 
		{
			if(!(((Player) p).getName() == "RecycledBunny"))
			{
				((Player) p).spigot().sendMessage(thename);
			}
			else
			{
				if(((Player) p).getLocation().distance(e.getPlayer().getLocation()) <= 64)
				{
					((Player) p).spigot().sendMessage(thename);
				}
			}
		}
		Bukkit.getConsoleSender().sendMessage(thename.toPlainText());
	}
	
	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
		Entity a = e.getRightClicked();
		TextComponent pets = new TextComponent(new ComponentBuilder("*pet pet*").color(ChatColor.GRAY).create()[0]);
		try {
				if(a.getType() != null ){
					if(a.getType().toString().equalsIgnoreCase("WOLF")) {
						if(metaEditor.timeSinceCheck(e.getPlayer()) >= 3000) {
							e.getPlayer().spigot().sendMessage(pets);
							e.getPlayer().spawnParticle(Particle.HEART, a.getLocation().add(0, 1, 0),3);
						}
					}
				}
			}
		catch(Exception x){
			System.out.println("Player Interact with animal execption error");
		}
	}
	
	@EventHandler
	public void onPlayerTab(PlayerCommandSendEvent e) 
	{
		if(!e.getPlayer().isOp())
		{
			e.getCommands().removeAll(e.getCommands());
			e.getCommands().add("sethome");
			e.getCommands().add("home");
			e.getCommands().add("tpa");
			e.getCommands().add("tpaccept");
			e.getCommands().add("wp");
			e.getCommands().add("dwp");
			e.getCommands().add("swp");
			e.getCommands().add("waypoint");
			e.getCommands().add("setwaypoint");
			e.getCommands().add("deletewaypoint");
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String command = label.toLowerCase();
		if (sender instanceof Player) {
			// if player command is available
			Player player = (Player) sender;
			switch (command) {
			case "sethome":
				player = (Player) sender;
				playerHome.setHome(player, player.getLocation());
				return true;
			case "home":
				playerHome.sendHome(player);
				return true;
			case "rankup":
				if (player.isOp()) {
					if (args.length > 0 && Bukkit.getPlayer(args[0]) != null) {
						Player reciever = Bukkit.getPlayer(args[0]);
						if (!(rankFile.getRankValue(reciever) >= 3)) {
							rankFile.setrank(reciever, rankFile.getRankValue(Bukkit.getPlayer(args[0])) + 1);
						}
					} else {
						player.sendMessage(ChatColor.RED + "That player doesn't exist!");
					}
				}
				return true;
			case "rankdown":
				if (player.isOp()) {
					if (args.length > 0 && Bukkit.getPlayer(args[0]) != null) {
						Player reciever = Bukkit.getPlayer(args[0]);
						if (!(rankFile.getRankValue(reciever) <= 0)) {
							rankFile.setrank(reciever, rankFile.getRankValue(Bukkit.getPlayer(args[0])) - 1);
						}
					} else {
						player.sendMessage(ChatColor.RED + "That player doesn't exist!");
					}
				}
				return true;
			case "tpa":

				Player theSender = ((Player) sender);
				if (args.length == 1 && Bukkit.getPlayer(args[0]) != null) 
				{
					Player theReceiver = Bukkit.getPlayer(args[0]);
					long elapstedtime = (System.currentTimeMillis() - metaEditor.getSendingTime(theSender));
					if(elapstedtime > 60000)
					{
						if (metaEditor.areSame(theSender, theReceiver)) 
						{
							sender.sendMessage(ChatColor.RED + "You may have already sent a request to this player");
						}
						theSender.sendMessage(ChatColor.DARK_GRAY + "Request sent to " + ChatColor.GREEN + theReceiver.getName());
						metaEditor.setSending(theSender, theReceiver.getName());
						elapstedtime = (System.currentTimeMillis() - metaEditor.getRecievingTime(theReceiver));
						metaEditor.SetReceiving(theReceiver, theSender.getName());
						metaEditor.setSendingTime(theSender);
						metaEditor.setRecievingTime(theReceiver);
						theReceiver.sendMessage(
								"§8" + "You are recieving a tpa request from" + "§a" + ((Player) sender).getName());
						// click here to accept
						TextComponent tpaComponent1 = new TextComponent("Click ");
						TextComponent tpaComponent2 = new TextComponent("here");
						TextComponent tpaComponent3 = new TextComponent(" to accept or type /tpaccept");
						tpaComponent1.setColor(ChatColor.DARK_GRAY);
						tpaComponent2.setColor(ChatColor.GREEN);
						tpaComponent3.setColor(ChatColor.DARK_GRAY);
						tpaComponent2.setHoverEvent(
								new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Yup, Click here")
										.color(ChatColor.DARK_GREEN).bold(true).create()));
						tpaComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
						tpaComponent1.addExtra(tpaComponent2);
						tpaComponent1.addExtra(tpaComponent3);
						theReceiver.spigot().sendMessage(tpaComponent1);

					}
					else
					{
						sender.sendMessage(ChatColor.RED + "You must wait 1 minute between sending requests");
					}
				}
				else 
				{
					sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
				}
				return true;
			case "tpaccept":
				Player accepter = ((Player) sender);
				if (metaEditor.getReceiving(accepter) != null) {
					Player tpaer = Bukkit.getPlayer(metaEditor.getReceiving(accepter));
					if (metaEditor.areSame(tpaer, accepter)) 
					{
						long timeelapsted = (System.currentTimeMillis() - metaEditor.getRecievingTime(accepter));
						if(timeelapsted < 150000)
						{
							Location loc = accepter.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
							tpaer.teleport(loc);
							tpaer.sendMessage(ChatColor.GREEN + "Teleporting!");
							accepter.sendMessage(ChatColor.GREEN + "Teleporting!");
							metaEditor.clearBoth(tpaer, accepter);
							metaEditor.clearBothTime(tpaer, accepter);
						}
						else
						{
							accepter.sendMessage(ChatColor.RED + "TPA request has expired");
							metaEditor.clearBoth(tpaer, accepter);
							metaEditor.clearBothTime(tpaer, accepter);
						}
					} else {
						accepter.sendMessage(
								ChatColor.GREEN + tpaer.getName() + ChatColor.RED + " is no longer tping to you");
						metaEditor.clearReceiving(accepter);
						metaEditor.clearRecievingTime(accepter);
					}
				} else {
					accepter.sendMessage(ChatColor.RED + "You are not revieving any requests");
				}
				return true;
			case "connect":
				if(sender.isOp())
				{
					if(!dataBase.connect())
					{
						sender.sendMessage("Failed");
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "You must be opped to run this command");
				}
				return true;
			case "wp": case "waypoint":
				if (args.length == 1 && args[0] != null)
				{
					waypoints.teleportToWaypoint(player, args[0]);	
				}
				else
				{
					player.sendMessage(ChatColor.of("#fc035e") + "You must select a waypoint to teleport to");
				}
				return true;
			case "dwp": case "deletewaypoint":
				
				if (args.length == 1 && args[0] != null)
				{
					waypoints.deleteWaypoint(player, args[0]);
				}
				else
				{
					player.sendMessage(ChatColor.of("#fc035e") + "You must select a waypoint to delete");
				}
				return true;
			case "swp": case "setwaypoint":
				if (args.length == 1 && args[0] != null)
				{
					waypoints.newWaypoint(player, args[0], player.getLocation());
				}
				else
				{
//					System.out.println("Printing args");
//					System.out.println(args.length);
//					System.out.println(args[0]);
					player.sendMessage(ChatColor.of("#fc035e") + "You must select a waypoint to create");
				}
				return true;
			default:
				return false;
			}
		} 
		//end of player commands
		else 
		//Start of console commands
		{
			switch (command) {
			case "rankup":
				if (args.length > 0 && Bukkit.getPlayer(args[0]) != null) {
					Player reciever = Bukkit.getPlayer(args[0]);
					if (!(rankFile.getRankValue(reciever) >= rankFile.getTotalRanks())) {
						rankFile.setrank(reciever, rankFile.getRankValue(Bukkit.getPlayer(args[0])) + 1);
					}
				} else {
					sender.sendMessage("That player doesn't exist!");
				}
				return true;
			case "rankdown":
				if (args.length > 0 && Bukkit.getPlayer(args[0]) != null) {
					Player reciever = Bukkit.getPlayer(args[0]);
					if (!(rankFile.getRankValue(reciever) <= 0)) {
						rankFile.setrank(reciever, rankFile.getRankValue(Bukkit.getPlayer(args[0])) - 1);
					}
				} else {
					sender.sendMessage("That player doesn't exist!");
				}
				return true;
			case "setrank":
				if (args.length > 0 && Bukkit.getPlayer(args[0]) != null) {
					if (args.length > 1 && rankFile.getTotalRanks() >= Integer.parseInt(args[1])
							&& Integer.parseInt(args[1]) >= 0) {
						rankFile.setrank(Bukkit.getPlayer(args[0]), Integer.parseInt(args[1]));
					} else {
						sender.sendMessage("Invalid number");
					}
				} else {
					sender.sendMessage("That player doesn't exist");
				}
				return true;
			case "connect":
				if(!dataBase.connect())
				{
					sender.sendMessage("Failed");
				}
				return true;
			case "close":
				if(!dataBase.close())
				{
					sender.sendMessage("Failed");
				}
				return true;
			case "coreversion":
				sender.sendMessage("Running version " + pluginVersion);
				return true;
			default:
				return false;
			}
		} //end of console commands
	} //end of on command
	
	@EventHandler
	public void	onBlockBreak(BlockBreakEvent e)
	{
		//implement anti grief
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e)
	{

	}
	
	public void onPlayerCraft(PrepareItemCraftEvent e)
	{
		//If para makes tnt
	}
}