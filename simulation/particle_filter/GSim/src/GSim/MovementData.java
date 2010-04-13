package GSim;

public class MovementData {
	public int timestamp; // Global time stamp
	public double dr; // Distance travelled
	public double dangle; // Radians turned

	public MovementData(int timestamp, double dr, double dangle) {
		this.timestamp = timestamp;
		this.dr = dr;
		this.dangle = dangle;
	}

	public String toString() {
		return "[" + timestamp + ", " + dr + ", " + dangle + "]";
	}
}
