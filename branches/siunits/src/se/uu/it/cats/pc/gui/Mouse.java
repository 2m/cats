package se.uu.it.cats.pc.gui;

public class Mouse {
	private int _x; // Absolute position horizontal axis
	private int _y; // Absolute position vertical axis
	
	public Mouse() {
	}
	public void newPosition(int x, int y) {
		_x = x;
		_y = y;
	}
	public int getX() {
		return _x;
	}
	
	public int getY() {
		return _y;
	}
}