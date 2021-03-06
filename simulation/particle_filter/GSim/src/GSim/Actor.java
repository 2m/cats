package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Base class for all actors on the arena (cats, mice and landmarks)
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class Actor {
	public static final int CAT = 1;
	public static final int MOUSE = 2;
	public static final int LANDMARK = 3;

	protected double gotox;
	protected double gotoy;
	private boolean marked = false;
	private int type;
	protected Buffer motorBuffer = new BufferSorted();

	protected MotorControl motor = null;
	protected SensorHandler sensors;
	protected int iter;
	protected BillBoard billboard;

	protected int id;
	
	public boolean turnMode = true;

	public Actor(Actor mouse, double tx, double ty, double tangle, int ttype,
			BillBoard billboard, int id) {
		motor = new MotorControl(tx, ty, tangle, motorBuffer);
		this.id = id;
		type = ttype;
		gotox = tx;
		gotoy = ty;
		this.billboard = billboard;
	}

	public double getObjectiveX() {
		return motor.getX();
	}

	public double getObjectiveY() {
		return motor.getY();
	}

	public double getObjectiveAngle() {
		return motor.getAngle();
	}

	public double getX() {
		System.out.println("Bad use of getX()");
		return 0.0;
	}

	public double getY() {
		System.out.println("Bad use of getY()");
		return 0.0;
	}

	public double getAngle() {
		System.out.println("Bad use of getAngle()");
		return 0.0;
	}

	public void goTo(double x, double y) {
		gotox = x;
		gotoy = y;
	}

	/**
	 * Update the actor This should be overloaded
	 */
	public void update() {
		motor.goTo(gotox, gotoy);
		iter++;
	}

	/**
	 * Set actors as marked by mouse input
	 * 
	 */
	public void mark() {
		marked = true;
	}

	/**
	 * Set actors as unmarked by mouse input
	 * 
	 */
	public void unmark() {
		marked = false;
	}

	/**
	 * See if actor is marked
	 * 
	 */
	public boolean marked() {
		return marked;
	}

	public boolean isMouse() {
		return type == MOUSE;
	}

	public boolean isCat() {
		return type == CAT;
	}

	public boolean isLandmark() {
		return type == LANDMARK;
	}

	public static double g2eX(int x) {
		return ((double) (x - 3) * (GSim.ARENA_WIDTH / GSim.WINDOW_WIDTH));
	}

	public static double g2eY(int y) {
		return GSim.ARENA_HEIGHT - (y - 25)
				* (GSim.ARENA_HEIGHT / GSim.WINDOW_HEIGHT);
	}

	public static int e2gX(double x) {
		return (int) (x * (GSim.WINDOW_WIDTH / GSim.ARENA_WIDTH));
	}

	public static int e2gY(double y) {
		return (int) (GSim.WINDOW_HEIGHT - (y * (GSim.WINDOW_HEIGHT / GSim.ARENA_HEIGHT)));
	}

	/*
	 * Draw an Actor Code for drawing the position and angle of the actor.
	 */
	public void draw(Graphics g) {
		// Hook for filter graphics code
		drawMore(g);

		int size = 5; // Diameter
		int linelength = 12;
		int ix = e2gX(motor.getX());
		int iy = e2gY(motor.getY());
		double iangle = -motor.getAngle();

		Graphics2D g2 = (Graphics2D) g;

		if (marked) {
			g2.setColor(Color.green);
		} else if (type == CAT) {
			g2.setColor(Color.red);
		} else if (type == MOUSE) {
			g2.setColor(Color.blue);
		}

		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
				* linelength), (int) (iy + Math.sin(iangle) * linelength));
	}

	public void drawMore(Graphics g) {
	}
}
