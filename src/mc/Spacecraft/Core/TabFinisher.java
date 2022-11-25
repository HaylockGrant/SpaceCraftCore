package mc.Spacecraft.Core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class TabFinisher implements TabCompleter
{	
	WaypointManager wpman;
	public TabFinisher(WaypointManager wpmanager)
	{
		wpman = wpmanager;
	}
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) 
	{
		List<String> commands = new ArrayList<String>();
        List<String> completions = new ArrayList<String>();
        List<String> playersonline = new ArrayList<String>();
		for (Object p : Bukkit.getOnlinePlayers().toArray())
		{
			playersonline.add(((Player) p).getName());
		}
		if(sender instanceof Player)
		{
			Player p = (Player)sender;
			switch(command.getName().toLowerCase()) 
			{
				case "tpa":
					commands.addAll(playersonline);
					break;
				case "wp": case "waypoint": case "dwp": case "deletewaypoint":
					commands.addAll(wpman.getPlayersWaypointNames(p));
					break;
				default:
					break;
			}
		}
		StringUtil.copyPartialMatches(args[0], commands, completions);
//        if (args.length == 1) 
//        {
//        	if(!(sender instanceof Player))
//        	{
//        		commands.add("rankup");
//        		commands.add("rankdown");
//        		commands.add("setrank");
//        	}
//        	if((sender.isOp()) || !(sender instanceof Player))
//        	{
//        		
//        		commands.add("reload");
//        		commands.add("coreversion");
//        	}
//        	if((sender instanceof Player))
//        	{
//        		commands.add("tpa");
//        		commands.add("tpaccept");
//        		commands.add("home");
//        		commands.add("sethome");
//        	}
//            StringUtil.copyPartialMatches(args[0], commands, completions);
//        } else if (args.length == 2) {
//        	switch(args[0])
//        	{
//        		case "wp":
//        			commands.addAll(playersonline);
//        			break;
//        		case "tpa":
//        			commands.addAll(playersonline);
//        			break;
//        		default:
//        			commands.add("");
//        			break;
//        	}
//            StringUtil.copyPartialMatches(args[1], commands, completions);
//        }
        
        
        //sort the list
        Collections.sort(completions);
        return completions;
    }
}

