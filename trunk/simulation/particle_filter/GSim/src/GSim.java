/*
 * GSim.java
 * Version 0.1
 * Date 2010-02-16
 */

import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")

/** Main class of simulation. Initialises graphics and objects then runs
 * a main loop.
 * @version 0.1
 * @author 	Fredrik Wahlberg
 */
public class GSim extends JFrame {
	public static final int WORLD_WIDTH = 400;
	public static final int WORLD_HEIGHT = 400;
	private Actor t;

	public GSim() {
		t = new Cat();
		// TODO: Create more actors
	}

	/**
	 *  Open the graphics window and set up painting hooks.
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
		setSize(WORLD_WIDTH + 10, WORLD_HEIGHT + 40);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void paintActors(Graphics g) {
		// TODO: Add loop for actors.draw
		t.draw(g);
	}

	/**
	 *  Pause the excution this many milliseconds
	 * 
	 * @param millis to pause as a long int
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
		openWindow();
		while (true) {
			// TODO: Call run() or similar in cat
			repaint();
			sleep(100); // Wait before new update
		}
	}

	public static void main(String[] arg) {
		GSim sim = new GSim();
		sim.run();
	}
}
