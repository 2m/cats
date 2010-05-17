package se.uu.it.cats.brick.filter;

public class ComparableData {
	int comparable;

	public ComparableData(int comparable) {
		this.comparable = comparable;
	}

	public int getComparable() {
		return comparable;
	}

	public boolean isPositioningParticle() {
		return false;
	}

	public boolean isTrackingParticle() {
		return false;
	}

	public boolean isSightingData() {
		return false;
	}

	public boolean isMovementData() {
		return false;
	}
}
