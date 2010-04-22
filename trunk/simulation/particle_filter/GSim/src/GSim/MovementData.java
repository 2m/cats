package GSim;

public class MovementData extends BufferData {
	public double dr; // Distance travelled
	public double dangle; // Radians turned

	public MovementData(int timestamp, double dr, double dangle) {
		super(timestamp);
		this.dr = dr;
		this.dangle = dangle;
	}

	public String toString() {
		return "[" + timestamp + ", " + dr + ", " + dangle + "]";
	}
}
