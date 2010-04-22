package GSim;

/**
 * Basic place holder for the camera controls
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class SensorHandler {
	private double field_of_view = 43 * (Math.PI / 180); // F.O.V in radians
	private boolean limit_view = false;
	public Buffer positioningBuffer, trackerBuffer;
	private Actor cat, mouse;
	private LandmarkList landmarks;
	private boolean initialPhase = true;

	public SensorHandler(LandmarkList landmarks, Actor mouse, RealTimeClock realtime) {
		this.landmarks = landmarks;
		this.mouse = mouse;
		this.realtime = realtime;
	}

	public void update() {
		if ((cat==null) || (positioningBuffer==null) || (trackerBuffer==null) {
			System.out.println("Sensorhandler not initialised!");
			System.exit(1);
		}
		// Vector to target
		float cx = cat.getX();
		float cy = cat.getY();
		float x = mouse.getX() - cx;
		float y = mouse.getY() - cy;
		int t = realtime.getTime();
		// TODO: Check atan2 usage
		float angle = Math.atan2(y, x);
		int type = 0;
		// TODO: Check if buffer gets data
		// TODO: Add noise
		trackingBuffer.push(new SightingData(t, cx, cy, angle, type));
		for(int i=0;i<LandmarkList.landmarkX.langth;i++) {
			x = LandmarkList.landmarkX[i];
			y = LandmarkList.landmarkY[i];
			if (LandmarkList.landmarkC[i]) {
				type = LandmarkList.GREEN;
			} else {
				type = LandmarkList.RED;
			}
			float angle2 = Math.atan2(y, x);
			float angle_diff = Math.abs(angle2 - angle);
			// TODO: Check atan2 usage
			if ((initialPhase) || (angle_diff<(field_of_view/2))) {
				SightingData d = new SightingData(t, cx, cy, angle2, type);
				// TODO: Check if buffer gets data
				// TODO: Add noise
				positioningBuffer.push(d);
			}
		}
		// TODO: Check if initial phase has ended
	}

	public void register(Actor cat){
		this.cat = cat;
	}
	
	public Buffer regPositioner() {
		positioningBuffer = new BufferSorted();
		return positioningBuffer;
	}
	
	public Buffer regTracker() {		
		trackerBuffer = new BufferSorted();
		return trackerBuffer;
	}
}
