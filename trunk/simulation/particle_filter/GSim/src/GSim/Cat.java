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
	protected TrackingFilter trackingFilter;
	private Guide guide;

	public Cat(Actor mouse, double x, double y, double angle,
			RealTimeClock clock, BillBoard billboard, int id) {
		super(mouse, x, y, angle, CAT, clock, billboard, id);
		sensors = new SensorHandler(mouse, clock);
		sensors.register(this);
		positioningBuffer = sensors.regPositioner();
		trackerBuffer = sensors.regTracker();
		int N = 80;
		float T = (float) 0.5;
		/*
		 * positioningFilter = new AbsolutePositioningUKF(T, positioningBuffer,
		 * motorBuffer, clock);
		 */

		positioningFilter = new AbsolutePositioningParticleFilter(N, T,
				positioningBuffer, motorBuffer, clock);

		trackingFilter = new TrackingParticleFilter(id, N, T, trackerBuffer,
				clock, billboard);
		positioningFilter.initData((float) motor.getX(), (float) motor.getY(),
				(float) motor.getAngle());
		guide = new Guide(id, billboard);
	}

	/**
	 * Update the actor This should be overloaded
	 */
	public void update() {
		motor.goTo(gotox, gotoy);
		float[] g = guide.getGradient((float) gotox, (float) gotoy);
		gotox += 0.01 * Math.signum(g[0]);
		gotoy += 0.01 * Math.signum(g[1]);
		if ((iter % 5) == 0) {
			sensors.update();
			positioningFilter.update();
			trackingFilter.update();
		}
		iter++;
	}

	public void drawMore(Graphics g) {
		// positioningFilter.draw(g);
		trackingFilter.draw(g);
		guide.draw(g);

	}
}
