import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

// Note: "extends JFrame" enables Graphics
@SuppressWarnings("serial")

public class GSim extends JFrame {
	public static final int WORLD_WIDTH = 400;
	public static final int WORLD_HEIGHT = 400;
	private Actor t;

	public GSim() {
		t = new Cat();
	}

	// Open the graphics window
	private void openWindow() {
		setContentPane(new JPanel() {
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				paintActors(g);
			}
		}
					  );
		setVisible(true);
		setResizable(false);
		setSize(WORLD_WIDTH+10, WORLD_HEIGHT+40);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ?
	}

	private void paintActors(Graphics g) {
		t.draw(g);
	}

	// Pause the excution this many milliseconds
	private boolean sleep(long millis) {
		if (Thread.interrupted()) return false;

		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			return false;
		}

		return true;
	}

	public void run() {
		openWindow();
		for (int i = 1; i <10; i++) {
			repaint();
			sleep(100);
		}
	}

	public static void main(String [] arg) {
		GSim sim = new GSim();
		sim.run();
	}
}
