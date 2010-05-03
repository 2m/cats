package se.uu.it.cats.pc.network;

import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.pc.network.ConnectionHandler;

public class ConnectionManager
{
	public static final String[] _deviceNames = new String[] {"cat1", "cat2", "cat2"};
	
	public static final int MAX_OUTBOUND_CONN = _deviceNames.length;
	
	private static ConnectionManager _instanceHolder = new ConnectionManager();
	
	private ConnectionHandler[] _outboundConnectionHolder = null;
	
	public static ConnectionManager getInstance()
	{
		return _instanceHolder;
	}
	
	private ConnectionManager()
	{
		_outboundConnectionHolder = new ConnectionHandler[MAX_OUTBOUND_CONN];
	}
	
	public void openConnection(String name)
	{
		int id = getIdByName(name);
		
		if (isCreated(id))
		{
			Logger.println("Connection already created.");
			return;
		}
		
		ConnectionHandler ch = null;
		
		ch = new ConnectionHandler(name);
		
		_outboundConnectionHolder[id] = ch;
		Thread initiatorThread = new Thread(ch);
		initiatorThread.start();
	}
	
	public boolean isCreated(int i)
	{
		return getConnection(i) != null;
	}
	
	public ConnectionHandler getConnection(int i)
	{
		if (i < MAX_OUTBOUND_CONN)
		{
			return _outboundConnectionHolder[i];
		}
		
		return null;
	}
	
	public static int getIdByName(String name)
	{
		for (int i = 0; i < _deviceNames.length; i++)
			if (_deviceNames[i].equals(name))
				return i;
		
		return -1;
	}
	
	public void closeConnection(int i)
	{
		if (isCreated(i))
		{
			getConnection(i).close();
		}
		
		if (i < MAX_OUTBOUND_CONN)
		{
			_outboundConnectionHolder[i] = null;			
		}
	}
	
	public void closeConnection(ConnectionHandler conn)
	{
		for (int i = 0; i < _outboundConnectionHolder.length; i++)
			if (_outboundConnectionHolder[i] == conn)
			{
				closeConnection(i);
				return;
			}
	}
}
