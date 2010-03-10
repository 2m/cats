package se.uu.it.cats.brick.storage;

public class SharedByte
{
	private byte _data = 0;
	
	public byte getData()
	{
		return _data;
	}
	
	public void setData(byte b)
	{
		setData(b, true);
	}
	
	public void setData(byte b, boolean inform)
	{
		_data = b;
		
		if (inform)
			// inform StorageManager about the update
			StorageManager.getInstance().informAboutUpdate(this);
	}
	
	public void updateData(byte b)
	{
		// should be called by StorageManager
		// new data from network, so do not need to inform anyone
		setData(b, false);
	}
}