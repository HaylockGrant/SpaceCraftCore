package mc.Spacecraft.Core;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
public class JSON 
{
	private static ObjectMapper objectMapper = getDefaultMapper();
	
	private static ObjectMapper getDefaultMapper()
	{
		ObjectMapper defaultObjectMapper = new ObjectMapper();
		return defaultObjectMapper;
	}
	
	public static JsonNode parse(String source)
	{
		try 
		{
			JsonNode node = objectMapper.readTree(source);
			return node;
		} catch (Exception e)
		{
			System.out.println("Json couldn't be parsed, Returned Null");
			return null;
		}
	}
	
	public static ObjectNode makeObjectNode()
	{
		try
		{
			return getDefaultMapper().createObjectNode();
		}
		catch(Exception e)
		{
			System.out.println("Json couldn't be generated, returned null");
			return null;
		}
	}
	
	public static String getPrettyJson(ObjectNode obj)
	{
		try
		{
			return getDefaultMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static String getNormalJson(ObjectNode obj)
	{
		try
		{
			return getDefaultMapper().writer().writeValueAsString(obj);
		}
		catch(Exception e)
		{
			return null;
		}
	}
}