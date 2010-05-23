package se.uu.it.cats.brick.filter;

public class MovementData extends ComparableData {
	public float dr; // Distance travelled
	public float dangle; // Radians turned	

	public MovementData(int timestamp, float dr, float dangle) {
		super(timestamp);
		this.dr = dr;
		this.dangle = dangle;
	}

	public boolean isMovementData() {
		return true;
	}

	public String toString() {
		return "MovementData[" + comparable + ",dr= " + dr + ",dangle= " + dangle + "]";
	}
}
