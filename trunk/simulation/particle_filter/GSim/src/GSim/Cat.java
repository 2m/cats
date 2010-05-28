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
	private boolean usePositioningUnscentedKalmanFilter = false;
	private boolean useTrackingParticleFilter = false;
	private boolean useTrackingUnscentedKalmanFilter = true;
	private boolean useGuide = false;

	/* Periods in ms */
	private int trackingKalmanPeriod = 50;
	private int positioningKalmanPeriod = 200;
	private int trackingParticlePeriod = 200;
	private int positioningParticlePeriod = 200;

	private int nexttrack, nextpos;
	private int Ntracking = 100;
	private int Npositioning = 200;

	public Cat(Actor mouse, double x, double y, double angle,
			BillBoard billboard, int id) {
		super(mouse, x, y, angle, CAT, billboard, id);
		sensors = new SensorHandler(mouse);
		unifiedBuffer = sensors.register(this);
		nexttrack = Clock.timestamp();
		nextpos = Clock.timestamp();
		if (usePositioningParticleFilter) {
			positioningFilter = new AbsolutePositioningParticleFilter(id,
					Npositioning, (float) positioningParticlePeriod / 1000f,
					unifiedBuffer, billboard);
		} else if (usePositioningUnscentedKalmanFilter) {
			positioningFilter = new AbsolutePositioningUKF(id,
					(float) positioningKalmanPeriod / 1000f, unifiedBuffer,
					billboard);

		} else {
			positioningFilter = new AbsolutePositioningNaiveFilter(id,
					(float) positioningKalmanPeriod / 1000f, unifiedBuffer,
					billboard);
		}

		if (useTrackingParticleFilter) {
			trackingFilter = new TrackingParticleFilter(id, Ntracking,
					(float) trackingParticlePeriod / 1000f, billboard);
		} else if (useTrackingUnscentedKalmanFilter) {
			trackingFilter = new TrackingUnscentedKalmanFilter(id,
					(float) trackingKalmanPeriod / 1000f, billboard);
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
		if ((iter % 10) == 0) {
			sensors.update();
			if ((useGuide)
					&& (Math.sqrt(Math.pow(getX() - gotox, 2)
							+ Math.pow(getY() - gotoy, 2)) < 0.05f)
					&& (iter > 10)) {
				float[] adv = guide.getAdvice();
				if (adv[0] >= 0) {
					gotox = adv[0];
					gotoy = adv[1];
					System.out.println("Advice: (" + adv[0] + ", " + adv[1]
							+ ")");
				}
			}
		}

		if (nextpos < Clock.timestamp()) {
			positioningFilter.update();
			if (useTrackingParticleFilter) {
				nextpos += positioningParticlePeriod;
			} else {
				nextpos += positioningKalmanPeriod;
			}
		}
		if (nexttrack < Clock.timestamp()) {
			if (useTrackingParticleFilter) {
				trackingFilter.update();
				nexttrack += trackingParticlePeriod;
			} else if (useTrackingUnscentedKalmanFilter) {
				trackingFilter.update();
				nexttrack += trackingKalmanPeriod;
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
