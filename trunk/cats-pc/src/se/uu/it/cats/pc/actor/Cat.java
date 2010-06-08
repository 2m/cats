package se.uu.it.cats.pc.actor;

public class Cat {
	
	public static final int ORDER_NONE = 0;
	public static final int ORDER_GUI = 1;
	public static final int ORDER_GUIDE = 2;
	
	private float x; // Absolute position horizontal axis
	private float y; // Absolute position vertical axis
	private float angle_c = 0;
	private float angle_cam = 0;
	private float cam_angle_width = 30;
	private String catName;
	
	private int manualOrder = ORDER_NONE;
	private float goToX; //Orders for new X-position
	private float goToY; //Orders for new X-position
	
	private boolean marked = false;
	
	private int bufferLength = 50;
	private float[] bufferX = new float[bufferLength];
	private float[] bufferY = new float[bufferLength];
	private int posBuffer = 0;
	
	// angles for every sightings in angles
	// id - 0 is angle to mouse relative to cat
	// id - 1 is angle to lighthouse1 relative to cat
	// and so on...
	// first index in the array holds the actuall angle,
	// second - the number of times it has been drawn
	private float[][] sightings = new float[Area.LIGHTHOUSE_COUNT + 1][2];
	
	public Cat(String name, float x, float y) {
		catName = name;
		this.x = x;
		this.y = y;
	}
	
	public String getName() {
		return catName;
	}
	
	public int getX() {
		// convert meters to centimeters for the GUI
		return (int)(x*100);
	}
	
	public int getY() {
		// convert meters to centimeters for the GUI
		return (int)(y*100);
	}
	
	public float getFloatX() {
		return x;
	}
	
	public float getFloatY() {
		return y;
	}
	
	public void goTo(float newOrderX, float newOrderY) {
		goToX = newOrderX;
		goToY = newOrderY;
	}
	public float getGoToX() {
		return (int)(goToX*100);
	}
	public float getGoToY() {
		return (int)(goToY*100);
	}
	public float[] getBufferX() {
		return bufferX;
	}
	public float[] getBufferY() {
		return bufferY;
	}
	
	public int getPosBuffer() {
		return posBuffer;
	}
	public int getBufferLength() {
		return bufferLength;
	}
	
	public boolean isManualOrder() {
		return manualOrder != ORDER_NONE;
	}
	public void setManualOrder(int order) {
		manualOrder = order;
	}
	public int getManualOrder() {
		return manualOrder;
	}
	public boolean isMarked() {
		return marked;
	}
	public void setMarked(boolean mark) {
		marked = mark;
	}
	
	public float getAngle_c() {
		return angle_c;
	}
	
	public float getAngle_cam() {
		return angle_cam;
	}
	
	public void setAngle_cam(float angle) {
		angle_cam = angle;
	}
	
	public void updateXYAngles(float newX, float newY, float newAngle_c, float newAngle_cam) {
		
		angle_c = newAngle_c;
		angle_cam = newAngle_cam;	
		
		updateXY(newX, newY);
	}
	
	public void updateXY(float newX, float newY) {
		x = newX;
		y = newY;
		
		//Position buffer
		if(posBuffer == bufferLength-1) 
			posBuffer = 0;
		bufferX[posBuffer] = x;
		bufferY[posBuffer] = y;
		posBuffer++;
		
		//Remove order if closer than 5x5 cm square
		if (Math.abs(x-goToX) < 0.01 && Math.abs(y-goToY) < 0.01) {
			manualOrder = ORDER_NONE;
		}
	}
	
	
	public void updateAngleCam(float newAngle_cam) {
		angle_cam = newAngle_cam;
	}	
	
	public float getCam_angle_width() {
		return cam_angle_width;
	}
	
	public void setAbsSighting(int sightingId, float angle) {
		sightings[sightingId][0] = angle - angle_c;
		sightings[sightingId][1] = 50;
	}
	
	public void setRelSighting(int sightingId, float angle) {
		sightings[sightingId][0] = angle;
		sightings[sightingId][1] = 50;
	}
	
	public float getSighting(int sightingId) {
		return sightings[sightingId][0];
	}
	
	public float getSightingCount() {
		return sightings.length;
	}
	
	public float getSightingDrawCount(int sightingId) {
		return sightings[sightingId][1];
	}
	
	public void decreaseSightingDrawnCount(int sightingId) {
		sightings[sightingId][1]--;
	}
}