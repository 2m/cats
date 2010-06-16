package se.uu.it.cats.brick;

public class Settings {
	// movement pilot settings
	public static float WHEEL_DIAMATER;
	public static float TRACK_WIDTH;

	// should be >0 for correcting right to left drift
	// and <0 for left to right drift
	public static float DRIFT_BALANCE;

	// camera offset, measured in pixels
	public static int CAMERA_OFFSET;

	// positioning filter (no checked = naive positioning)
	public static final boolean USE_POSITIONING_GEOMETRIC_FILTER = true;
	public static final boolean USE_POSITIONING_UNSCENTED_KALMAN_FILTER = false;
	public static final boolean USE_POSITIONING_PARTICLE_FILTER  = false;
	
	// tracking filter
	// These variables shouldn't be final as they can be changed during runtime
	public static boolean USE_TRACKING_UNSCENTED_KALMAN_FILTER = false;  
	public static boolean USE_TRACKING_PARTICLE_FILTER = true;
	
	// enable sweeps at some motor command count
	public static final boolean ENABLE_SWEEPS = true;
	
	// guide
	public static boolean USE_GUIDE = false; //Should always be enabled in the GUI instead

	// orders from GUI
	public static float GUI_ORDER_X = 0;
	public static float GUI_ORDER_Y = 0;
	public static boolean GUI_ORDER_PROCESSED = true;

	// Settings for time slotted camera/guide settings
	public static final int CAMERA_TIME_SLOTS = 3;
	public static final int CAMERA_TIME_SLOT_LENGTH = 2000; // [ms]
	
	// periods in ms 
	public static final int PERIOD_POSITIONING_NAIVE = 330; //PC:500;
	public static final int PERIOD_POSITIONING_GEOMETRIC = 500;	//Estimated time needed for one iteration = 10-100ms @ 2010-06-08
	public static final int PERIOD_POSITIONING_KALMAN = 500; //PC:200;
	public static final int PERIOD_POSITIONING_PARTICLE = 800; //PC:800;
	public static final int PERIOD_TRACKING_KALMAN = 500; //Estimated time needed for one iteration = 300-400ms @ 2010-06-08
	public static final int PERIOD_TRACKING_PARTICLE = 1000; //Estimated time needed for one iteration = 300-2500ms (most often 300-400)@ 2010-06-08
	public static final int PERIOD_MAIN = 5000; //TODO: Use in main! Aka PERIOD_GUIDE Estimated time needed for one iteration = 2500-8000ms (most often 4500)@ 2010-06-08
	
	// particle filter settings
	public static final int N_TRACKING = 60; //PC:100;
	public static final int N_POSITIONING = 200;

	// Arena size
	public static final float ARENA_MIN_X = 0;
	public static final float ARENA_MAX_X = 2.5f;
	public static final float ARENA_MIN_Y = 0;
	public static final float ARENA_MAX_Y = 2.5f;

	// Landmark data
	public static final int NO_LANDMARKS = 4;
	public static final int TYPE_MOUSE = 0;
	public static final int TYPE_PURPLE = 1;
	public static final int TYPE_BLUE = 2;
	public static final int TYPE_GREEN = 3;
	public static final int TYPE_WHITE = 4;

	// Positions [number][coordinate as (x, y)]
	public static final float[][] LANDMARK_POSITION = { { 0, 0 }, { 0, 2.5f },
			{ 2.5f, 0 }, { 2.5f, 2.5f } };
	public static final int[] LANDMARK_COLOR = { TYPE_PURPLE, TYPE_WHITE,
		TYPE_GREEN, TYPE_BLUE };

	// starting position and angle
	public static float START_X;
	public static float START_Y;
	public static float START_ANGLE;

	public static void init(int id) {
		switch (id) {
		case 0: {
			WHEEL_DIAMATER = 0.0544680375f;
			TRACK_WIDTH = 0.181f;
			DRIFT_BALANCE = -0.00012f;

			CAMERA_OFFSET = -10;

			START_X = 1f;
			START_Y = 0;
			START_ANGLE = 0;
			break;
		}
		case 1: {
			WHEEL_DIAMATER = 0.0552877815f;
			TRACK_WIDTH = 0.165f;
			DRIFT_BALANCE = 0.0004444f;

			CAMERA_OFFSET = -14;

			START_X = 0.134f;
			START_Y = 1.5f;
			START_ANGLE = 0;
			break;
		}
		case 2: {
			WHEEL_DIAMATER = 0.0561170982f;
			TRACK_WIDTH = 0.16f;
			DRIFT_BALANCE = -0.0003f;

			CAMERA_OFFSET = -3;

			START_X = 1.866f;
			START_Y = 1.5f;
			START_ANGLE = 0;
			break;
		}
		}
	}
}
