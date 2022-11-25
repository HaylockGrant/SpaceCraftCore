package mc.Spacecraft.Core;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class URLConnectionReader 
{	
	public static String getDatas(String url)
	{
		try
		{
			URL u = new URL(url);
			URLConnection con = u.openConnection();
			con.setConnectTimeout(1500);
			con.setReadTimeout(1500);
			try (InputStream in = con.getInputStream()) 
			{
			    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
		catch (Exception e)
		{
			System.out.println("Error occured when getting URL connection");
			e.printStackTrace();
			return "";
		}
	}
	
	public static void sendDatas(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URL u = new URL(url);
					URLConnection con = u.openConnection();
					con.getInputStream();
					con.setConnectTimeout(100);
					con.setReadTimeout(100);
					try (InputStream in = con.getInputStream()) {
					}
				} catch (Exception e) {
					System.out.println("Error occured when sending URL connection");
				}
			}
		}).start();
	}
}