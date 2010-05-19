package GSim;

import java.util.Random;

/**
 * Basic place holder for the camera controls
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class SensorHandler {
	private double field_of_view = 43 * (Math.PI / 180); // F.O.V in radians
	public Buffer positioningBuffer, trackingBuffer;
	private Actor cat, mouse;
	private boolean initialPhase = true;
	private Random rnd = new Random();

	public SensorHandler(Actor mouse) {
		this.mouse = mouse;
	}

	public void update() {
		if ((cat == null) || (positioningBuffer == null)
				|| (trackingBuffer == null)) {
			System.out.println("Sensorhandler not initialised!");
			System.exit(1);
		}
		// Vector to target
		float cx = (float) cat.getX();
		float cy = (float) cat.getY();
		float x1 = (float) mouse.getX() - cx;
		float y1 = (float) mouse.getY() - cy;
		// float norm1 = (float) Math.sqrt(x1 * x1 + y1 * y1);
		float angle1 = (float) Math.atan2(y1, x1);
		int t = Clock.getTime();
		int type = 0;
		// TODO: Use estimated x and y in tracking data
		SightingData d = new SightingData(t, cx, cy, (float) (angle1 + rnd
				.nextGaussian()
				* 1 * (Math.PI / 180)), type);
		// System.out.println("Push: " + d);
		trackingBuffer.push(d);
		for (int i = 0; i < LandmarkList.landmarkX.length; i++) {
			float x2 = LandmarkList.landmarkX[i] - cx;
			float y2 = LandmarkList.landmarkY[i] - cy;
			// float norm2 = (float) Math.sqrt(x2 * x2 + y2 * y2);
			float angle2 = (float) Math.atan2(y2, x2);
			if (LandmarkList.landmarkC[i]) {
				type = LandmarkList.GREEN;
			} else {
				type = LandmarkList.RED;
			}
			// System.out.println("(cx, cy, angle)=(" + cx + ", " + cy + ", " +
			// angle2*(180/Math.PI) + ")");
			// float angle_diff = (float) Math.acos((x1 * x2 + y1 * y2)
			// / (norm1 * norm2));
			float angle_diff = (float) Math.abs(angle2 - cat.getAngle());
			// System.out.println("In sensor, landmark " + i + ", angle diff = "
			// + Math.toDegrees(angle_diff) + ", fov/2 = " +
			// Math.toDegrees((field_of_view / 2)));
			if ((initialPhase) || (angle_diff < (field_of_view / 2))) {
				// System.out.println("In sensor, landmark " + i +
				// " within field of view");
				/*
				 * d = new SightingData(t, cx, cy, (float) ((angle2 -
				 * cat.getAngle()) + rnd.nextGaussian() 3 * (Math.PI / 180)),
				 * type);
				 */
				// TODO: Add noise
				d = new SightingData(t, cx, cy, (float) (angle2 - cat
						.getAngle()), type);
				// TODO: Angle_diff might be wrong
				// System.out.println("i:" + i + " Push: " + d + " Anglediff: "
				// + angle_diff*(180/Math.PI));
				positioningBuffer.push(d);
			}
		}

		// Needs to leave initial phase to make the field of view check work
		if (initialPhase)
			initialPhase = false;

		// TODO: Check if initial convergence phase has ended
	}

	public void register(Actor cat) {
		this.cat = cat;
	}

	public Buffer regPositioner() {
		positioningBuffer = new BufferSorted();
		return positioningBuffer;
	}

	public Buffer regTracker() {
		trackingBuffer = new BufferSorted();
		return trackingBuffer;
	}
}
