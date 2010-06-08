package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
	private boolean usePositioningGeometricFilter = false;
	private boolean usePositioningUnscentedKalmanFilter = false;
	private boolean useTrackingParticleFilter = false;
	private boolean useTrackingUnscentedKalmanFilter = true;
	private boolean useGuide = true;

	/* Periods in ms */
	private int positioningNaivePeriod = 500;// 200;
	private int trackingKalmanPeriod = 50;// 50;
	private int positioningKalmanPeriod = 200;// 500;
	// NB: working ok @500ms with q = 0.5f and stddegrees = 1f,
	// working somewhat ok @750ms with q = 0.005f and stddegrees = 1f

	private int trackingParticlePeriod = 500;
	private int positioningParticlePeriod = 800;
	private int positioningGeometricPeriod = 500;

	private int nexttrack, nextpos;
	private int Ntracking = 100;
	private int Npositioning = 200;

	private int guideCounter = 0;

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

		} else if (usePositioningGeometricFilter) {
			positioningFilter = new AbsolutePositioningGeometricFilter(id,
					(float) positioningGeometricPeriod / 1000f, unifiedBuffer,
					billboard);
		} else {
			positioningFilter = new AbsolutePositioningNaiveFilter(id,
					(float) positioningNaivePeriod / 1000f, unifiedBuffer,
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
		int time = Clock.timestamp()
				% (Settings.CAMERA_TIME_SLOT_LENGTH * Settings.CAMERA_TIME_SLOTS);
		int slot = (int) Math.floor(time / Settings.CAMERA_TIME_SLOT_LENGTH);
		if ((iter % 5) == 0) {
			sensors.setLookForLandmarksFlag(slot == id);
			sensors.update();
		}
		if ((iter % 10) == 0) {
			if (useGuide) {
				if ((slot != id)
						&& (guideCounter > 5)
						&& (iter > 10)
						&& (Math.sqrt(Math.pow(gotox - motor.getX(), 2)
								+ Math.pow(gotoy - motor.getY(), 2)) < 0.1f)) {
					float[] adv = guide.getAdvice();
					if (adv[0] >= 0) {
						gotox = adv[0] - (getX() - motor.getX());
						gotoy = adv[1] - (getY() - motor.getY());
						if (id == 0) {
							System.out.println("Advice: (" + adv[0] + ", "
									+ adv[1] + ")");
						}
					}
					guideCounter = 0;
				}
				guideCounter++;
			}
		}

		if (nextpos < Clock.timestamp()) {
			positioningFilter.update();
			if (usePositioningParticleFilter) {
				nextpos += positioningParticlePeriod;
			} else if (usePositioningUnscentedKalmanFilter) {
				nextpos += positioningKalmanPeriod;
			} else {
				nextpos += positioningNaivePeriod;
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

		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.RED);
		int ix = Actor.e2gX(gotox);
		int iy = Actor.e2gY(gotoy);
		int size = 3;
		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
	}
}
