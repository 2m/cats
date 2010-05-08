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
	private boolean usePositioningParticleFilter = false;
	private boolean usePositioningUnscentedKalmanFilter = true;
	private boolean useTrackingParticleFilter = false;
	private boolean useTrackingUnscentedKalmanFilter = false;
	private boolean useGuide = true;
	
	public Cat(Actor mouse, double x, double y, double angle,
			RealTimeClock clock, BillBoard billboard, int id) {
		super(mouse, x, y, angle, CAT, clock, billboard, id);
		sensors = new SensorHandler(mouse, clock);
		sensors.register(this);
		positioningBuffer = sensors.regPositioner();
		trackerBuffer = sensors.regTracker();
		int N = 80;
		float T = (float) 0.5;
		
		if (usePositioningParticleFilter) {
			positioningFilter = new AbsolutePositioningParticleFilter(N, T,
					positioningBuffer, motorBuffer, clock);
		}
		else if (usePositioningUnscentedKalmanFilter){
			positioningFilter = new AbsolutePositioningUKF(T, positioningBuffer,
					 motorBuffer, clock);
		}
		
		if (useTrackingParticleFilter) {
			trackingFilter = new TrackingParticleFilter(id, N, T, trackerBuffer,
					clock, billboard);
		}
		else if (useTrackingUnscentedKalmanFilter){
			//TODO implement
			/*trackingFilter = new TrackingUKF(id, N, T, trackerBuffer,
					clock, billboard);*/
		}
		
		if (usePositioningParticleFilter || usePositioningUnscentedKalmanFilter){
			positioningFilter.initData((float) motor.getX(), (float) motor.getY(),
					(float) motor.getAngle());
		}
		if (useGuide){
			guide = new Guide(id, billboard);
		}
		
	}

	/**
	 * Update the actor This should be overloaded
	 */
	public void update() {
		motor.goTo(gotox, gotoy);
		if (useGuide){		
			float[] g = guide.getGradient((float) gotox, (float) gotoy);
			gotox += 0.01 * Math.signum(g[0]);
			gotoy += 0.01 * Math.signum(g[1]);
		}
		
		if ((iter % 5) == 0) {
			sensors.update();
			if (usePositioningParticleFilter || usePositioningUnscentedKalmanFilter){
				positioningFilter.update();
			}
			if (useTrackingParticleFilter || useTrackingUnscentedKalmanFilter){
				trackingFilter.update();
			}	
		}
		iter++;
	}

	public void drawMore(Graphics g) {
		if (usePositioningParticleFilter || usePositioningUnscentedKalmanFilter){
			positioningFilter.draw(g);
		}
		if (useTrackingParticleFilter || useTrackingUnscentedKalmanFilter){
			trackingFilter.draw(g);
		}	
		if (useGuide){
			guide.draw(g);
		}
	}
}
