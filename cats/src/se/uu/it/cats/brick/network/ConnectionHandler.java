package se.uu.it.cats.brick.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.bluetooth.RemoteDevice;

import se.uu.it.cats.brick.Logger;

import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTCommDevice;
import lejos.util.Stopwatch;

public class ConnectionHandler implements Runnable
{
	public static final RemoteDevice CAT1 = new RemoteDevice("cat1", "00165302CC4E", new byte[] {0, 0, 8, 4});
	public static final RemoteDevice CAT2 = new RemoteDevice("cat2", "0016530E6938", new byte[] {0, 0, 8, 4});
	public static final RemoteDevice CAT3 = new RemoteDevice("cat3", "00165302CDC3", new byte[] {0, 0, 8, 4});
		
	protected BTConnection _btc = null;
	
	protected DataInputStream _dis = null;
	protected DataOutputStream _dos = null;
	
	protected RemoteDevice _peerDevice = null;
	
	protected Stopwatch sw = null;
	
	public ConnectionHandler(RemoteDevice device)
	{
		_peerDevice = device;
	}
	
	public ConnectionHandler(BTConnection btc)
	{
		_btc = btc;
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
		
		_dis = _btc.openDataInputStream();
		_dos = _btc.openDataOutputStream();
		
		return true;
	}

	@Override
	public void run()
	{
		//_btc.close();
	}
}
