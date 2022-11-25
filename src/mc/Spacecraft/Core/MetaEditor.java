package mc.Spacecraft.Core;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;


public class MetaEditor

{
	Plugin plugin;

	public MetaEditor(Plugin plugin) {
		this.plugin = plugin;
	}

	public boolean setSending(Player p, String sendingTo) {
		/*
		 * this method will set the sending metadata to the sending to value if the
		 * values are the same then will return true
		 */
		boolean isTrue = false;
		if (p.hasMetadata("Sending")) {
			if (p.getMetadata("Sending").get(0).asString().equalsIgnoreCase(sendingTo)) {
				isTrue = true;
			}
			p.removeMetadata("Sending", plugin);
		}
		p.setMetadata("Sending", new FixedMetadataValue(plugin, sendingTo));
		return isTrue;
	}

	public String getSending(Player p) {
		if (p.hasMetadata("Sending")) {
			return p.getMetadata("Sending").get(0).asString();
		} else {
			return null;
		}
	}

	public void clearSending(Player p) {
		if (p.hasMetadata("Sending")) {
			p.removeMetadata("Sending", plugin);
		}
	}

	public boolean SetReceiving(Player p, String receivingFrom) {
		/*
		 * this method will set the sending metadata to the receiving to value if the
		 * values are the same then will return true
		 */
		boolean isTrue = false;
		if (p.hasMetadata("Receiving")) {
			if (p.getMetadata("Receiving").get(0).asString().equalsIgnoreCase(receivingFrom)) {
				isTrue = true;
			}
			p.removeMetadata("Receiving", plugin);
		}
		p.setMetadata("Receiving", new FixedMetadataValue(plugin, receivingFrom));
		return isTrue;
	}

	public String getReceiving(Player p) {
		if (p.hasMetadata("Receiving")) {
			return p.getMetadata("Receiving").get(0).asString();
		} else {
			return null;
		}
	}

	public void clearReceiving(Player p) {
		if (p.hasMetadata("Receiving")) {
			p.removeMetadata("Receiving", plugin);
		}
	}

	public boolean areSame(Player sender, Player receiver) 
	{
		if(this.getSending(sender) != null && this.getReceiving(receiver) != null)
		{
			return(this.getSending(sender).equalsIgnoreCase(receiver.getName()) && this.getReceiving(receiver).equalsIgnoreCase(sender.getName()));
		}
		else
		{
			return false;
		}
		
	}
	
	public void clearBoth(Player sender, Player receiver)
	{
		this.clearSending(sender);
		this.clearReceiving(receiver);
	}
	
	public long timeSinceCheck(Player p)
	{
		if (p.hasMetadata("timeSince")) 
		{
			long pdata = p.getMetadata("timeSince").get(0).asLong();
			long timesince = (System.currentTimeMillis() - pdata);
			p.setMetadata("timeSince", new FixedMetadataValue(plugin, System.currentTimeMillis()));
			return timesince;
		} 
		else 
		{
			p.setMetadata("timeSince", new FixedMetadataValue(plugin, System.currentTimeMillis()));
			return 1814400000;
		}
	}
	
	public void setSendingTime(Player p)
	{
		if(p.hasMetadata("sendingTime"))
		{
			p.removeMetadata("sendingTime", plugin);
		}
		p.setMetadata("sendingTime", new FixedMetadataValue(plugin, System.currentTimeMillis()));
	}
	
	public void setRecievingTime(Player p)
	{
		if(p.hasMetadata("recievingTime"))
		{
			p.removeMetadata("recievingTime", plugin);
		}
		p.setMetadata("recievingTime", new FixedMetadataValue(plugin, System.currentTimeMillis()));
	}
	
	public long getSendingTime(Player p)
	{
		if(p.hasMetadata("sendingTime"))
		{
			return p.getMetadata("sendingTime").get(0).asLong();
		}
		else return 0;
	}
	
	public long getRecievingTime(Player p)
	{
		if(p.hasMetadata("recievingTime"))
		{
			return p.getMetadata("recievingTime").get(0).asLong();
		}
		else return 0;
	}
	
	public void clearSendingTime(Player p)
	{
		if(p.hasMetadata("sendingTime"))
		{
			p.removeMetadata("sendingTime", plugin);
		}
	}
	
	public void clearRecievingTime(Player p)
	{
		if(p.hasMetadata("recievingTime"))
		{
			p.removeMetadata("recievingTime", plugin);
		}
	}
	
	public void clearBothTime(Player sender, Player reciever)
	{
		this.clearSendingTime(sender);
		this.clearRecievingTime(reciever);
	}
}
