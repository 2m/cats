package se.uu.it.cats.brick.filter;

public class MovementData extends ComparableData {
	public float dr; // Distance travelled
	public float dangle; // Radians turned
	public double vx;  //speed in m/s
	public double vy;

	public MovementData(int timestamp, float dr, float dangle) {
		super(timestamp);
		this.dr = dr;
		this.dangle = dangle;
	}

	public MovementData(int timestamp, double xv, double yv) {
		super(timestamp);
		this.vx = xv;
		this.vy = yv;
	}

	public boolean isMovementData() {
		return true;
	}

	public String toString() {
		return "[" + comparable + ",dr= " + dr + ",dangle= " + dangle + ",vx= " + vx + ",vy= " + vy + "]";
	}
}
