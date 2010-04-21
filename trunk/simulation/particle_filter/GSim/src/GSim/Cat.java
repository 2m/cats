package GSim;

import java.awt.Graphics;

/**
 * The Cat
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class Cat extends Actor {
	protected Buffer positioningBuffer, trackerBuffer;

	public Cat(Actor mouse, LandmarkList landmarks, double x, double y,
			double angle) {
		super(mouse, landmarks, x, y, angle, CAT);
		sensors = new SensorHandler(landmarks, mouse);
		sensors.register(this);
		positioningBuffer = sensors.regPositioner();
		trackerBuffer = sensors.regTracker();
	}

	/**
	 * Update the actor This should be overloaded
	 */
	public void update() {
		motor.goTo(gotox, gotoy);
		if ((iter % 5) == 0) {
			// sensors.update();
			// TODO: Call filters every 5 iteration
			// positioningFilter.update();
			// trackingFilter.update();
		}
		iter++;
	}

	public void drawMore(Graphics g) {

		// TODO: Draw particles
		// positioningFilter.update();
		// trackingFilter.update();

	}
}
