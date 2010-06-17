package GSim;

public class Settings {
	
	// positioning filter (no checked = naive positioning)
	public static final boolean USE_POSITIONING_GEOMETRIC_FILTER = false;
	public static final boolean USE_POSITIONING_UNSCENTED_KALMAN_FILTER = false;
	public static final boolean USE_POSITIONING_PARTICLE_FILTER  = false;
	
	// tracking filter
	// These variables shouldn't be final as they can be changed during runtime
	public static boolean USE_TRACKING_UNSCENTED_KALMAN_FILTER = true;  //Default: false
	public static boolean USE_TRACKING_PARTICLE_FILTER = false;  //Default: true
	
	// guide
	public static boolean USE_GUIDE = false; 
	
	// periods in ms 
	public static final int PERIOD_POSITIONING_NAIVE = 400; //Brick:330; //PC:500;
	public static final int PERIOD_POSITIONING_GEOMETRIC = 100; //Brick:500;	//Estimated time needed for one iteration = 10-100ms @ 2010-06-08
	public static final int PERIOD_POSITIONING_KALMAN = 200 ;//Brick:500; //PC:200;
	public static final int PERIOD_POSITIONING_PARTICLE = 500; //Brick:800; //PC:800;
	public static final int PERIOD_TRACKING_KALMAN = 100; //Brick:500; //Estimated time needed for one iteration = 300-400ms @ 2010-06-08
	public static final int PERIOD_TRACKING_PARTICLE = 800; //Brick:1000; //Estimated time needed for one iteration = 300-2500ms (most often 300-400)@ 2010-06-08
	//public static final int PERIOD_MAIN = 1000; //Brick:5000; //TODO: Use in main! Aka PERIOD_GUIDE Estimated time needed for one iteration = 2500-8000ms (most often 4500)@ 2010-06-08
	
	// Settings for time slotted camera settings
	static final int CAMERA_TIME_SLOTS = 3;
	static final int CAMERA_TIME_SLOT_LENGTH = 1000; // [ms]

	static final float ARENA_MIN_X = 0;
	static final float ARENA_MAX_X = (float) 3;
	static final float ARENA_MIN_Y = 0;
	static final float ARENA_MAX_Y = (float) 3;

	// Landmark data
	static final int NO_LANDMARKS = 4;
	public static final int TYPE_MOUSE = 0;
	public static final int TYPE_PURPLE = 1;
	public static final int TYPE_BLUE = 2;
	public static final int TYPE_GREEN = 3;
	public static final int TYPE_CYAN = 4;
	public static final float[][] LANDMARK_POSITION = { { 0, 0 }, { 0, 3 },
			{ 3, 0 }, { 3, 3 } };
	public static final int[] LANDMARK_COLOR = { TYPE_PURPLE, TYPE_CYAN,
			TYPE_GREEN, TYPE_BLUE };
	

}
