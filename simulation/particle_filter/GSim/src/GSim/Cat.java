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
		if (Settings.USE_POSITIONING_PARTICLE_FILTER) {
			positioningFilter = new AbsolutePositioningParticleFilter(id,
					Npositioning, (float) (Settings.PERIOD_POSITIONING_PARTICLE) / 1000f,
					unifiedBuffer, billboard);
		} else if (Settings.USE_POSITIONING_UNSCENTED_KALMAN_FILTER) {
			positioningFilter = new AbsolutePositioningUKF(id,
					(float) (Settings.PERIOD_POSITIONING_KALMAN) / 1000f, unifiedBuffer,
					billboard);

		} else if (Settings.USE_POSITIONING_GEOMETRIC_FILTER) {
			positioningFilter = new AbsolutePositioningGeometricFilter(id,
					(float) (Settings.PERIOD_POSITIONING_GEOMETRIC) / 1000f, unifiedBuffer,
					billboard);
		} else {
			positioningFilter = new AbsolutePositioningNaiveFilter(id,
					(float) (Settings.PERIOD_POSITIONING_NAIVE) / 1000f, unifiedBuffer,
					billboard);
		}

		if (Settings.USE_TRACKING_PARTICLE_FILTER) {
			trackingFilter = new TrackingParticleFilter(id, Ntracking,
					(float) (Settings.PERIOD_TRACKING_PARTICLE) / 1000f, billboard);
		} else if (Settings.USE_TRACKING_UNSCENTED_KALMAN_FILTER) {
			trackingFilter = new TrackingUnscentedKalmanFilter(id,
					(float) (Settings.PERIOD_TRACKING_KALMAN) / 1000f, billboard);
		}

		positioningFilter.initData((float) motor.getX(), (float) motor.getY(),
				(float) motor.getAngle());
		if (Settings.USE_GUIDE) {
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
			if (Settings.USE_GUIDE) {
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
			if (Settings.USE_POSITIONING_PARTICLE_FILTER) {
				nextpos += Settings.PERIOD_POSITIONING_PARTICLE;
			} else if (Settings.USE_POSITIONING_UNSCENTED_KALMAN_FILTER) {
				nextpos += Settings.PERIOD_POSITIONING_KALMAN;
			} else if (Settings.USE_POSITIONING_GEOMETRIC_FILTER) {
				nextpos += Settings.PERIOD_POSITIONING_GEOMETRIC;
			} else {
				nextpos += Settings.PERIOD_POSITIONING_NAIVE;
			}
		}
		if (nexttrack < Clock.timestamp()) {
			if (Settings.USE_TRACKING_PARTICLE_FILTER) {
				trackingFilter.update();
				nexttrack += Settings.PERIOD_TRACKING_PARTICLE;
			} else if (Settings.USE_TRACKING_UNSCENTED_KALMAN_FILTER) {
				trackingFilter.update();
				nexttrack += Settings.PERIOD_TRACKING_KALMAN;
			}
		}
		iter++;
	}

	public void drawMore(Graphics g) {
		positioningFilter.draw(g);
		if (Settings.USE_TRACKING_PARTICLE_FILTER || Settings.USE_TRACKING_UNSCENTED_KALMAN_FILTER) {
			trackingFilter.draw(g);
		}
		if (Settings.USE_GUIDE) {
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
