package mc.Spacecraft.Core;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class WaypointManager 
{
	JavaPlugin plugin;
	DataBaseManager db;
	MetaEditor me;
	HashMap<String, ArrayList<waypoint>> playWPMap = new HashMap<String, ArrayList<waypoint>>();
	String bannedSymbles = "~`!@#$%^&*()_-+={[}]\\|;:\"'<,>.?/	";
	public WaypointManager(JavaPlugin plugin, DataBaseManager dataBase, MetaEditor metaEditor)
	{
		this.plugin = plugin;
		db = dataBase;
		me = metaEditor;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run()
			{
				for(Player ps:Bukkit.getOnlinePlayers())
				{
					playWPMap.put(ps.getUniqueId().toString(), db.getWaypoints(ps));
				}
			}
		});
	}
	
	public void playerJoin(Player p)
	{
		playWPMap.put(p.getUniqueId().toString(), db.getWaypoints(p));
	}
	
	public void playerDisconects(Player p)
	{
		playWPMap.remove(p.getUniqueId().toString());
	}
	
	public ArrayList<waypoint> getPlayersWaypoints(Player p)
	{
		return playWPMap.get(p.getUniqueId().toString());
	}
	
	public ArrayList<String> getPlayersWaypointNames(Player p)
	{
		ArrayList<String> returnlist = new ArrayList<String>();
		for(waypoint w:this.getPlayersWaypoints(p))
		{
			returnlist.add(w.getName());
		}
		return returnlist;
	}
	
	public void newWaypoint(final Player p, String wpname, Location l)
	{
		IllegalCharPair pair = illegalChar(wpname);
		if(pair.getFound())
		{
			BaseComponent message  = new ComponentBuilder("The special character '").color(ChatColor.RED).append(pair.getValue()).color(ChatColor.RED).bold(true).append("' is not allowed").color(ChatColor.RED).bold(false).create()[0];
			p.spigot().sendMessage(message);
		}
		else if(wpname.length() > 42)
		{
			p.sendMessage(ChatColor.RED + "Waypoint name is too long. Maximum length is 42 characters");
		}
		else
		{
			boolean waypointexists = false;
			String foundwaypointsname = "";
			for(waypoint w : this.getPlayersWaypoints(p))
			{
				if(w.getName().equalsIgnoreCase(wpname))
				{
					foundwaypointsname = w.getName();
					waypointexists = true;
					break;
				}
			}
			if(!waypointexists)
			{
				final waypoint WP = new waypoint(p,wpname,l);
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() 
				{
					@Override
					public void run()
					{
						ChatColor messageColor;
						ChatColor wpColor;
						final String message;
						if(db.newWaypoint(WP))
						{
							playWPMap.get(p.getUniqueId().toString()).add(WP);
							message = ChatColor.GREEN + "Waypoint "+ ChatColor.GOLD + ChatColor.BOLD + WP.getName()  + ChatColor.RESET + ChatColor.GREEN  + " has been created!";
						}
						else
						{
							message = ChatColor.RED + "Waypoint "+ ChatColor.GOLD + ChatColor.BOLD +  WP.getName() + ChatColor.RESET + ChatColor.RED  + " couldn't be created";
						}
						Bukkit.getScheduler().runTask(plugin, new Runnable() 
						{
							@Override
							public void run() 
							{
								p.sendMessage(message);
							}
						});
					}
					
				});
			}
			else
			{
				p.sendMessage(ChatColor.RED + "Waypoint " + ChatColor.GOLD + ChatColor.BOLD + foundwaypointsname + ChatColor.RESET + ChatColor.RED + " already exists");
			}
		}
	}
	
	public void deleteWaypoint(final Player p, String wpname)
	{
		IllegalCharPair pair = illegalChar(wpname);
		if(pair.getFound())
		{
			BaseComponent message  = new ComponentBuilder("The special character '").color(ChatColor.RED).append(pair.getValue()).color(ChatColor.RED).bold(true).append("' is not allowed").color(ChatColor.RED).bold(false).create()[0];
			p.spigot().sendMessage(message);
		}
		else
		{
			waypoint foundwaypoint = null;
			for(waypoint w : this.getPlayersWaypoints(p))
			{
				if(w.getName().equalsIgnoreCase(wpname))
				{
					foundwaypoint = w;
					break;
				}
			}
			if(foundwaypoint != null)
			{
				final String deletedwpname = foundwaypoint.getName();
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() 
				{
					@Override
					public void run()
					{
						final String message;
						final Boolean deleted;
						if(db.deleteWaypoint(p, deletedwpname))
						{
							deleted = true;
							message = ChatColor.GREEN + "Waypoint " + ChatColor.GOLD + ChatColor.BOLD + deletedwpname + ChatColor.RESET + ChatColor.GREEN +  " has been deleted!";
						}
						else
						{
							deleted = false;
							message = ChatColor.GREEN + "Waypoint " + ChatColor.GOLD + ChatColor.BOLD + deletedwpname + ChatColor.RESET + ChatColor.GREEN +  " couldn't be deleted.";
						}
						Bukkit.getScheduler().runTask(plugin, new Runnable() 
						{
							@Override
							public void run() 
							{
								if(deleted)
								{
									boolean removedFromList = false;
									for(waypoint wps : playWPMap.get(p.getUniqueId().toString()))
									{
										if(wps.getName().equalsIgnoreCase(deletedwpname)) {
											playWPMap.get(p.getUniqueId().toString()).remove(wps);
											removedFromList = true;
											break;
										}
									}
									if(removedFromList) System.out.println("Error 683. Could not remove waypoint from playermap array in waypointsmanager");
								}
								p.sendMessage(message);
							}
						});
					}
					
				});
			}
			else
			{
				p.sendMessage(ChatColor.RED + "Waypoint " + ChatColor.GOLD + ChatColor.BOLD + wpname + ChatColor.RESET + ChatColor.RED + " doesn't exists");
			}
		}
	}
	
	public void teleportToWaypoint(Player p, String wpname)
	{
		//TODO implement waypoint things
		waypoint foundwaypoint = null;
		for(waypoint w : this.getPlayersWaypoints(p))
		{
			if(w.getName().equalsIgnoreCase(wpname))
			{
				foundwaypoint = w;
				break;
			}
		}
		if(foundwaypoint != null)
		{
			p.sendMessage(ChatColor.GREEN + "Teleporting!");
        	p.teleport(foundwaypoint.l);
		}
		else
		{
			p.sendMessage(ChatColor.RED + "Waypoint " + ChatColor.GOLD + ChatColor.BOLD +  wpname + ChatColor.RESET + ChatColor.RED + " doesn't exists");
		}
	}
	
	public IllegalCharPair illegalChar(String s)
	{
		String special = "~`!@#$%^&*()_-+={[}]\\|;:\"'<,>.?/";
		boolean foundIt = false;
		String valueFound = null;
		for(char i : s.toCharArray())
		{
			if(special.contains(Character.toString(i)))
			{
				valueFound = String.valueOf(i);
				foundIt = true;
				break;
			}
		}
		return new IllegalCharPair(foundIt,valueFound);
	}
}
