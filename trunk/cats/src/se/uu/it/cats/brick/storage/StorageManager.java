package se.uu.it.cats.brick.storage;

import se.uu.it.cats.brick.network.ConnectionManager;

public class StorageManager
{
	private static StorageManager _instanceHolder = new StorageManager();
	
	public static SharedByte sb = new SharedByte();
	
	public static StorageManager getInstance()
	{
		return _instanceHolder;
	}
	
	public void dataInput(byte b)
	{
		sb.updateData(b);
	}
	
	public void informAboutUpdate(SharedByte sb)
	{
		ConnectionManager.getInstance().sendByteToAll(sb.getData());
	}
	
	public byte getData()
	{
		return sb.getData();
	}
	
	public void setData(byte b)
	{
		sb.setData(b);
	}
}
