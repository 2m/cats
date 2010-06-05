package se.uu.it.cats.pc.gui;

public class Mouse {
	private float _x; // Absolute position horizontal axis
	private float _y; // Absolute position vertical axis
	
	public Mouse() {
	}
	public void newPosition(float x, float y) {
		_x = x;
		_y = y;
	}
	public int getX() {
		// convert meters to centimeters for the GUI
		return (int)(_x*100);
	}
	
	public int getY() {
		// convert meters to centimeters for the GUI
		return (int)(_y*100);
	}
	
	public float getFloatX() {
		return _x;
	}
	
	public float getFloatY() {
		return _y;
	}
}