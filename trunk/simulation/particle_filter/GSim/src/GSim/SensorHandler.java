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

	public SensorHandler(Actor mouse) {
		this.mouse = mouse;
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
		float angle = (float) Math.atan2(y1, x1);
		int t = Clock.timestamp();
		SightingData d = new SightingData(t, cx, cy, (float) (angle
				- cat.getObjectiveAngle() + rnd.nextGaussian() * 1
				* (Math.PI / 180)), LandmarkList.MOUSE);
		unifiedBuffer.push(d);
		int type = -1;
		for (int i = 0; i < LandmarkList.landmarkX.length; i++) {
			float x2 = LandmarkList.landmarkX[i] - cx;
			float y2 = LandmarkList.landmarkY[i] - cy;
			angle = (float) Math.atan2(y2, x2);
			if (LandmarkList.landmarkC[i]) {
				type = LandmarkList.GREEN;
			} else {
				type = LandmarkList.RED;
			}
			float angle_diff = (float) Math
					.abs(angle - cat.getObjectiveAngle());
			if ((initialPhase) || (angle_diff < (field_of_view / 2))) {
				d = new SightingData(t, cx, cy, (float) (angle
						- cat.getObjectiveAngle() + rnd.nextGaussian() * 1
						* (Math.PI / 180)), type);
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
