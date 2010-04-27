package GSim;

/**
 * The Mouse
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class Mouse extends Actor {
	public Mouse(Actor mouse, double x, double y, double angle,
			RealTimeClock clock, BillBoard billboard) {
		super(mouse, x, y, angle, MOUSE, clock, billboard);
	}

}
