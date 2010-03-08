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
		_data = b;
		
		// inform StorageManager about the update
		StorageManager.getInstance().informAboutUpdate(this);
	}
	
	public void updateData(byte b)
	{
		// should be called by StorageManager
		setData(b);
	}
}