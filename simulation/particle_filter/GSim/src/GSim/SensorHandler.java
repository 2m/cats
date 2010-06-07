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
	public Buffer unifiedBuffer;
	private Actor cat, mouse;
	private boolean initialPhase = true;
	private Random rnd = new Random();
	private boolean lookForLandmarksFlag = false;
	private float ERROR_STD = 2;// Standard deviation in degrees

	public SensorHandler(Actor mouse) {
		this.mouse = mouse;
	}

	public void setLookForLandmarksFlag(boolean flag) {
		lookForLandmarksFlag = flag;
	}

	public void update() {
		if ((cat == null) || (unifiedBuffer == null)) {
			System.out.println("Sensorhandler not initialised!");
			System.exit(1);
		}
		// Vector to target
		float cx = (float) cat.getObjectiveX();
		float cy = (float) cat.getObjectiveY();
		float x1 = (float) mouse.getObjectiveX() - cx;
		float y1 = (float) mouse.getObjectiveY() - cy;

		float looking_angle = (float) Math.atan2(y1, x1);
		if (lookForLandmarksFlag) {
			// This tries to simulate looking around
			looking_angle += ((rnd.nextDouble() - 0.5) * 2) * Math.PI;
			looking_angle %= Math.PI * 2;
		}

		float angle_to_mouse = (float) Math.atan2(y1, x1);

		int t = Clock.timestamp();

		float angle_diff = (float) Math.abs(angle_to_mouse - looking_angle);
		if ((initialPhase) || (angle_diff < (field_of_view / 2))) {
			SightingData d = new SightingData(t, cx, cy,
					(float) (angle_to_mouse - cat.getObjectiveAngle() + rnd
							.nextGaussian()
							* ERROR_STD * (Math.PI / 180)), Settings.TYPE_MOUSE);
			unifiedBuffer.push(d);
		}
		int type = -1;
		for (int i = 0; i < Settings.LANDMARK_POSITION.length; i++) {
			float x2 = Settings.LANDMARK_POSITION[i][0] - cx;
			float y2 = Settings.LANDMARK_POSITION[i][1] - cy;
			float angle_to_landmark = (float) Math.atan2(y2, x2);
			type = Settings.LANDMARK_COLOR[i];
			angle_diff = (float) Math.abs(angle_to_landmark - looking_angle);
			if ((initialPhase) || (angle_diff < (field_of_view / 2))) {
				SightingData d = new SightingData(
						t,
						cx,
						cy,
						(float) (angle_to_landmark - cat.getObjectiveAngle() + rnd
								.nextGaussian()
								* ERROR_STD * (Math.PI / 180)), type);
				unifiedBuffer.push(d);
			}
		}

		// Needs to leave initial phase to make the field of view check work
		if (initialPhase) {
			initialPhase = false;
		}
	}

	public Buffer register(Actor cat) {
		this.cat = cat;
		unifiedBuffer = new BufferSorted();
		return unifiedBuffer;
	}
}
