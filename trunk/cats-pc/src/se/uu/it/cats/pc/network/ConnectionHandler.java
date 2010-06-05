package se.uu.it.cats.pc.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;

import lejos.pc.comm.NXTConnector;
import se.uu.it.cats.brick.network.packet.Packet;
import se.uu.it.cats.brick.network.packet.SimpleMeasurement;
import se.uu.it.cats.pc.Logger;
import se.uu.it.cats.pc.gui.PanelBluetooth;
import se.uu.it.cats.pc.network.packet.PacketManager;

public class ConnectionHandler implements Runnable
{
	DataOutputStream _dos = null;
	DataInputStream _dis = null;
	
	boolean _alive = false;
	
	String _remoteName;
	String _localName;
	
	public ConnectionHandler(String remoteName)
	{
		_remoteName = remoteName;
		
		try {
			_localName = ConnectionManager.getInstance().getNameByAddress(LocalDevice.getLocalDevice().getBluetoothAddress());
		} catch (BluetoothStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean connect()
	{
		System.out.println("Trying to connect to "+_remoteName);
		NXTConnector conn = new NXTConnector();
		boolean connected = conn.connectTo("btspp://"+_remoteName);
		
		if (!connected)
		{
			System.err.println("Failed to connect to "+_remoteName);
			return false;
		}
		
		System.out.println("Connection successful to "+_remoteName);
		
		_dos = conn.getDataOut();
		_dis = conn.getDataIn();
		
		return true;
	}
	
	protected boolean isAlive()
	{
		return _alive;
	}
	
	protected void setAlive(boolean alive)
	{
		_alive = alive;
	}
	
	public void run()
	{
		setAlive(connect());
		
		byte[] bArr = new byte[255];
		int index = 0;
		
		int[] packetCounter = new int[3];
		long startTime = System.currentTimeMillis();
		
		while (isAlive())
		{
			try
			{
				// read byte by byte
				// this ensures that we will never be blocked when half of buffer has arrived
				bArr[index] = _dis.readByte();
				int received = 1;				
				
				/*byte[] receivedBytes = new byte[received];
				System.arraycopy(bArr, index, receivedBytes, 0, received);
				_dos.write(receivedBytes);
				_dos.flush();*/
				
				index = index + received;
				
				/*Logger.print("Rcvd:"+received+" input buffer:");
				for (int i = 0; i < index; i++)
					Logger.print(bArr[i]+", ");
				Logger.println("of length"+index);*/
				
				Packet p = PacketManager.getInstance().checkForCompletePackets(bArr, index);
				
				while (p != null)
				{
					int bytesRead = p.getLength();
					
					System.arraycopy(bArr, bytesRead, bArr, 0, index - bytesRead);
					index -= bytesRead;
					packetCounter[p.getSource()]++;
					
					// add info to log panel
					PanelBluetooth.updatePacket(p.toString());
					
					// forward packet to other cats, some packets do not need to be forwarded
					if (!(p instanceof SimpleMeasurement))
					{
						//relayPacket(p);
						// relay packet to all except the cat that the packet came from
						ConnectionManager.getInstance().relayPacketFrom(p, getRemoteName());
					}
					
					p = PacketManager.getInstance().checkForCompletePackets(bArr, index);
				}
				
				if (index > 255)
					Logger.println("Received data buffer is full.");
			}
			catch (IOException ioe)
			{
				Logger.println("IO Exception reading bytes. Closing connection to "+getRemoteName());				
				setAlive(false);
			}
			catch (IndexOutOfBoundsException ex)
			{
				Logger.println("IndexOutOfBoundsException Exception reading bytes. Closing connection to "+getRemoteName());
				setAlive(false);
			}
			
			if (System.currentTimeMillis() - startTime > 3000)
			{
				int packetSum = 0;
				for (int i = 0; i < packetCounter.length; i++)
				{
					packetSum += packetCounter[i];
				}
				
				float currentBw = (float)packetSum / 3;
				Logger.print("BW from "+getRemoteName()+":"+currentBw+"Pck/s");					
				for (int i = 0; i < packetCounter.length; i++)
				{
					Logger.print(" "+(float)packetCounter[i] / 3);
				}
				
				Logger.println("");
				
				for (int i = 0; i < packetCounter.length; i++)
				{
					packetCounter[i] = 0;
				}
				
				startTime = System.currentTimeMillis();
			}
			
			try { Thread.sleep(1); } catch (Exception ex) {}
		}
		
		ConnectionManager.getInstance().closeConnection(this);
	}
	
	protected String getRemoteName()
	{
		return _remoteName;
	}
	
	protected int getLocalId()
	{
		return ConnectionManager.getInstance().getIdByName(_localName);
	}
	
	protected void close()
	{
		_alive = false;
	}
	
	public void relayPacket(Packet p)
	{
		try
		{
			_dos.write(p.writeImpl());
			_dos.flush();
		}
		catch (IOException ioe)
		{
			Logger.println("IO Exception writing bytes to "+getRemoteName());				
			setAlive(false);
		}
	}
	
	public void sendPacket(Packet p)
	{
		try
		{
			p.setSource(getLocalId());
			
			_dos.write(p.writeImpl());
			_dos.flush();
		}
		catch (IOException ioe)
		{
			Logger.println("IO Exception writing bytes to "+getRemoteName());				
			setAlive(false);
		}
	}
}
