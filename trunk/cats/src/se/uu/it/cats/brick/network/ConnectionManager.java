package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.comm.BTConnection;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.packet.Packet;

public class ConnectionManager
{
	public static final RemoteDevice[] _devices = new RemoteDevice[] {
		new RemoteDevice("cat1", "00165302CC4E", new byte[] {0, 0, 8, 4}),
		new RemoteDevice("cat2", "0016530E6938", new byte[] {0, 0, 8, 4}),
		new RemoteDevice("cat3", "00165302CDC3", new byte[] {0, 0, 8, 4}),
		new RemoteDevice("dongle1", "0015832A3670", new byte[] {0, 0, 8, 4}),
		new RemoteDevice("dongle2", "000C783394E7", new byte[] {0, 0, 8, 4}),
		new RemoteDevice("MartinPC", "002556F9072D", new byte[] {0, 0, 8, 4})
	};
	
	public static final int MAX_OUTBOUND_CONN = _devices.length;
	public static final int INBOUND_CONN_ID = MAX_OUTBOUND_CONN;
	
	private static ConnectionManager _instanceHolder = new ConnectionManager();
	
	private ConnectionHandler[] _outboundConnectionHolder = null;
	private ConnectionHandler _inboundConnectionHolder = null;
	
	public static ConnectionManager getInstance()
	{
		return _instanceHolder;
	}
	
	private ConnectionManager()
	{
		_outboundConnectionHolder = new ConnectionHandler[MAX_OUTBOUND_CONN];
	}
	
	public void openConnection(int i)
	{
		if (isCreated(i))
		{
			Logger.println("Connection already created.");
			return;
		}
		
		ConnectionHandler ch = null;
		
		ch = new KeepAlive(_devices[i]);
		
		_outboundConnectionHolder[i] = ch;
		Thread initiatorThread = new Thread(ch);
		initiatorThread.start();
	}
	
	// incomming connection
	public void openConnection(BTConnection btc)
	{
		ConnectionListener.setListen(false);
		
		ConnectionHandler ch = new KeepAlive(btc);
		_inboundConnectionHolder = ch;
		Thread t = new Thread(ch);
		t.start();
	}
	
	public ConnectionHandler getConnection(int i)
	{
		if (i < 0)
		{
			return null;
		}
		else if (i < MAX_OUTBOUND_CONN)
		{
			return _outboundConnectionHolder[i];
		}
		else
		{
			return _inboundConnectionHolder;
		}
	}
	
	public void closeConnection(int i)
	{
		if (isCreated(i))
		{
			Logger.println("Closing connection to:"+getConnection(i).getRemoteName());
			getConnection(i).close();
		}
		
		if (i < MAX_OUTBOUND_CONN)
		{
			_outboundConnectionHolder[i] = null;			
		}
		else
		{
			_inboundConnectionHolder = null;
			ConnectionListener.setListen(true);
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
		
		if (_inboundConnectionHolder == conn)
			closeConnection(INBOUND_CONN_ID);
	}
	
	public boolean canListen()
	{
		return _inboundConnectionHolder == null;
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
	
	public void sendByteTo(int i, byte b)
	{
		if (isAlive(i))
		{
			getConnection(i).sendByte(b);
			return;
		}
		else if (isAlive(INBOUND_CONN_ID))
		{
			if (getConnection(INBOUND_CONN_ID).getRemoteId() == i || i == -1)
			{
				getConnection(INBOUND_CONN_ID).sendByte(b);
				return;
			}
		}
		
		Logger.println("Can't send byte, no open connection to: "+i);
	}
	
	public void sendByteToAll(byte b)
	{
		sendByteToAllExcept(b, null);
	}
	
	public void sendByteToAllExcept(byte b, String name)
	{
		Logger.println("Sending byte "+String.valueOf(b)+" to all exc "+name);
		for (int i = 0; i < MAX_OUTBOUND_CONN + 1; i++)
		{
			if (isAlive(i) && !getConnection(i).getRemoteName().equals(name))
				getConnection(i).sendByte(b);
		}
	}
	
	public void sendBytesToAll(byte[] bArr)
	{
		sendBytesToAllExcept(bArr, null);
	}
	
	public void sendBytesToAllExcept(byte[] bArr, String name)
	{
		//Logger.println("Sending bytes to all exc "+name);
		for (int i = 0; i < MAX_OUTBOUND_CONN + 1; i++)
		{
			if (isAlive(i) && !getConnection(i).getRemoteName().equals(name))
				getConnection(i).sendBytes(bArr);
		}
	}
	
	public void sendPacketTo(int i, Packet p)
	{
		if (isAlive(i))
		{
			getConnection(i).sendPacket(p);
			return;
		}
		else if (isAlive(INBOUND_CONN_ID))
		{
			if (getConnection(INBOUND_CONN_ID).getRemoteId() == i || i == -1)
			{
				getConnection(INBOUND_CONN_ID).sendPacket(p);
				return;
			}
		}
		
		Logger.println("Can't send packet, no open connection to: "+i);
	}
	
	public void sendPacketToAll(Packet p)
	{
		sendPacketToAllExcept(p, null);
	}
	
	public void sendPacketToAllExcept(Packet p, String name)
	{
		//Logger.println("Sending packet to all exc "+name);
		for (int i = 0; i < MAX_OUTBOUND_CONN + 1; i++)
		{
			if (isAlive(i) && !getConnection(i).getRemoteName().equals(name))
				getConnection(i).sendPacket(p);
		}
	}
	
	public RemoteDevice getDeviceByAddress(String address)
	{
		for (int i = 0; i < _devices.length; i++)
			if (_devices[i].getDeviceAddr().equals(address))
				return _devices[i];
		
		return null;
	}
	
	public int getIdByAddress(String address)
	{
		for (int i = 0; i < _devices.length; i++)
			if (_devices[i].getDeviceAddr().equals(address))
				return i;
		
		return -1;
	}
	
	public int getIdByName(String name)
	{
		for (int i = 0; i < _devices.length; i++)
			if (_devices[i].getFriendlyName(false).equals(name))
				return i;
		
		return -1;
	}
}
