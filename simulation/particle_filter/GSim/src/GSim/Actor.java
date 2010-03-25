package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Actor {
	public static final int CAT = 1;
	public static final int MOUSE = 2;
	public static final int LANDMARK = 3;

	private double gotox;
	private double gotoy;
	private boolean marked = false;
	private int type;
	private buffer motorBuffer = new bufferFIFO();
	private realTimeClock clock = new realTimeClock();
	private sensorHandler sensor;
	private motorControl motor;

	public Actor(Actor mouse, double tx, double ty, double tangle, int ttype) {
		motor = new motorControl(tx, ty, tangle, motorBuffer, clock);
		sensor = new sensorHandler(motor);
		type = ttype;
		gotox = tx;
		gotoy = ty;
	}

	public double getX() {
		return motor.getX();
	}

	public double getY() {
		return motor.getY();
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
		final int size = 5; // Diameter
		final int linelength = 12;
		int ix = e2gX(motor.getX());
		int iy = e2gY(motor.getY());
		double iangle = -motor.getAngle() / 2;

		Graphics2D g2 = (Graphics2D) g;

		// Save the current tranform
		AffineTransform oldTransform = g2.getTransform();

		// Rotate and translate the actor
		g2.rotate(iangle, ix, iy);
		if (type == CAT) {
			if (marked) {
				g2.setColor(Color.green);
			} else {
				g2.setColor(Color.red);
			}
		} else if (type == MOUSE) {
			g2.setColor(Color.blue);
		} else if (type == LANDMARK) {
			g2.setColor(Color.gray);
		}

		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);

		if (type != LANDMARK) {
			g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
					* linelength), (int) (iy + Math.sin(iangle) * linelength));
		}

		// Reset the tranformation matrix
		g2.setTransform(oldTransform);
	}

}
