package se.uu.it.cats.pc.gui;

class Mouse {
	private int x; // Absolute position horizontal axis
	private int y; // Absolute position vertical axis
	
	public Mouse() {
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void newPosition(int newX, int newY) {
		x=newX;
		y=newY;
	}

}