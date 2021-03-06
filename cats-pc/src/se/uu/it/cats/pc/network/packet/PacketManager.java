package se.uu.it.cats.pc.network.packet;

import se.uu.it.cats.pc.Logger;
import se.uu.it.cats.pc.actor.Area;
import se.uu.it.cats.pc.actor.BillBoard;
import se.uu.it.cats.pc.actor.Cat;
import se.uu.it.cats.pc.gui.GuidePanel;

import se.uu.it.cats.brick.Settings;
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
				if (arrayEndIndex >= SettingUpdate.LENGTH)
				{
					p = new SettingUpdate();
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
					float camAngle = ((SimpleMeasurement)p).getCamAngle(); // is always zero
					
					Area.getInstance().getCat(catId).setRelSighting(sightId, angle);
					//Area.getInstance().getCat(catId).setAngle_cam(camAngle);
					
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
					float angle_c = ((LatestSightingUpdate)p).getTheta(); // absolute angle to mouse
					//float angle_cam = 0;
					
					// set sighting to cat, this function accepts
					Area.getInstance().getCat(catId).setAbsSighting(0, angle_c);					
					Area.getInstance().getCat(catId).updateXY(x, y);
					
					BillBoard.getInstance().setLatestSighting(catId, x, y, angle_c, ((LatestSightingUpdate)p).getTimestamp());
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
					
					Area.getInstance().getMouse().newPosition(x, y);
					//System.out.println("from cat"+p.getSource()+"X: " + x + " Y: " + y);
					
					MeanAndCovarianceUpdate pck = (MeanAndCovarianceUpdate)p;				
					BillBoard.getInstance().setMeanAndCovariance(
							pck.getSource(),
							pck.getMeanX(),
							pck.getMeanY(),
							pck.getMeanXv(),
							pck.getMeanYv(),
							pck.getVarXX(),
							pck.getVarXY(),
							pck.getVarYY(),
							pck.getVarXvXv(),
							pck.getVarXvYv(),
							pck.getVarYvYv(),
							pck.getWeight(),
							pck.getTimestamp()
					);
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
					float angle_cam = 0;	//TODO: Real camera angles - can they be acquired?				
					
					Area.getInstance().getCat(catId).updateXYAngles(x, y, angle_c, angle_cam); //Values from network in meters, values in Area in cm.
					
					BillBoard.getInstance().setAbsolutePosition(catId, x, y, angle_c, ((AbsolutePositionUpdate)p).getTimestamp());
				}
				break;
			}
			case 0x09:
			{
				if (arrayEndIndex >= MoveOrder.LENGTH)
				{
					p = new MoveOrder();
					p.readImpl(bArr);
					
					int catId = p.getSource();
					float x = ((MoveOrder)p).getX();
					float y = ((MoveOrder)p).getY();
					
					Area.getInstance().getCat(catId).goTo(x, y);
					Area.getInstance().getCat(catId).setManualOrder(Cat.ORDER_GUIDE);
					
					GuidePanel.updateOrder(catId, "Order x:"+x+" y:"+y);
				}
				break;
			}
			default:
			{
				Logger.println("No code to handle packet of type "+packetType);
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
