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
	protected AbsolutePositioningFilter positioningFilter;

	public Cat(Actor mouse, double x, double y, double angle,
			RealTimeClock clock) {
		super(mouse, x, y, angle, CAT, clock);
		sensors = new SensorHandler(mouse, clock);
		sensors.register(this);
		positioningBuffer = sensors.regPositioner();
		trackerBuffer = sensors.regTracker();
		int N = 50;
		float T = (float) 0.5;
		positioningFilter = new AbsolutePositioningParticleFilter(N, T,
				positioningBuffer, motorBuffer, clock);
		positioningFilter.initData((float)x, (float)y, (float)angle);
	}

	/**
	 * Update the actor This should be overloaded
	 */
	public void update() {
		motor.goTo(gotox, gotoy);
		if ((iter % 5) == 0) {
			sensors.update();
			// positioningFilter.update();
			// trackingFilter.update();
		}
		iter++;
	}

	public void drawMore(Graphics g) {
		positioningFilter.draw(g);
		// trackingFilter.draw(g);

	}
}
