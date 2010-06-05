package se.uu.it.cats.pc.gui;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.network.ConnectionManager;
import se.uu.it.cats.brick.network.packet.LatestSightingUpdate;

// only for visual purposes
public class BillBoard
{
	private static BillBoard instanceHolder = new BillBoard(); 

	public static BillBoard getInstance()
	{
		return instanceHolder;
	}
	
	public float[] sightings = new float[Area.CAT_COUNT * 4];

	private BillBoard() {
		
	}
	
	public void setLatestSighting(int id, float x, float y, float theta, int timestamp) {
		sightings[id * 4 + 0] = x;
		sightings[id * 4 + 1] = y;
		sightings[id * 4 + 2] = theta;
		sightings[id * 4 + 3] = timestamp;
	}
	
	public float getLatestSightingX(int id) {
		return sightings[id * 4 + 0];
	}
	
	public float getLatestSightingY(int id) {
		return sightings[id * 4 + 1];
	}
	
	public float getLatestSightingTheta(int id) {
		return sightings[id * 4 + 2];
	}
	
	public float getLatestSightingTimestamp(int id) {
		return sightings[id * 4 + 3];
	}
}
