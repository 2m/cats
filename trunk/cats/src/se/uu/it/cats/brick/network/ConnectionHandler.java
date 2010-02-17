package se.uu.it.cats.brick.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.bluetooth.RemoteDevice;

import se.uu.it.cats.brick.Logger;

import lejos.nxt.Button;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

public class ConnectionHandler implements Runnable
{
	protected BTConnection _btc = null;
	
	protected DataInputStream _dis = null;
	protected DataOutputStream _dos = null;
	
	protected String _peerName = null;
	
	public ConnectionHandler(String name)
	{
		_peerName = name;
	}
	
	public ConnectionHandler(BTConnection btc)
	{
		_btc = btc;
	}
	
	protected void connect()
	{
		if (_btc == null)
		{
			RemoteDevice btrd = Bluetooth.getKnownDevice(_peerName);
			if (btrd == null)
			{
				Logger.println("No such device: "+_peerName);
			    Button.waitForPress();
			    System.exit(1);
			}
			
			_btc = Bluetooth.connect(btrd);

		    if (_btc == null)
		    {
		    	Logger.println("Connect fail to: "+_peerName);
		    	Button.waitForPress();
		    	System.exit(1);
		    }
		}
		
		_dis = _btc.openDataInputStream();
		_dos = _btc.openDataOutputStream();
	}

	@Override
	public void run()
	{
		_btc.close();
	}
}
