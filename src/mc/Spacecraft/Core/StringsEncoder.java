package mc.Spacecraft.Core;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
public class StringsEncoder 
{
	public static String encode(String message)
	{
		String url = URLEncoder.encode(message, StandardCharsets.UTF_8);
		url = url.replace("+","%20");
		return url;
	}
}
