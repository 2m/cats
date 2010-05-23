package se.uu.it.cats.pc.gui;

public class Cat {
	private int x; // Absolute position horizontal axis
	private int y; // Absolute position vertical axis
	private float angle_c;
	private float angle_cam;
	private float cam_angle_width = 30;
	private String catName;
	private boolean manualOrder = false;
	private int goToX; //Orders for new X-position
	private int goToY; //Orders for new X-position
	private boolean marked = false;
	private int bufferLength = 50;
	private int[] bufferX = new int[bufferLength];
	private int[] bufferY = new int[bufferLength];
	private int posBuffer = 0;
	
	// angles for every sightings in angles
	// id - 0 is angle to mouse relative to cat
	// id - 1 is angle to lighthouse1 relative to cat
	// and so on...
	// first index in the array holds the actuall angle,
	// second - the number of times it has been drawn
	private float[][] sightings = new float[Area.LIGHTHOUSE_COUNT + 1][2];
	
	public Cat(String name) {
		catName = name;
	}
	
	public String getName() {
		return catName;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void goTo(int newOrderX, int newOrderY) {
		goToX = newOrderX;
		goToY = newOrderY;
	}
	public int getGoToX() {
		return goToX;
	}
	public int getGoToY() {
		return goToY;
	}
	public int[] getBufferX() {
		return bufferX;
	}
	public int[] getBufferY() {
		return bufferY;
	}
	
	public int getPosBuffer() {
		return posBuffer;
	}
	public int getBufferLength() {
		return bufferLength;
	}
	
	public boolean isManualOrder() {
		return manualOrder;
	}
	public void setManualOrder(boolean manual) {
		manualOrder = manual;
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
	
	public void updateXYAngles(int newX, int newY, float newAngle_c, float newAngle_cam) {
		x = newX;
		y = newY;
		angle_c = newAngle_c;
		angle_cam = newAngle_cam;
		//Position buffer
		if(posBuffer == bufferLength-1) 
			posBuffer = 0;
		bufferX[posBuffer] = x;
		bufferY[posBuffer] = y;
		posBuffer++;
		
		//Remove order if closer than 5x5 cm square
		if (Math.abs(x-goToX) < 5 && Math.abs(y-goToY) < 5) {
			manualOrder = false;
		}
	}
	
	
	public void updateAngleCam(float newAngle_cam) {
		angle_cam = newAngle_cam;
	}
	
	public void newPosition(int newX, int newY) {
		x=newX;
		y=newY;
	}
	
	public float getCam_angle_width() {
		return cam_angle_width;
	}
	
	public void setSighting(int sightingId, float angle) {
		sightings[sightingId][0] = angle;
		sightings[sightingId][1] = 20;
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