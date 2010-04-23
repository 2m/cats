package GSim;

public class MovementData extends BufferData {
	public float dr; // Distance travelled
	public float dangle; // Radians turned

	public MovementData(int timestamp, float dr, float dangle) {
		super(timestamp);
		this.dr = dr;
		this.dangle = dangle;
	}

	public String toString() {
		return "[" + timestamp + ", " + dr + ", " + dangle + "]";
	}
}
