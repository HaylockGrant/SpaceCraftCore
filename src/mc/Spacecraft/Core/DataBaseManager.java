package mc.Spacecraft.Core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;


//import org.mariadb.jdbc.Driver;

public class DataBaseManager 
{
	Connection connection = null;
	private String url;// = "jdbc:mysql://localhost:3306/SpaceCraft";
	private String username;
	private String password;
	private long timeOfQuerry;
	private TelegramViaNodeRed telegram;
	
	public DataBaseManager(String location,String port,String db,String username,String password, TelegramViaNodeRed t) 
	{
		this.url = "jdbc:mysql://" + location + ":" + port + "/" + db;
		this.username = username;
		this.password = password;
		timeOfQuerry = Long.MAX_VALUE;
		telegram = t; 
		try {
			connection = DriverManager.getConnection(url, username, password);
			timeOfQuerry = System.currentTimeMillis();
			System.out.println("Connected!");
		} catch (SQLException e) {
			System.out.println("Failed to connect to database");
			e.printStackTrace();
		}
	}

	public boolean close() 
	{
		if(getConnectionStatus(false))
		{
			try {
				connection.close();
				System.out.println("Closed Connection");
				return true;
			} catch (SQLException e) {
				System.out.println("Coudln't close");
				e.printStackTrace();
				return false;
			}
		} else {
			System.out.println("Not open");
			return true;
		}
	}
	
	public boolean connect()
	{
		return(connect(false));
	}
	
	private boolean connect(boolean override)
	{
		if(override || !getConnectionStatus(false))
		{
			try {
				connection = DriverManager.getConnection(url, username, password);
				timeOfQuerry = System.currentTimeMillis();
				if(!override) System.out.println("Connected!");
				//TODO make mqtt tell node red to tell telegram bot to tell me that the database went off and had to be reconected
				return true;
			} catch (SQLException e) {
				System.out.println("Failed to reconnect to database. This is bad.");
				telegram.sendApiMessage("SpaceCore failed to connect to the database...");
				e.printStackTrace();
				return false;
			}
		} else {
			System.out.println("Already Connected");
			return true;
		}
	}
	
	public boolean setHome(Player p, Location l)
	{
		String uuid = p.getUniqueId().toString();
		String worldname = l.getWorld().getName();
		double x = l.getX();
		double y = l.getY();
		double z = l.getZ();
		float pitch = l.getPitch();
		float yaw = l.getYaw();
		
		if(getConnectionStatus(true))
		{
			try 
			{
				Statement stmt2 = connection.createStatement();
				String SQL2 = "INSERT INTO homes(uuid, world, x, y, z, pitch, yaw) VALUES ('" + uuid + "', '" + worldname + "', " + x + ", " + y + ", " + z + ", " + yaw + ", " + pitch + ")";
				stmt2.execute(SQL2);
				timeOfQuerry = System.currentTimeMillis();
				return true;
			}
			catch(SQLException e)
			{
				System.out.println("Couldn't upload homes for some reason");
				return false;
			}
		} else return false;
	}
	
	public Location getHome(Player p)
	{
		String uuid = p.getUniqueId().toString();
		if(getConnectionStatus(true))
		{
			try {
				Statement stmt = connection.createStatement();
				String SQL = "SELECT world, x, y, z, pitch, yaw FROM homes WHERE uuid = '" + uuid + "'" + " ORDER BY times DESC LIMIT 1";
				ResultSet rs = stmt.executeQuery(SQL);
				timeOfQuerry = System.currentTimeMillis();
				Location l = null;
				while(rs.next())
				{
					String worldname = rs.getString("world");
					double x = rs.getDouble("x");
					double y = rs.getDouble("y");
					double z = rs.getDouble("z");
					float pitch = rs.getFloat("pitch");
					float yaw = rs.getFloat("yaw");
					l = new Location(Bukkit.getWorld(worldname),x,y,z,pitch,yaw);
					break;
				} 
				return l;
			} catch (SQLException e) {
				System.out.println("Couldn't get player home for some reason");
				return null;
			}
		} else return null;
	}
	
//	public ArrayList<String> getWaypointNames(Player p)
//	{
//		ArrayList<String> waypoints = new ArrayList<String>();
//		String uuid = p.getUniqueId().toString();
//		if(getConnectionStatus(true))
//		{
//			try {
//				String SQL = "SELECT name FROM waypoints WHERE uuid = ?";
//				PreparedStatement updateStatement = connection.prepareStatement(SQL);
//				updateStatement.setString(1, uuid);
//				ResultSet rs = updateStatement.executeQuery();
//				timeOfQuerry = System.currentTimeMillis();
//				while(rs.next())
//				{
//					String name = rs.getString("name");
//					waypoints.add(name);
//				}
//			} catch (SQLException e) {
//				System.out.println("Couldn't get player waypoints for some reason");
//			}
//		}
//		return waypoints;
//	}
	
