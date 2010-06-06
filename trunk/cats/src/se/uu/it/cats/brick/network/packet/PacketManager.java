package se.uu.it.cats.brick.network.packet;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.Identity;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.Music;
import se.uu.it.cats.brick.Settings;
import se.uu.it.cats.brick.network.KeepAlive;
import se.uu.it.cats.brick.storage.BillBoard;

public class PacketManager
{
	private static PacketManager _instanceHolder = new PacketManager();
	
	private static final int BUF_COUNT = 4;
	
	private PacketBuffer[] _buffer;
	
	private PacketManager()
	{
		_buffer = new PacketBuffer[BUF_COUNT];
	}
	
	public static PacketManager getInstance()
	{
		return _instanceHolder;
	}
	
	public Packet checkForCompletePackets(byte[] bArr, int arrayEndIndex)
	{
		Packet p = null;
		
		if (arrayEndIndex == 0)
			// empty array, return
			return null;
		
		// first byte should be packet type
		byte packetType = bArr[0];
		//Logger.println("Checking for packets of type:"+packetType);
		
		// determine which packet do we have by type,
		// an create object from bytes
		switch (packetType)
		{
			case 0x00:
			{
				if (arrayEndIndex >= Timestamp.LENGTH)
				{
					p = new Timestamp();
					p.readImpl(bArr);
					
					Clock.incommingPacket((Timestamp)p);
					//addToBuffer(p);
				}
				break;
			}
			case 0x01:
			{
				if (arrayEndIndex >= SettingUpdate.LENGTH)
				{
					p = new SettingUpdate();
					p.readImpl(bArr);
					
					int setting = ((SettingUpdate)p).getSetting();
					int value = ((SettingUpdate)p).getValue();
					
					switch (setting)
					{
						case SettingUpdate.USE_GUIDE:
						{
							Settings.USE_GUIDE = (value == 1 ? true : false);
							break;
						}
					}
				}
				break;
			}
			case 0x02:
			{
				if (arrayEndIndex >= SimpleMeasurement.LENGTH)
				{
					p = new SimpleMeasurement();
					p.readImpl(bArr);
					
					//addToBuffer(p);
				}
				break;
			}
			case 0x03:
			{
				if (arrayEndIndex >= LatestSightingUpdate.LENGTH)
				{
					p = new LatestSightingUpdate();
					p.readImpl(bArr);
					
					BillBoard.getInstance().setLatestSighting((LatestSightingUpdate)p);
				}
				break;
			}
			case 0x04:
			{
				if (arrayEndIndex >= MeanAndCovarianceUpdate.LENGTH)
				{
					p = new MeanAndCovarianceUpdate();
					p.readImpl(bArr);
					
					BillBoard.getInstance().setMeanAndCovariance((MeanAndCovarianceUpdate)p);
				}
				break;
			}
			case 0x05:
			{
				if (arrayEndIndex >= AbsolutePositionUpdate.LENGTH)
				{
					p = new AbsolutePositionUpdate();
					p.readImpl(bArr);
					
					BillBoard.getInstance().setAbsolutePosition((AbsolutePositionUpdate)p);
				}
				break;
			}
			case 0x06:
			{
				if (arrayEndIndex >= SyncTimeOrder.LENGTH)
				{
					p = new SyncTimeOrder();
					p.readImpl(bArr);
					
					Clock.syncTime();
				}
				break;
			}
			case 0x07:
			{
				if (arrayEndIndex >= StartOrder.LENGTH)
				{
					p = new StartOrder();
					p.readImpl(bArr);
					
					Clock.syncDone();
				}
				break;
			}
			case 0x08:
			{
				if (arrayEndIndex >= PlayMusicOrder.LENGTH)
				{
					p = new PlayMusicOrder();
					p.readImpl(bArr);
					
					Clock.setBeep(false);
					Music m = new Music(Identity.getId(), 3);
					m.play();
					Clock.setBeep(true);
				}
				break;
			}
			case 0x09:
			{
				if (arrayEndIndex >= MoveOrder.LENGTH)
				{
					p = new MoveOrder();
					p.readImpl(bArr);
					
					float x = ((MoveOrder)p).getX();
					float y = ((MoveOrder)p).getY();
					
					Settings.GUI_ORDER_X = x;
					Settings.GUI_ORDER_Y = y;
					Settings.GUI_ORDER_PROCESSED = false;
				}
				break;
			}
			case -1:
			{
				if (arrayEndIndex >= CloseConnection.LENGTH)
				{
					p = new CloseConnection();
					p.readImpl(bArr);
					
					//addToBuffer(p);
				}
				break;
			}
		}
		
		if (p != null)
		{
			//Logger.println("New packet read "+p);
		}
		else
		{
			Logger.println("No code to handle packet of type: "+packetType);
		}
		
		return p;
	}
	
	public boolean addToBuffer(Packet p)
	{
		int bufId = p.getSource();
		
		if (_buffer[bufId] == null)
			_buffer[bufId] = new PacketBuffer();
		
		return _buffer[bufId].addPacket(p);
	}
}
