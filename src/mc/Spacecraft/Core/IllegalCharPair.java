package mc.Spacecraft.Core;

public class IllegalCharPair 
{
	protected boolean found;
	protected String value;
	
	public IllegalCharPair(boolean found, String value)
	{
		this.found = found;
		this.value = value;
	}
	
	public boolean getFound()
	{
		return found;
	}
	
	public String getValue()
	{
		return value;
	}
}
