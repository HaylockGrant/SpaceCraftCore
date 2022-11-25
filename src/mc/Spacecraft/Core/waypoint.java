package mc.Spacecraft.Core;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class waypoint 
{
	protected Player p;
	protected String wp;
	protected Location l;
	public waypoint(Player p, String name, Location l)
	{
		this.p = p;
		wp = name;
		this.l = l;
	}
	public String getName() 
	{
		return wp;
	}
}
