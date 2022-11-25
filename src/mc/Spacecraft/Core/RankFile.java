package mc.Spacecraft.Core;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class RankFile extends AbstractFile {
	
	protected ArrayList<rank> rankList = new ArrayList<rank>(0);

	public RankFile(Main main) 
	{
		super(main, "ranks.yml");
		rankList.add(new rank("",ChatColor.GRAY)); //Starts at 0
		rankList.add(new rank("Fancy",ChatColor.LIGHT_PURPLE));
		rankList.add(new rank("Bat",ChatColor.DARK_PURPLE));
		rankList.add(new rank("PresidentialElect",ChatColor.BLUE));
		rankList.add(new rank("Kitten",ChatColor.GREEN));
		rankList.add(new rank("Unwell",ChatColor.DARK_GREEN));
		rankList.add(new rank("Kitty",ChatColor.DARK_PURPLE));
		rankList.add(new rank("DaQueen",ChatColor.DARK_AQUA));
		rankList.add(new rank("DaBoy",ChatColor.GOLD));
		rankList.add(new rank("Parental Figure",ChatColor.BLUE));
		rankList.add(new rank("Corn",ChatColor.GOLD));
		rankList.add(new rank("Viking BlackSmith",ChatColor.DARK_PURPLE));
		rankList.add(new rank("\"Straight\"",ChatColor.AQUA));
		rankList.add(new rank("Dangerous",ChatColor.RED));
		rankList.add(new rank("Gold Standard",ChatColor.GOLD));
	}

	public void newPlayer(Player player) 
	{
		if (!config.contains(player.getUniqueId().toString())) 
		{
			config.set(player.getUniqueId().toString(), 0);
		}
	}

	public void setrank(Player player, int num) {
		config.set(player.getUniqueId().toString(), null);
		config.set(player.getUniqueId().toString(), num);
	}

	public int getRankValue(Player player) {
		return Integer.parseInt(config.get(player.getUniqueId().toString()).toString());
	}
	

	public TextComponent getRankTitle(Player player) {
		int rank = getRankValue(player);
		return getRankTitle(rank);
	}
	
	public TextComponent getRankTitle(int rank) {
		TextComponent title;		
		if(rank <= this.getTotalRanks())
		{
			title = rankList.get(rank).getTitle();
		}
		else
		{
			title = (TextComponent) new ComponentBuilder("Hacker").bold(true).color(ChatColor.DARK_RED).create()[0];
		}
		return title;
	}
	
	public TextComponent getName(Player player) {
		int rank = getRankValue(player);
		TextComponent name;
		if(rank <= this.getTotalRanks())
		{
			name = new TextComponent(rankList.get(rank).getColor() + player.getName());
		}
		else
		{
			name = new TextComponent(ChatColor.DARK_RED + player.getName());
		}
		return name;
	}

	public TextComponent getTitleAndName(Player player) {
		int rank = getRankValue(player);
		TextComponent title = this.getRankTitle(player);
		TextComponent space = new TextComponent(" ");
		TextComponent name = this.getName(player);
		if (rank == 0) 
		{
			return name;
		} else {
			title.addExtra(space);
			title.addExtra(name);
			return title;
		}
	}
	
	public int getTotalRanks()
	{
		return(rankList.size()-1);
	}
	
}
