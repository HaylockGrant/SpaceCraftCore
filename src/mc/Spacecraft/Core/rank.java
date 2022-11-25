package mc.Spacecraft.Core;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
public class rank 
{
	String title;
	ChatColor color;
	
	
	public rank(String title, ChatColor chatcolor) 
	{
		this.title = title;
		color = chatcolor;
	}
	
	public String getTitleString()
	{
		return title;
	}
	
	public TextComponent getTitle()
	{ 
		if(title.equalsIgnoreCase(""))
		{
			return(new TextComponent(ChatColor.GRAY + ""));
		}
		else
		{
			return((TextComponent) new ComponentBuilder(title).bold(true).color(color).create()[0]);
		}
	}
	
	public ChatColor getColor()
	{
		return(color); 
	}
}
