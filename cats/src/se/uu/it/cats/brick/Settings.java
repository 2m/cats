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
	
	// positioning filters (only one should be selected)
	public static boolean POSITIONING_FILTER_PARTICLE 			= false;
	public static boolean POSITIONING_FILTER_UNSCENTED_KALMAN	= false;
	public static boolean POSITIONING_FILTER_BASIC				= true;
	
	// tracking filters (only one should be selected)
	public static boolean TRACKING_FILTER_PARTICLE				= true;
	public static boolean TRACKING_FILTER_UNSCENTED_KALMAN		= false;
	
	// guide
	public static boolean USE_GUIDE = false;
	
	public static void init()
	{
		switch (Identity.getId())
		{
			case 0:
			{
				WHEEL_DIAMATER = 0.0544680375f;				
				TRACK_WIDTH = 0.181f;
				DRIFT_BALANCE = -0.00012f;
				
				CAMERA_OFFSET = -8;
				break;
			}
			case 1:
			{
				WHEEL_DIAMATER = 0.0552877815f;
				TRACK_WIDTH = 0.165f;
				DRIFT_BALANCE = 0.0004444f;
				
				CAMERA_OFFSET = -7;
				break;
			}
			case 2:
			{
				WHEEL_DIAMATER = 0.0561170982f;				
				TRACK_WIDTH = 0.16f;
				DRIFT_BALANCE = 0;
				
				CAMERA_OFFSET = -19;
				break;
			}
		}		
	}
}
