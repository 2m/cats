package se.uu.it.cats.pc.network;

import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.packet.Packet;
import se.uu.it.cats.pc.network.ConnectionHandler;

public class ConnectionManager
{
	public static final String[] _deviceNames = new String[] {"cat1", "cat2", "cat3"};
	
	public static final int MAX_OUTBOUND_CONN = _deviceNames.length;
	
	private static ConnectionManager _instanceHolder = new ConnectionManager();
	
	private ConnectionHandler[] _outboundConnectionHolder = null;
	String[][] _ignoreNames = null;
	
	public static ConnectionManager getInstance()
	{
		return _instanceHolder;
	}
	
	private ConnectionManager()
	{
		_outboundConnectionHolder = new ConnectionHandler[MAX_OUTBOUND_CONN];
		
		_ignoreNames = new String[MAX_OUTBOUND_CONN][MAX_OUTBOUND_CONN - 1];		
	}
	
	public void openConnection(String name)
	{
		int id = getIdByName(name);
		
		if (id == -1)
			return;
		
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
	
	public void addIgnore(String source, String dest)
	{
		int id = getIdByName(source);
		
		for (int i = 0; i < _ignoreNames[id].length; i++)
			if (_ignoreNames[id][i] == null)
			{
				_ignoreNames[id][i] = dest;
				return;
			}		
		
	}
	
	public void remIgnore(String source, String dest)
	{
		int id = getIdByName(source);
		
		for (int i = 0; i < _ignoreNames[id].length; i++)
			if (dest.equals(_ignoreNames[id][i]))
				_ignoreNames[id][i] = null;
		
	}
	
	public boolean isCreated(int i)
	{
		return getConnection(i) != null;
	}
	
	public boolean isAlive(int i)
	{
		try
		{
			return isCreated(i) && getConnection(i).isAlive();
		}
		catch (NullPointerException ex)
		{
			return false;
		}
	}
	
	public ConnectionHandler getConnection(int i)
	{
		if (i < MAX_OUTBOUND_CONN)
		{
			return _outboundConnectionHolder[i];
		}
		
		return null;
	}
	
	public int getIdByName(String name)
	{
		for (int i = 0; i < _deviceNames.length; i++)
			if (_deviceNames[i].equals(name))
				return i;
		
		Logger.println("Unknown name to connect to.");
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
	
	public void relayPacketToAllExcept(Packet p, String name)
	{
		for (int i = 0; i < MAX_OUTBOUND_CONN; i++)
		{
			if (isAlive(i) && !getConnection(i).getRemoteName().equals(name))
			{
				int id = getIdByName(name);
				for (String ignoreName: _ignoreNames[id])
					if (name.equals(ignoreName))
						return;
				
				getConnection(i).relayPacket(p);
			}				
		}
	}
	
	public void sendPacketTo(String name, Packet p)
	{
		int id = getIdByName(name);
		
		if (id == -1)
			return;
		
		if (isAlive(id))
		{
			getConnection(id).sendPacket(p);
			return;
		}
		
		Logger.println("Connection not open to "+name);
	}
	
	private void printIgnores()
	{
		for (int i = 0; i < _ignoreNames.length; i++)
			for (int j = 0; j < _ignoreNames[i].length; j++)
				System.out.println(i+": "+j+": "+_ignoreNames[i][j]);
	}
}
