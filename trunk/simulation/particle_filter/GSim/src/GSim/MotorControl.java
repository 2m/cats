package GSim;
public class MotorControl {
	public double x;
	public double y;
	public double angle;

	public void drive(double distance) {
		x += Math.cos(angle) * distance;
		y += Math.sin(angle) * distance;
	}

	public void turn(double turnangle) {
		angle += turnangle;
	}

	public void setPos(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public MotorControl() {
	}

	public MotorControl(double x, double y, double angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
	}

}
