package se.uu.it.cats.pc.gui;

// only for visual purposes
public class BillBoard
{
	private static int SIGHT_COM_COUNT = 4;
	private static int POS_COM_COUNT = 4;
	private static int DATA_COMP_COUNT = 12;
	
	private static BillBoard instanceHolder = new BillBoard(); 

	public static BillBoard getInstance()
	{
		return instanceHolder;
	}
	
	public float[] sightings = new float[Area.CAT_COUNT * SIGHT_COM_COUNT];
	public float[] positions = new float[Area.CAT_COUNT * POS_COM_COUNT];
	public float[] data = new float[Area.CAT_COUNT * DATA_COMP_COUNT];

	private BillBoard() {
		
	}
	
	public void setLatestSighting(int id, float x, float y, float theta, int timestamp) {
		sightings[id * SIGHT_COM_COUNT + 0] = x;
		sightings[id * SIGHT_COM_COUNT + 1] = y;
		sightings[id * SIGHT_COM_COUNT + 2] = theta;
		sightings[id * SIGHT_COM_COUNT + 3] = timestamp;
	}
	
	public float getLatestSightingX(int id) {
		return sightings[id * SIGHT_COM_COUNT + 0];
	}
	
	public float getLatestSightingY(int id) {
		return sightings[id * SIGHT_COM_COUNT + 1];
	}
	
	public float getLatestSightingTheta(int id) {
		return sightings[id * SIGHT_COM_COUNT + 2];
	}
	
	public float getLatestSightingTimestamp(int id) {
		return sightings[id * SIGHT_COM_COUNT + 3];
	}
	
	public void setAbsolutePosition(int id, float x, float y, float theta, int timestamp) {
		positions[id * POS_COM_COUNT + 0] = x;
		positions[id * POS_COM_COUNT + 1] = y;
		positions[id * POS_COM_COUNT + 2] = theta;
		positions[id * POS_COM_COUNT + 3] = timestamp;
	}
	
	public float getAbsolutePositionX(int id) {
		return positions[id * POS_COM_COUNT + 0];
	}
	
	public float getAbsolutePositionY(int id) {
		return positions[id * POS_COM_COUNT + 1];
	}
	
	public float getAbsolutePositionTheta(int id) {
		return positions[id * POS_COM_COUNT + 2];
	}
	
	public float getAbsolutePositionTimestamp(int id) {
		return positions[id * POS_COM_COUNT + 3];
	}
	
	public void setMeanAndCovariance(int id, float mean_x, float mean_y,
			float mean_xv, float mean_yv, float var_xx, float var_xy,
			float var_yy, float var_xvxv, float var_xvyv, float var_yvyv,
			float weight, float timestamp)
	{
		data[id * DATA_COMP_COUNT + 0] = mean_x;
		data[id * DATA_COMP_COUNT + 1] = mean_y;
		data[id * DATA_COMP_COUNT + 2] = mean_xv;
		data[id * DATA_COMP_COUNT + 3] = mean_yv;
		data[id * DATA_COMP_COUNT + 4] = var_xx;
		data[id * DATA_COMP_COUNT + 5] = var_xy;
		data[id * DATA_COMP_COUNT + 6] = var_yy;
		data[id * DATA_COMP_COUNT + 7] = var_xvxv;
		data[id * DATA_COMP_COUNT + 8] = var_xvyv;
		data[id * DATA_COMP_COUNT + 9] = var_yvyv;
		data[id * DATA_COMP_COUNT + 10] = weight;
		data[id * DATA_COMP_COUNT + 11] = timestamp;
	}
	
	public float getMeanX(int id) {
		return data[id * DATA_COMP_COUNT + 0];
	}
	
	public float getMeanY(int id) {
		return data[id * DATA_COMP_COUNT + 1];
	}
	
	public float getMeanXv(int id) {
		return data[id * DATA_COMP_COUNT + 2];
	}
	
	public float getMeanYv(int id) {
		return data[id * DATA_COMP_COUNT + 3];
	}
	
	public float getVarXX(int id) {
		return data[id * DATA_COMP_COUNT + 4];
	}
	
	public float getVarXY(int id) {
		return data[id * DATA_COMP_COUNT + 5];
	}
	
	public float getVarYY(int id) {
		return data[id * DATA_COMP_COUNT + 6];
	}
	
	public float getVarXvXv(int id) {
		return data[id * DATA_COMP_COUNT + 7];
	}
	
	public float getVarXvYv(int id) {
		return data[id * DATA_COMP_COUNT + 8];
	}
	
	public float getVarYvYv(int id) {
		return data[id * DATA_COMP_COUNT + 9];
	}
	
	public float getWeight(int id) {
		return data[id * DATA_COMP_COUNT + 10];
	}
	
	public float getDataTimestamp(int id) {
		return data[id * DATA_COMP_COUNT + 11];
	}
}
