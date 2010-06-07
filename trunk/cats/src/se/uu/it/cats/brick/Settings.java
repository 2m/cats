package se.uu.it.cats.brick;

public class Settings
{
	// movement pilot settings
	public static float WHEEL_DIAMATER;
	public static float TRACK_WIDTH;
	
	// should be >0 for correcting right to left drift
	// and <0 for left to right drift
	public static float DRIFT_BALANCE;
	
	// camera offset, measured in pixels
	public static int CAMERA_OFFSET;
	
	// guide
	public static boolean USE_GUIDE = false;
	
	// orders from GUI
	public static float GUI_ORDER_X = 0;
	public static float GUI_ORDER_Y = 0;
	public static boolean GUI_ORDER_PROCESSED = true;
	
	// Settings for time slotted camera/guide settings
	public static final int CAMERA_TIME_SLOTS = 3;
	public static final int CAMERA_TIME_SLOT_LENGTH = 3000; // [ms]

	// Arena size
	public static final float ARENA_MIN_X = 0;
	public static final float ARENA_MAX_X = 3f;
	public static final float ARENA_MIN_Y = 0;
	public static final float ARENA_MAX_Y = 3f;
	
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

	// starting position and angle
	public static float START_X;
	public static float START_Y;
	public static float START_ANGLE;
	
	public static void init(int id)
	{
		switch (id)
		{
			case 0:
			{
				WHEEL_DIAMATER = 0.0544680375f;				
				TRACK_WIDTH = 0.181f;
				DRIFT_BALANCE = -0.00012f;
				
				CAMERA_OFFSET = -8;
				
				START_X = 0;
				START_Y = 0;
				START_ANGLE = 0;
				break;
			}
			case 1:
			{
				WHEEL_DIAMATER = 0.0552877815f;
				TRACK_WIDTH = 0.165f;
				DRIFT_BALANCE = 0.0004444f;
				
				CAMERA_OFFSET = -7;
				
				START_X = 0.5f;
				START_Y = 0;
				START_ANGLE = 0;
				break;
			}
			case 2:
			{
				WHEEL_DIAMATER = 0.0561170982f;
				TRACK_WIDTH = 0.16f;
				DRIFT_BALANCE = -0.0003f;
				
				CAMERA_OFFSET = -19;
				
				START_X = 0;
				START_Y = 0.5f;
				START_ANGLE = 0;
				break;
			}
		}		
	}
}
