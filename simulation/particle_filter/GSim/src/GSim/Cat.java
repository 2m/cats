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
	protected Buffer unifiedBuffer;
	protected AbsolutePositioningFilter positioningFilter;
	protected TrackingFilter trackingFilter;
	private Guide guide;
	private boolean usePositioningParticleFilter = false;
	private boolean usePositioningUnscentedKalmanFilter = true;
	private boolean useTrackingParticleFilter = false;
	private boolean useTrackingUnscentedKalmanFilter = true;
	private boolean useGuide = false;

	private int timestepsBetweenFilterUpdates = 5;

	public Cat(Actor mouse, double x, double y, double angle,
			BillBoard billboard, int id) {
		super(mouse, x, y, angle, CAT, billboard, id);
		sensors = new SensorHandler(mouse);
		unifiedBuffer = sensors.register(this);
		int N = 150;
		float T = (float) GSim.timestep * timestepsBetweenFilterUpdates / 1000; // 5;//0.5;

		if (usePositioningParticleFilter) {
			/*
			 * positioningFilter = new AbsolutePositioningParticleFilter(id, N,
			 * T, unifiedBuffer, billboard);
			 */
		} else if (usePositioningUnscentedKalmanFilter) {
			 positioningFilter = new AbsolutePositioningUKF(id, T,
			 unifiedBuffer, billboard);

		} else {
			positioningFilter = new AbsolutePositioningNaiveFilter(id, T,
					unifiedBuffer, billboard);
		}

		if (useTrackingParticleFilter) {
			trackingFilter = new TrackingParticleFilter(id, N, T, billboard);
		} else if (useTrackingUnscentedKalmanFilter) {

			trackingFilter = new TrackingUnscentedKalmanFilter(id, T,
					billboard);

		}

		positioningFilter.initData((float) motor.getX(), (float) motor.getY(),
				(float) motor.getAngle());
		if (useGuide) {
			guide = new Guide(id, billboard);
		}

	}

	public double getX() {
		return positioningFilter.getX();
	}

	public double getY() {
		return positioningFilter.getY();
	}

	public double getAngle() {
		return positioningFilter.getAngle();
	}

	public void update() {
		motor.goTo(gotox, gotoy);
		ComparableData data = motorBuffer.pop();
		while (data != null) {
			unifiedBuffer.push(data);
			data = motorBuffer.pop();
		}
		if ((useGuide)
				&& (Math.sqrt(Math.pow(getX() - gotox, 2)
						+ Math.pow(getY() - gotoy, 2)) < 0.05f) && (iter > 10)) {
			float[] adv = guide.getAdvice();
			if (adv[0] >= 0) {
				gotox = adv[0];
				gotoy = adv[1];
			}
		}
		if ((iter % timestepsBetweenFilterUpdates) == 0) {
			sensors.update();
			positioningFilter.update();
			if (useTrackingParticleFilter || useTrackingUnscentedKalmanFilter) {
				trackingFilter.update();
			}
		}
		iter++;
	}

	public void drawMore(Graphics g) {
		positioningFilter.draw(g);
		if (useTrackingParticleFilter || useTrackingUnscentedKalmanFilter) {
			trackingFilter.draw(g);
		}
		if (useGuide) {
			guide.draw(g);
		}
	}
}