	public ArrayList<waypoint> getWaypoints(Player p)
	{
		ArrayList<waypoint> waypoints = new ArrayList<waypoint>();
		String uuid = p.getUniqueId().toString();
		if(getConnectionStatus(true))
		{
			try 
			{
				
				String SQL = "SELECT name, world, x, y, z, pitch, yaw FROM waypoints WHERE uuid = ?";
				PreparedStatement updateStatement = connection.prepareStatement(SQL);
				updateStatement.setString(1, uuid);
				ResultSet rs = updateStatement.executeQuery();
				timeOfQuerry = System.currentTimeMillis();
				while(rs.next())
				{
					String name = rs.getString("name");
					String worldname = rs.getString("world");
					double x = rs.getDouble("x");
					double y = rs.getDouble("y");
					double z = rs.getDouble("z");
					float pitch = rs.getFloat("pitch");
					float yaw = rs.getFloat("yaw");
					Location l = new Location(Bukkit.getWorld(worldname),x,y,z,pitch,yaw);
					waypoint w = new waypoint(p,name,l);
					waypoints.add(w);
				} 
			} catch (SQLException e) {
				System.out.println("Couldn't get player waypoints for some reason");
				return null;
			}
		}
		return waypoints;
	}
	
	public boolean newWaypoint(waypoint w)
	{
		if(getConnectionStatus(true))
		{
			String uuid = w.p.getUniqueId().toString();
			String name = w.wp;
			String worldname = w.l.getWorld().getName();
			double x = w.l.getX();
			double y = w.l.getY();
			double z = w.l.getZ();
			float pitch = w.l.getPitch();
			float yaw = w.l.getYaw();
			try 
			{
//				Statement stmt = connection.createStatement();
//				String SQL = "INSERT INTO waypoints VALUES ('" + uuid + "', '" + name + "','" + worldname + "', " + x + ", " + y + ", " + z + ", " + yaw + ", " + pitch + ")";
//				stmt.execute(SQL);
//				timeOfQuerry = System.currentTimeMillis();
				
				String updateString = "INSERT INTO waypoints VALUES (?,?,?,?,?,?,?,?)";
				PreparedStatement updateStatement = connection.prepareStatement(updateString);
				updateStatement.setString(1, uuid);
				updateStatement.setString(2, name);
				updateStatement.setString(3, worldname);
				updateStatement.setDouble(4, x);
				updateStatement.setDouble(5, y);
				updateStatement.setDouble(6, z);
				updateStatement.setFloat(7, pitch);
				updateStatement.setFloat(8, yaw);
				updateStatement.executeUpdate();
				timeOfQuerry = System.currentTimeMillis();
				return true;
			}
			catch (SQLException e)
			{
				System.out.println("Couldn't set player waypoint for some reason");
				return false;
			}
		}
		return false;
	}
	
	public boolean deleteWaypoint(Player p, String waypoint)
	{
		if(getConnectionStatus(true))
		{
			try
			{
				String SQL = "DELETE FROM waypoints WHERE uuid = ? AND name = ?";
				PreparedStatement updateStatement = connection.prepareStatement(SQL);
				updateStatement.setString(1, p.getUniqueId().toString());
				updateStatement.setString(2, waypoint);
				updateStatement.executeUpdate();
				timeOfQuerry = System.currentTimeMillis();
				return true;
			}
			catch (SQLException e)
			{
				System.out.println("Couldn't delete player waypoint for some reason");
				return false;
			}
		}
		return false;
	}
	
