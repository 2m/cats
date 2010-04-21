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

	// TODO: Connect sensor to buffers (let actors subscribe?)
	
	public SensorHandler(LandmarkList landmarks, Actor mouse) {
		//this.targets = targets;
	}

	public void update() {

	}

	public regCat(Actor cat) {}
	public regMouse(Actor mouse) {}

}
