import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Actor {
	public static final int CAT = 1;
	public static final int MOUSE = 2;
	public int type;
	public MotorControl motor;
	
	public Actor() {
		motor = new MotorControl(0,0,0);
	}

	public Actor(double tx, double ty, double tangle, int ttype) {
		motor = new MotorControl(tx,ty,tangle);
		type = ttype;
	}
	
	// Draw an Actor
	public void draw(Graphics g) {
		int ix = (int) motor.getX();
		int iy = (int) (GSim.WORLD_HEIGHT - motor.getY());
		double iangle = motor.getAngle();

		Graphics2D g2 = (Graphics2D) g;

		// Save the current tranform
		AffineTransform oldTransform = g2.getTransform();

		// Rotate and translate the actor
		g2.rotate(iangle, ix, iy);

		int size = 15; // Diameter

		if (type == CAT)
			g2.setColor(Color.red);
		else if (type == MOUSE)
			g2.setColor(Color.gray);

		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);

		int linelength = 12;
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
				* linelength), (int) (iy + Math.sin(iangle) * linelength));

		// Reset the tranformation matrix
		g2.setTransform(oldTransform);
	}

}
