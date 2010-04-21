package GSim;

/**
 * Basic place holder for the camera controls
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class SensorHandler {
	private double angle = 0.0; // Camera heading
	private double field_of_view = 43 * (Math.PI / 180); // F.O.V in radians
	private boolean limit_view = false;
	public Buffer positioningBuffer, trackerBuffer;
	private Actor cat, mouse;
	private LandmarkList landmarks;

	// TODO: Connect sensor to buffers (let actors subscribe?)
	
	public SensorHandler(LandmarkList landmarks, Actor mouse) {
		this.landmarks = landmarks;
		this.mouse = mouse;
	}

	public void update() {
		double x = cat.getX();
		double y = cat.getY();
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
