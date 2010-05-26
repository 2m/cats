package GSim;

public class LandmarkList {
	// FIXME: Add real colours to landmark lists
	public static final int MOUSE = 0;
	public static final int GREEN = 1;
	public static final int RED = 2;

	// Positions of landmarks
	public static final float[] landmarkX = { (float) 0.05, (float) 0.05,
			(float) 2.95, (float) 2.95 };
	public static final float[] landmarkY = { (float) 0.05, (float) 2.95,
			(float) 0.05, (float) 2.95 };
	public static final boolean[] landmarkC = { true, true, true, false };
	// True if green, false if red
	public static final int[] Colour = { GREEN, GREEN, GREEN, RED };
}
