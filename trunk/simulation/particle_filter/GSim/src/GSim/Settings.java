package GSim;

public class Settings {
	// Settings for time slotted camera settings
	static final int CAMERA_TIME_SLOTS = 3;
	static final int CAMERA_TIME_SLOT_LENGTH = 3000; // [ms]

	static final float ARENA_MIN_X = 0;
	static final float ARENA_MAX_X = (float) 3;
	static final float ARENA_MIN_Y = 0;
	static final float ARENA_MAX_Y = (float) 3;

	// Landmark data
	static final int NO_LANDMARKS = 4;
	// FIXME: Add real colours to landmark lists in settings
	public static final int TYPE_MOUSE = 0;
	public static final int TYPE_GREEN = 1;
	public static final int TYPE_RED = 2;
	public static final float[][] LANDMARK_POSITION = { { 0, 0 }, { 0, 3 },
			{ 3, 0 }, { 3, 3 } };
	public static final int[] LANDMARK_COLOR = { TYPE_GREEN, TYPE_GREEN,
			TYPE_GREEN, TYPE_RED };

}
