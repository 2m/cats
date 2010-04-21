package GSim;

/**
 * This is a class for a landmark
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 */

public class LandMark extends Actor {

	public LandMark(Actor mouse, LandmarkList landmarks, double x, double y) {
		super(mouse, landmarks, x, y, 0.0, LANDMARK);
	}


}
