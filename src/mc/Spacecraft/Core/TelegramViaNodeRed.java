package mc.Spacecraft.Core;

public class TelegramViaNodeRed 
{
	protected String address;
	public TelegramViaNodeRed(String url)
	{
		address = url;
	}
	public void sendApiMessage(final String message)
	{
		new Thread(new Runnable() {
			@Override
			public void run() 
			{
				URLConnectionReader.sendDatas(address+StringsEncoder.encode(JSON.getNormalJson(JSON.makeObjectNode().put("Notify", message))));
				return;
			}
		}).start();
	}
}
