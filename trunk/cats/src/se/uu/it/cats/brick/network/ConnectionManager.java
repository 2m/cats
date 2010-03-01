package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import se.uu.it.cats.brick.Logger;

import lejos.nxt.comm.BTConnection;

public class ConnectionManager
{
	public static final RemoteDevice CAT1 = new RemoteDevice("cat1", "00165302CC4E", new byte[] {0, 0, 8, 4});
	public static final RemoteDevice CAT2 = new RemoteDevice("cat2", "0016530E6938", new byte[] {0, 0, 8, 4});
	public static final RemoteDevice CAT3 = new RemoteDevice("cat3", "00165302CDC3", new byte[] {0, 0, 8, 4});
	
	private static ConnectionManager _instanceHolder = null;
	private ConnectionHandler[] _outboundConnectionHolder = null;
	private ConnectionHandler _inboundConnectionHolder = null;
	
	public static ConnectionManager getInstance()
	{
		if (_instanceHolder == null)
		{
			_instanceHolder = new ConnectionManager();
		}
		
		return _instanceHolder;
	}
	
	public ConnectionManager()
	{
		_outboundConnectionHolder = new ConnectionHandler[3];
	}
	
	public void openConnection(int i)
	{
		if (isCreated(i))
		{
			Logger.println("Connection already created.");
			return;
		}
		
		switch (i)
		{
			case 1:
			{
				ConnectionHandler ch = new KeepAlive(CAT1);
				_outboundConnectionHolder[0] = ch;
				Thread initiatorThread = new Thread(ch);
				initiatorThread.start();
				break;
			}
			case 2:
			{
				ConnectionHandler ch = new KeepAlive(CAT2);
				_outboundConnectionHolder[1] = ch;
				Thread initiatorThread = new Thread(ch);
				initiatorThread.start();
				break;
			}
			case 3:
			{
				ConnectionHandler ch = new KeepAlive(CAT3);
				_outboundConnectionHolder[2] = ch;
				Thread initiatorThread = new Thread(ch);
				initiatorThread.start();
				break;
			}
		}
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
		if (i < 4)
		{
			return _outboundConnectionHolder[i-1];
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
		
		if (i < 4)
		{
			_outboundConnectionHolder[i-1] = null;			
		}
		else
		{
			_inboundConnectionHolder = null;
		}
	}
	
	public void closeConnection(ConnectionHandler conn)
	{
		for (int i = 0; i < _outboundConnectionHolder.length; i++)
			if (_outboundConnectionHolder[i] == conn)
			{
				closeConnection(i+1);
				return;
			}
		
		if (_inboundConnectionHolder == conn)
			closeConnection(4);
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
	
	public void sendByteTo(int i)
	{
		getConnection(i).sendByte();
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
