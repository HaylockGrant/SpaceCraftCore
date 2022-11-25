package mc.Spacecraft.Core;
import java.util.Properties;

public class ApiConfig
{
	Properties configFile;
	public ApiConfig()
	{
		configFile = new java.util.Properties();
		try 
		{
			configFile.load(this.getClass().getClassLoader().getResourceAsStream("ApiConfig.cfg"));
		}
		catch(Exception eta)
		{
			eta.printStackTrace();
		}
	}

	public String getProperty(String key)
	{
		String value = this.configFile.getProperty(key);
		return value;
	}
}
