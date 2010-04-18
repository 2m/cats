package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;


import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.packet.Packet;

import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.util.Stopwatch;
import se.uu.it.cats.brick.Logger;

/**
 * @author Martynas
 *
 */
public abstract class ConnectionHandler implements Runnable
{
	protected BTConnection _btc = null;	
	protected RemoteDevice _remoteDevice = null;
	protected String _localName = Bluetooth.getFriendlyName();
	
	protected boolean _alive = false;
	
	protected Stopwatch sw = null;
	
	/**
	 * @param device
	 */
	public ConnectionHandler(RemoteDevice device)
	{
		_remoteDevice = device;
	}
	
	/**
	 * @param btc
	 */
	public ConnectionHandler(BTConnection btc)
	{
		_btc = btc;
		_remoteDevice = ConnectionManager.getInstance().getDeviceByAddress(_btc.getAddress());
	}
	
	/**
	 * @return
	 */
	protected boolean connect()
	{
		sw = new Stopwatch();
		sw.reset();
		
		if (_btc == null)
		{
			_btc = Bluetooth.connect(_remoteDevice);
			
		    if (_btc == null)
		    {
		    	Logger.println("Connect fail to: "+getRemoteName());
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
	
	protected String getRemoteName()
	{
		return _remoteDevice.getFriendlyName(false);
	}
	
	protected int getLocalId()
	{
		return ConnectionManager.getInstance().getIdByName(_localName);
	}
	
	protected int getRemoteId()
	{
		return ConnectionManager.getInstance().getIdByAddress(_btc.getAddress());
	}	

	@Override
	public abstract void run();
	
	public abstract void sendByte(byte b);
	public abstract void sendBytes(byte[] bArr);
	public abstract void sendPacket(Packet p);
}
