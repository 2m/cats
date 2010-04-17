package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.packet.Packet;

import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.util.Stopwatch;

public abstract class ConnectionHandler implements Runnable
{
	protected BTConnection _btc = null;	
	protected RemoteDevice _peerDevice = null;
	protected String _localName = Bluetooth.getFriendlyName();
	
	protected boolean _alive = false;
	
	protected Stopwatch sw = null;
	
	public ConnectionHandler(RemoteDevice device)
	{
		_peerDevice = device;
	}
	
	public ConnectionHandler(BTConnection btc)
	{
		_btc = btc;
		_peerDevice = ConnectionManager.getInstance().getDeviceByAddress(_btc.getAddress());
	}
	
	protected boolean connect()
	{
		sw = new Stopwatch();
		sw.reset();
		
		if (_btc == null)
		{
			_btc = Bluetooth.connect(_peerDevice);
			
		    if (_btc == null)
		    {
		    	Logger.println("Connect fail to: "+_peerDevice.getFriendlyName(false));
		    	return false;
		    }
		}
		
		return true;
	}
	
	protected void close()
	{
		if (_btc != null)
			_btc.close();
		_alive = false;
	}
	
	protected boolean isAlive()
	{
		return _alive;
	}
	
	protected void setAlive(boolean alive)
	{
		_alive = alive;
	}
	
	protected String getPeerName()
	{
		return _peerDevice.getFriendlyName(false);
	}
	
	protected int getPeerId()
	{
		return ConnectionManager.getInstance().getIdByAddress(_btc.getAddress());
	}
	
	protected int getLocalId()
	{
		return ConnectionManager.getInstance().getIdByName(_localName);
	}

	@Override
	public abstract void run();
	
	public abstract void sendByte(byte b);
	public abstract void sendPacket(Packet p);
}
