package se.uu.it.cats.pc.network;

import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.packet.Packet;

public class ConnectionManager
{
	public final Device[] _devices = new Device[] {
		new Device("cat0", "00165302CDC3"),
		new Device("cat1", "00165302CC4E"),
		new Device("cat2", "0016530E6938"),		
		new Device("dongle1", "0015832A3670"),
		new Device("dongle2", "000C783394E7"),
		new Device("MartinPC", "002556F9072D"),
		new Device("ChristianPC", "002243B7BDAA")
	};
	
	public final int MAX_OUTBOUND_CONN = _devices.length;
	
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
				//printIgnores();
				return;
			}		
		
	}
	
	public void remIgnore(String source, String dest)
	{
		int id = getIdByName(source);
		
		for (int i = 0; i < _ignoreNames[id].length; i++)
			if (dest.equals(_ignoreNames[id][i]))
				_ignoreNames[id][i] = null;
		
		//printIgnores();
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
		for (int i = 0; i < _devices.length; i++)
			if (_devices[i].getName().equals(name))
				return i;
		
		Logger.println("Unknown name to get id by.");
		return -1;
	}
	
	public String getNameByAddress(String address)
	{
		for (int i = 0; i < _devices.length; i++)
			if (_devices[i].getAddress().equals(address))
				return _devices[i].getName();
		
		Logger.println("Unknown address for name search.");
		return null;
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
	
	public void relayPacketToAll(Packet p)
	{
		relayPacketFrom(p, null);
	}
	
	public synchronized void relayPacketFrom(Packet p, String name)
	{
		for (int i = 0; i < MAX_OUTBOUND_CONN; i++)
		{
			// check if connection open
			if (!isAlive(i))
				continue;
			
			String remoteName = getConnection(i).getRemoteName();
			if (!remoteName.equals(name))
			{
				boolean ignore = false;
				
				// if name is null, then packet is being sent from GUI
				// and has to be sent to everyone connected
				if (name != null)
				{
					int id = getIdByName(name);
					
					for (String ignoreName: _ignoreNames[id])
						if (remoteName.equals(ignoreName))
							ignore = true;
				}
				
				if (!ignore)
					getConnection(i).relayPacket(p);
			}				
		}
	}
	
	public void sendPacketTo(String name, Packet p)
	{
		int id = getIdByName(name);
		sendPacketTo(id, p);
	}
	
	public synchronized void sendPacketTo(int id, Packet p)
	{
		if (id == -1)
			return;
		
		if (isAlive(id))
		{
			getConnection(id).sendPacket(p);
			return;
		}
		
		Logger.println("Connection not open to "+id);
	}
	
	private void printIgnores()
	{
		System.out.println("--- Ignores print start ---");
		for (int i = 0; i < _ignoreNames.length; i++)
			for (int j = 0; j < _ignoreNames[i].length; j++)
				System.out.println(i+": "+j+": "+_ignoreNames[i][j]);
	}
	
	private class Device
	{
		private String _name;
		private String _address;
		
		public Device(String name, String address)
		{
			_name = name;
			_address = address;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public String getAddress()
		{
			return _address;
		}
	}
}
