package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import se.uu.it.cats.brick.Logger;

import lejos.nxt.comm.BTConnection;

public class ConnectionManager
{
	public static final RemoteDevice CAT1 = new RemoteDevice("cat1", "00165302CC4E", new byte[] {0, 0, 8, 4});
	public static final RemoteDevice CAT2 = new RemoteDevice("cat2", "0016530E6938", new byte[] {0, 0, 8, 4});
	public static final RemoteDevice CAT3 = new RemoteDevice("cat3", "00165302CDC3", new byte[] {0, 0, 8, 4});
	
	public static final int MAX_OUTBOUND_CONN = 3;
	
	private static ConnectionManager _instanceHolder = new ConnectionManager();
	private ConnectionHandler[] _outboundConnectionHolder = null;
	private ConnectionHandler _inboundConnectionHolder = null;
	
	public static ConnectionManager getInstance()
	{
		return _instanceHolder;
	}
	
	public ConnectionManager()
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
		
		switch (i)
		{
			case 0:
			{
				ch = new KeepAlive(CAT1);				
				break;
			}
			case 1:
			{
				ch = new KeepAlive(CAT2);
				break;
			}
			case 2:
			{
				ch = new KeepAlive(CAT3);
				break;
			}
		}
		
		_outboundConnectionHolder[i] = ch;
		Thread initiatorThread = new Thread(ch);
		initiatorThread.start();
	}
	
	public void openConnection(BTConnection btc)
	{
		ConnectionHandler ch = new KeepAlive(btc);
		_inboundConnectionHolder = ch;
		Thread t = new Thread(ch);
		t.start();
	}
	
	public ConnectionHandler getConnection(int i)
	{
		if (i < MAX_OUTBOUND_CONN)
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
		Logger.println("Closing connection to:"+getConnection(i).getPeerName());
		getConnection(i).close();
		
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
			closeConnection(MAX_OUTBOUND_CONN);
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
		return isCreated(i) && getConnection(i).isAlive();
	}
	
	public void sendByteTo(int i, byte b)
	{
		getConnection(i).sendByte(b);
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
			if (isAlive(i) && !getConnection(i).getPeerName().equals(name))
				sendByteTo(i, b);
		}
	}
	
	public RemoteDevice getDeviceByAddress(String address)
	{
		if (CAT1.getDeviceAddr().equals(address))
			return CAT1;
		else if (CAT2.getDeviceAddr().equals(address))
			return CAT2;
		else if (CAT3.getDeviceAddr().equals(address))
			return CAT3;
		
		return null;
	}
}
