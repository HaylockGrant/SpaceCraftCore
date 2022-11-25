package mc.Spacecraft.Core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerHome {
	
	JavaPlugin plugin;
	DataBaseManager db;
	
	public PlayerHome(JavaPlugin plugin, DataBaseManager database) 
	{
		this.plugin = plugin;
		db = database;
	}

	
	public void newPlayer(final Player player)
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() 
		{
		    @Override
		    public void run() 
		    {
		    	final boolean newP;
		    	if(db.getConnectionStatus(true))
			    {
			    	newP = (db.getHome(player) == null);
			    	//TODO check if database is running
			    	Bukkit.getScheduler().runTask(plugin, new Runnable() 
			    	{
			    	    @Override
			    	    @SuppressWarnings("deprecation")
			    	    public void run() 
			    	    {
			    	        if(newP)
			    	        {
			    	        	player.sendTitle(ChatColor.DARK_GREEN + "Welcome To The Landing Zome", "", 10, 150, 10);
			    	        	player.sendMessage(ChatColor.DARK_GREEN + "Remember to set your home");
			    				TextComponent message = new TextComponent("To set your home do /sethome or click ");
			    				message.setColor(ChatColor.DARK_GREEN);
			    				TextComponent messagep2 = new TextComponent("here");
			    				messagep2.setColor(ChatColor.GREEN);
			    				messagep2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sethome"));
			    				messagep2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
			    						new ComponentBuilder("Yup, Click here").color(ChatColor.DARK_GREEN).bold(true).create()));
			    				message.addExtra(messagep2);
			    				player.spigot().sendMessage(message);
			    	        } else player.sendTitle(ChatColor.RED + "Welcome Back", "", 20, 60, 20);
			    	    }
			    	});
			    } else {
			    	player.sendTitle(ChatColor.RED + "Welcome Back", "", 20, 60, 20);
			    	player.sendMessage(ChatColor.RED + "Could not get your home at this time");
			    }
		    	//TODO check if database is running
		    }
		});
	}

	public void setHome(final Player player, final Location loc) 
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() 
		{
		    @Override
		    public void run() 
		    {
		    	final boolean homeSet = db.setHome(player, loc);
		    	
		    	Bukkit.getScheduler().runTask(plugin, new Runnable() {
		    	    @Override
		    	    public void run() {
		    	    	if(homeSet)
		    			{
		    				player.sendMessage(ChatColor.GREEN + "Home set!");
		    			} else player.sendMessage(ChatColor.RED + "Could not set your home at this time");
		    	    }
		    	});
		    }
		});
	}

	@SuppressWarnings("deprecation")
	public void sendHome(final Player player) 
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
		    @Override
		    public void run() 
		    {
		        final Location l = db.getHome(player);
		        Bukkit.getScheduler().runTask(plugin, new Runnable() {
		            @Override
		            public void run() 
		            {
		                if(l != null)
		                {
		                	player.sendMessage(ChatColor.GREEN + "Teleporting!");
		                	player.teleport(l);
		                } else {
		                	player.sendMessage(ChatColor.RED + "You don't have a home set yet");
		        			TextComponent message = new TextComponent("To set your home do /sethome or click ");
		        			message.setColor(ChatColor.DARK_GREEN);
		        			TextComponent messagep2 = new TextComponent("here");
		        			messagep2.setColor(ChatColor.GREEN);
		        			messagep2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sethome"));
		        			messagep2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
		        					new ComponentBuilder("Yup, Click here").color(ChatColor.DARK_GREEN).bold(true).create()));
		        			message.addExtra(messagep2);
		        			player.spigot().sendMessage(message);
		                }
		            }
		        });
		    }
		});
	}
}