	public boolean logConnection(String ip, String uuid, boolean allowed)
	{
		if(getConnectionStatus(true))
		{
			try 
			{
				String insertString = "INSERT INTO loginlogs (address,uuid,allowed) VALUES (?,?,?)";
				PreparedStatement updateStatement = connection.prepareStatement(insertString);
				updateStatement.setString(1, ip);
				updateStatement.setString(2, uuid);
				updateStatement.setBoolean(3, allowed);
				updateStatement.executeUpdate();
				timeOfQuerry = System.currentTimeMillis();
				return true;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				System.out.println("Couldn't make IP logs for some reason");
				return false;
			}
		} else return false;
	}
	
	public boolean logVPNAPI(String ip, String json)
	{
		if(getConnectionStatus(true))
		{
			try
			{
				String insertString = "INSERT INTO vpnapi (address,json) VALUES (?,?)";
				PreparedStatement updateStatement = connection.prepareStatement(insertString);
				updateStatement.setString(1, ip);
				updateStatement.setString(2, json);
				updateStatement.executeUpdate();
				timeOfQuerry = System.currentTimeMillis();
				return true;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				System.out.println("Couldn't make VPNAPI logs for some reason");
				return false;
			}
		} else return false;
	}
	
	public String getVPNAPI(String ip) throws Exception
	{
		try {
			return getVPNAPI(ip,7);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public String getVPNAPI(String ip, int days) throws Exception
	{
		if(getConnectionStatus(true))
		{
			try 
			{
				String SQL = "SELECT json FROM vpnapi where address = ? AND times >= (DATE_SUB(NOW(),INTERVAL ? DAY)) order by times desc limit 1";
				PreparedStatement updateStatement = connection.prepareStatement(SQL);
				updateStatement.setString(1, ip);
				updateStatement.setInt(2, days);
				ResultSet rs = updateStatement.executeQuery();
				timeOfQuerry = System.currentTimeMillis();
				String json = "";
				if(rs.next())
				{
					json = rs.getString("json");
				}
				else
				{
					throw new Exception("row count 0");
				}
				return json;
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
				System.out.println("Couldn't get VPNAPI for some reason");
				throw new Exception("DataBase Down");
			}
		} else throw new Exception("DataBase Down");
	}
	
	public boolean getConnectionStatus()
	{
		return getConnectionStatus(false);
	}
	
	public boolean getConnectionStatus(Boolean AttemptReconnect)
	{
		boolean connected = false;
		try 
		{
			Statement stmt = connection.createStatement();
			String SQL = "SELECT alive FROM alive";
			ResultSet rs = stmt.executeQuery(SQL);
			timeOfQuerry = System.currentTimeMillis();
			while (rs.next()) 
			{
				Boolean value = rs.getBoolean("alive");
				if (value == true) 
				{
					connected = true;
					break;
				} 
				else 
				{
					connected = false;
					// ERROR 221 this line should never execute, maybe your database has a bad
					// variable in it
					System.out.println("Error 221 in DataBasemanager");
				}
			}
		} 
		catch (SQLException e) 
		{
			try 
			{
				if(connection.isClosed() == false)
				{
					System.out.println("Error 222 in DataBaseManager");
					//ERROR 222 when the isclosed method doesn't line up with my connection status
				}
			} 
			catch (SQLException e1) 
			{
				System.out.println("Error 223 in DataBaseManager");
				//ERROR 223 failed getting the isClosed connection from build in function
			}
			connected = false;
			System.out.println("Couldn't connect to the database for some reason");
		}
		if(AttemptReconnect)
		{
			connect(true);
			connected = getConnectionStatus(false);
		}
		return connected;
	}
	
	public long getTimeSinceLastQuerry()
	{
		return System.currentTimeMillis() - timeOfQuerry;
	}
}
