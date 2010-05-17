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

	private boolean usePositioningParticleFilter = true;
	private boolean usePositioningUnscentedKalmanFilter = false;
	private boolean useTrackingParticleFilter = true;
	private boolean useTrackingUnscentedKalmanFilter = false;
	private boolean useGuide = true;

	private int timestepsBetweenFilterUpdates = 5;

	public Cat(Actor mouse, double x, double y, double angle,
			RealTimeClock clock, BillBoard billboard, int id) {
		super(mouse, x, y, angle, CAT, clock, billboard, id);
		sensors = new SensorHandler(mouse, clock);
		sensors.register(this);
		positioningBuffer = sensors.regPositioner();
		trackerBuffer = sensors.regTracker();
		int N = 150;
		float T = (float) GSim.timestep * timestepsBetweenFilterUpdates / 1000; // 5;//0.5;

		if (usePositioningParticleFilter) {
			positioningFilter = new AbsolutePositioningParticleFilter(N, T,
					positioningBuffer, motorBuffer, clock);
		} else if (usePositioningUnscentedKalmanFilter) {
			positioningFilter = new AbsolutePositioningUKF(T,
					positioningBuffer, motorBuffer, clock);
		} else {
			positioningFilter = new AbsolutePositioningNaiveFilter(T,
					positioningBuffer, motorBuffer, clock);
		}

		if (useTrackingParticleFilter) {
			trackingFilter = new TrackingParticleFilter(id, N, T,
					trackerBuffer, clock, billboard);
		} else if (useTrackingUnscentedKalmanFilter) {
			// TODO implement tracking UKF in cat
			/*
			 * trackingFilter = new TrackingUKF(id, N, T, trackerBuffer, clock,
			 * billboard);
			 */
		}

		if (usePositioningParticleFilter || usePositioningUnscentedKalmanFilter) {
			positioningFilter.initData((float) motor.getX(), (float) motor
					.getY(), (float) motor.getAngle());
		}
		if (useGuide) {
			guide = new Guide(id, billboard);
		}

	}

	public void update() {
		motor.goTo(gotox, gotoy);
		if (useGuide) {
			/*
			 * if (id == 1) { System.out.println("Distance: " +
			 * Math.max(Math.abs(getX() - gotox), Math.abs(getY() - gotoy))); }
			 */
			if (Math.max(Math.abs(getX() - gotox), Math.abs(getY() - gotoy)) < 0.05f) {
				int i = 0;
				float gx = (float) gotox, gy = (float) gotoy;
				while (i < 10) {
					float[] g = guide.getGradient((float) gx, (float) gy);
					gx += 0.01 * Math.signum(g[0]);
					gy += 0.01 * Math.signum(g[1]);
					i++;
				}
				/*
				 * if (Math .max(Math.abs(getX() - gotox), Math.abs(getY() -
				 * gotoy)) > 0.05f) { System.out.println("(gx, gy): " + gx + " "
				 * + gy);
				 */
				gotox = gx;
				gotoy = gy;
				// }
			}
		}
		if ((iter % timestepsBetweenFilterUpdates) == 0) {
			sensors.update();
			if (usePositioningParticleFilter
					|| usePositioningUnscentedKalmanFilter) {
				positioningFilter.update();
			}
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
