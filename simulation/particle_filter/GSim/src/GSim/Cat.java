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
	protected Buffer positioningBuffer, trackerBuffer, unifiedBuffer;
	protected AbsolutePositioningFilter positioningFilter;
	protected TrackingFilter trackingFilter;
	private Guide guide;

	private boolean usePositioningParticleFilter = false;
	private boolean usePositioningUnscentedKalmanFilter = false;
	private boolean useTrackingParticleFilter = false;
	private boolean useTrackingUnscentedKalmanFilter = false;
	private boolean useGuide = true;

	private int timestepsBetweenFilterUpdates = 5;

	public Cat(Actor mouse, double x, double y, double angle,
			BillBoard billboard, int id) {
		super(mouse, x, y, angle, CAT, billboard, id);
		sensors = new SensorHandler(mouse);
		sensors.register(this);
		positioningBuffer = sensors.regPositioner();
		trackerBuffer = sensors.regTracker();
		unifiedBuffer = new BufferSorted();
		int N = 150;
		float T = (float) GSim.timestep * timestepsBetweenFilterUpdates / 1000; // 5;//0.5;

		if (usePositioningParticleFilter) {
			/*
			 * positioningFilter = new AbsolutePositioningParticleFilter(id, N,
			 * T, unifiedBuffer, billboard);
			 */
		} else if (usePositioningUnscentedKalmanFilter) {
			/*
			 * positioningFilter = new AbsolutePositioningUKF(id, T,
			 * unifiedBuffer, billboard);
			 */
		} else {
			positioningFilter = new AbsolutePositioningNaiveFilter(id, T,
					unifiedBuffer, billboard);
		}

		if (useTrackingParticleFilter) {
			trackingFilter = new TrackingParticleFilter(id, N, T, billboard);
		} else if (useTrackingUnscentedKalmanFilter) {
			/*
			 * trackingFilter = new TrackingUnscentedKalmanFilter(id, T,
			 * billboard);
			 */
		}

		positioningFilter.initData((float) motor.getX(), (float) motor
					.getY(), (float) motor.getAngle());
		if (useGuide) {
			guide = new Guide(id, billboard);
		}

	}

	public void update() {
		// Move motor data to the unified buffer
		ComparableData d = motorBuffer.pop();
		while (d != null) {
			unifiedBuffer.push(d);
			d = motorBuffer.pop();
		}
		// Move mouse sightings to the unified buffer
		d = trackerBuffer.pop();
		while (d != null) {
			unifiedBuffer.push(d);
			SightingData sd = (SightingData) d;
			sd.type = LandmarkList.MOUSE;
			d = trackerBuffer.pop();
		}
		// Move landmark sightings to the unified buffer
		d = positioningBuffer.pop();
		while (d != null) {
			unifiedBuffer.push(d);
			d = positioningBuffer.pop();
		}

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
