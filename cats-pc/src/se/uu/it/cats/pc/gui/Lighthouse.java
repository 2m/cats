package se.uu.it.cats.pc.gui;

class Lighthouse {
	private int x; // Absolute position horizontal axis
	private int y; // Absolute position vertical axis
	private String lighthouseName;
	
	public Lighthouse(String name) {
		lighthouseName = name;
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
	
	public String getLighthouseName() {
		return lighthouseName;
	}
}