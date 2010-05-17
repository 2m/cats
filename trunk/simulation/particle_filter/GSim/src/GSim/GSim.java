package GSim;

/*
 * GSim.java
 * Version $Rev$
 * Date 2010-02-16
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
/** Main class of simulation. Initialises graphics and objects then runs
 * a main loop.
 * @version 0.1
 * @author 	Fredrik Wahlberg
 */
public class GSim extends JFrame implements MouseListener {
	// Size of window
	public static final int WINDOW_WIDTH = 400;
	public static final int WINDOW_HEIGHT = 400;

	// Size of arena
	public static final double ARENA_WIDTH = 3;
	public static final double ARENA_HEIGHT = 3;

	// Time between updates in milliseconds
	public static final int timestep = 100;

	// Array with all the actors
	private Actor[] actors = new Actor[1 + 3];// [1 + 3];
	private boolean marked = false;
	public LandmarkList llist = new LandmarkList();
	public RealTimeClock clock = new RealTimeClock();
	public BillBoard billboard = new BillBoard(3);

	public GSim() {
		addMouseListener(this);

		/*
		 * actors[0] = new Mouse(null, 1.5, 1.5, 0.0, clock, billboard, 0);
		 * actors[1] = new Cat(actors[0], 0.1, 0.1, Math.PI / 6, clock,
		 * billboard, 1);
		 */

		actors[0] = new Mouse(null, 1.5, 1.5, 0.0, clock, billboard, 0);
		actors[1] = new Cat(actors[0], 0.1, 0.1, Math.PI / 6, clock, billboard,
				1);
		actors[2] = new Cat(actors[0], 1.0, 1.0, 0, clock, billboard, 2);
		actors[3] = new Cat(actors[0], 1.5, 0.1, 0, clock, billboard, 3);

	}

	/**
	 * Open the graphics window and set up painting hooks.
	 */
	private void openWindow() {
		setContentPane(new JPanel() {
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				paintActors(g);
			}
		});
		setVisible(true);
		setResizable(false);
		setSize(WINDOW_WIDTH + 10, WINDOW_HEIGHT + 40);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void paintActors(Graphics g) {
		for (int i = 0; i < actors.length; i++) {
			actors[i].draw(g);
		}
		// Draw landmarks
		for (int i = 0; i < LandmarkList.landmarkX.length; i++) {
			final int size = 5; // Diameter
			int ix = Actor.e2gX((double) LandmarkList.landmarkX[i]);
			int iy = Actor.e2gY((double) LandmarkList.landmarkY[i]);
			Graphics2D g2 = (Graphics2D) g;

			// Save the current tranform
			AffineTransform oldTransform = g2.getTransform();

			// Rotate and translate the actor
			g2.rotate(0.0, ix, iy);

			if (LandmarkList.landmarkC[i]) {
				g2.setColor(Color.green);
			} else {
				g2.setColor(Color.red);
			}
			g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2),
					(int) size, (int) size);

			// Reset the tranformation matrix
			g2.setTransform(oldTransform);
		}
	}

	/**
	 * Pause the excution this many milliseconds
	 * 
	 * @param millis
	 *            to pause as a long int
	 * @return Result as boolean
	 */
	private boolean sleep(long millis) {
		if (Thread.interrupted())
			return false;
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	/**
	 * Main loop
	 */
	public void run() {
		openWindow(); // Open window
		while (true) { // Main loop
			// Update all actors
			for (int i = 0; i < actors.length; i++) {
				actors[i].update();
			}

			repaint(); // Redraw all objects
			sleep(timestep); // Wait before new update
		}
	}

	public void mousePressed(MouseEvent e) {
		// saySomething("Mouse pressed", e);
	}

	public void mouseReleased(MouseEvent e) {
		// saySomething("Mouse released", e);
	}

	public void mouseEntered(MouseEvent e) {
		// saySomething("Mouse entered", e);
	}

	public void mouseExited(MouseEvent e) {
		// saySomething("Mouse exited", e);
	}

	public void mouseClicked(MouseEvent e) {
		double x = Actor.g2eX(e.getX());
		double y = Actor.g2eY(e.getY());
		// System.out.println(x +", "+ y);
		if (marked) {
			for (int i = 0; i < actors.length; i++) {
				if (actors[i].marked()) {
					actors[i].goTo(x, y);
					actors[i].unmark();
				}
			}
			marked = false;
		} else {
			double dist, mindist = ARENA_WIDTH * ARENA_WIDTH + ARENA_HEIGHT
					* ARENA_HEIGHT;
			int j = 0;
			for (int i = 0; i < actors.length; i++) {
				dist = Math.pow(actors[i].getX() - x, 2)
						+ Math.pow(actors[i].getY() - y, 2);
				if (dist < mindist) {
					mindist = dist;
					j = i;
				}
			}
			actors[j].mark();
			marked = true;
		}
	}

	/**
	 * Simple main Creates GSim object and runs the thread
	 * 
	 * @param arg
	 *            input from command line
	 */
	public static void main(String[] arg) {
		GSim sim = new GSim();
		System.out.println("Simulation started");
		sim.run();
	}
}
