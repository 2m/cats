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
			RealTimeClock clock, BillBoard billboard) {
		super(mouse, x, y, angle, CAT, clock, billboard);
		sensors = new SensorHandler(mouse, clock);
		sensors.register(this);
		positioningBuffer = sensors.regPositioner();
		trackerBuffer = sensors.regTracker();
		int N = 500;
		float T = (float) 0.5;
		positioningFilter = new AbsolutePositioningParticleFilter(N, T,
				positioningBuffer, motorBuffer, clock);
		positioningFilter.initData((float) motor.getX(), (float) motor.getY(),
				(float) motor.getAngle());
	}

	/**
	 * Update the actor This should be overloaded
	 */
	public void update() {
		motor.goTo(gotox, gotoy);
		if ((iter % 5) == 0) {
			sensors.update();
			// System.out.println(positioningFilter);
			positioningFilter.update();
			// trackingFilter.update();
			trackerBuffer.pop();
			System.out.println("---");
		}
		iter++;
	}

	public void drawMore(Graphics g) {
		positioningFilter.draw(g);
		// trackingFilter.draw(g);

	}
}
