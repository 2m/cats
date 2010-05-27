package se.uu.it.cats.pc.network.packet;

import se.uu.it.cats.pc.Logger;
import se.uu.it.cats.pc.gui.Area;

import se.uu.it.cats.brick.network.packet.*;

public class PacketManager
{
	private static PacketManager _instanceHolder = new PacketManager();
	
	private static final int BUF_COUNT = 4;
	
	//private PacketBuffer[] _buffer;
	
	private PacketManager()
	{
		//_buffer = new PacketBuffer[BUF_COUNT];
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
		
		switch (packetType)
		{
			case 0x00:
			{
				if (arrayEndIndex >= Timestamp.LENGTH)
				{
					p = new Timestamp();
					p.readImpl(bArr);
				}
				break;
			}
			case 0x01:
			{
				if (arrayEndIndex >= PFMeasurement.LENGTH)
				{
					p = new PFMeasurement();
					p.readImpl(bArr);
				}
				break;
			}
			case 0x02:
			{
				if (arrayEndIndex >= SimpleMeasurement.LENGTH)
				{
					p = new SimpleMeasurement();
					p.readImpl(bArr);
					
					int catId = p.getSource();
					int sightId = ((SimpleMeasurement)p).getId();
					float angle = ((SimpleMeasurement)p).getAngle();
					float camAngle = ((SimpleMeasurement)p).getCamAngle();
					
					Area.getInstance().getCat(catId).setSighting(sightId, angle);
					Area.getInstance().getCat(catId).setAngle_cam(camAngle);
					
					//Mouse._angles[p.getSource()] = ((SimpleMeasurement)p).getAngle();
				}
				break;
			}
			case 0x03:
			{
				if (arrayEndIndex >= LatestSightingUpdate.LENGTH)
				{
					p = new LatestSightingUpdate();
					p.readImpl(bArr);
					
					int catId = p.getSource();
					float x = ((LatestSightingUpdate)p).getX(); // cat x
					float y = ((LatestSightingUpdate)p).getY(); // cat y
					//float angle_c = ((LatestSightingUpdate)p).getTheta(); // absolute angle to mouse
					//float angle_cam = 0;					
					
					Area.getInstance().getCat(catId).updateXY((int) (x*100), (int) (y*100)); //Values from network in meters, values in Area in cm.
				}
				break;
			}
			case 0x04: //MeanAndCovarianceUpdate
			{
				if (arrayEndIndex >= MeanAndCovarianceUpdate.LENGTH)
				{
					p = new MeanAndCovarianceUpdate();
					p.readImpl(bArr);
					//TODO: MARTIN help.
					float x = ((MeanAndCovarianceUpdate)p).getMeanX();
					float y = ((MeanAndCovarianceUpdate)p).getMeanY();
					
					Area.getInstance().getMouse().newPosition((int) (x*100), (int) (y*100)); //Values from network in meters, values in Area in cm.
					System.out.println("X: " + x + " Y: " + y);
				}
				break;
			}
			case 0x05:
			{
				if (arrayEndIndex >= AbsolutePositionUpdate.LENGTH)
				{
					p = new AbsolutePositionUpdate();
					p.readImpl(bArr);
					
					int catId = p.getSource();
					float x = ((AbsolutePositionUpdate)p).getX();
					float y = ((AbsolutePositionUpdate)p).getY();
					float angle_c = ((AbsolutePositionUpdate)p).getTheta();
					float angle_cam = 0;					
					
					Area.getInstance().getCat(catId).updateXYAngles((int) (x*100), (int) (y*100), angle_c, angle_cam); //Values from network in meters, values in Area in cm.
					
				}
				break;
			}
			
		}
		
		if (p != null)
		{
			//Logger.println("New packet read "+p);
		}
		
		return p;
	}
	
	/*public boolean addToBuffer(Packet p)
	{
		int bufId = p.getSource();
		
		if (_buffer[bufId] == null)
			_buffer[bufId] = new PacketBuffer();
		
		return _buffer[bufId].addPacket(p);
	}*/
}
