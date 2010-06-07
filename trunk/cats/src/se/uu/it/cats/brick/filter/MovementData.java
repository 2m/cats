package se.uu.it.cats.brick.filter;

public class MovementData extends ComparableData {
	public float dr; // Distance travelled
	public float dangle; // Radians turned
	public float xv;
	public float yv;

	public MovementData(int timestamp, float dr, float dangle) {
		super(timestamp);
		this.dr = dr;
		this.dangle = dangle;
	}

	public MovementData(int timestamp, float dr, float dangle, float xv,
			float yv) {
		super(timestamp);
		this.dr = dr;
		this.dangle = dangle;
		this.xv = xv;
		this.yv = yv;
	}

	public boolean isMovementData() {
		return true;
	}

	public String toString() {
		return "[" + comparable + ", " + dr + ", " + dangle + "]";
	}
}
