package GSim;

public class LandmarkList {
	// FIXME: Depricated
	public static final int MOUSE_ = 0;
	public static final int GREEN_ = 1;
	public static final int RED_ = 2;

	// Positions of landmarks
	public static final float[] landmarkX_ = { (float) 0.05, (float) 0.05,
			(float) 2.95, (float) 2.95 };
	public static final float[] landmarkY_ = { (float) 0.05, (float) 2.95,
			(float) 0.05, (float) 2.95 };
	public static final boolean[] landmarkC_ = { true, true, true, false };
	// True if green, false if red
	public static final int[] Colour_ = { GREEN_, GREEN_, GREEN_, RED_ };
}
