package se.uu.it.cats.pc.gui;

public class Cat {
	private int x; // Absolute position horizontal axis
	private int y; // Absolute position vertical axis
	private float angle_c;
	private float angle_cam;
	private float cam_angle_width = 30;
	private String catName;
	
	public Cat(String name) {
		catName = name;
	}
	
	public String getCatName() {
		return catName;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public float getAngle_c() {
		return angle_c;
	}
	public float getAngle_cam() {
		return angle_cam;
	}
	
	public void updateXYAngles(int newX, int newY, float newAngle_c, float newAngle_cam) {
		x = newX;
		y = newY;
		angle_c = newAngle_c;
		angle_cam = newAngle_cam;
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
}