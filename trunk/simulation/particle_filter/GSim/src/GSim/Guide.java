package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Guide {
	private BillBoard billboard;
	private int id;
	private float[] myPos;
	private float[] mousePos;
	private float[][] otherCats;
	private boolean haveMousePosition = false;
	public int D1 = 80; // Opitmal distance from mouse
	public int D2 = 30; // Min distance to cats
	public int D3 = 20; // Min distance from line of sight

	public Guide(int id, BillBoard billboard) {
		this.billboard = billboard;
		this.id = id;
		myPos = new float[2];
		mousePos = new float[2];
		otherCats = new float[billboard.getNoCats() - 1][2];
	}

	private void getDataFromNetwork() {
		// Get the position of all cats
		float[] sightings = billboard.getLatestSightings();
		int j = 0;
		for (int i = 1; i <= billboard.getNoCats(); i++) {
			if (i == id) {
				myPos[0] = sightings[(i - 1) * 3];
				myPos[1] = sightings[(i - 1) * 3 + 1];

			} else {
				otherCats[j][0] = sightings[(i - 1) * 3];
				otherCats[j][1] = sightings[(i - 1) * 3 + 1];
				j++;
			}

		}
		// Get mouse position if variance is small enough
		float[] meanAndCoVariance = billboard.getMeanAndCoveriance();
		float maxVariance = 1;
		if ((meanAndCoVariance[4] < maxVariance)
				&& (meanAndCoVariance[6] < maxVariance)) {
			mousePos[0] = meanAndCoVariance[0];
			mousePos[1] = meanAndCoVariance[1];
			haveMousePosition = true;
		} else {
			haveMousePosition = false;
		}
	}

	/**
	 * Get criterion function value at a list of points in space.
	 * 
	 * @param coordinates
	 *            positions in space as an array of floats
	 */
	public float[] sampleList(float[][] coordinates) {
		getDataFromNetwork();
		float[] ret = new float[coordinates.length];
		for (int i = 0; i < coordinates.length; i++) {
			float x = coordinates[i][0];
			float y = coordinates[i][1];
			ret[i] = sample(x, y);
		}
		return ret;
	}

	/**
	 * Get criterion function value at a point in space.
	 * 
	 * @param x
	 *            position in x direction
	 * @param y
	 *            position in y direction
	 */
	public float sample(float x, float y) {
		getDataFromNetwork();
		return keepDistanceFromCats(x, y) * keepDistanceFromMouse(x, y)
				* keepDistanceFromEdges(x, y) * avoidLineOfSight(x, y);
	}

	/**
	 * Get the component of the criterion function that handles the distance
	 * from other cats.
	 * 
	 * @param x
	 *            position in x direction
	 * @param y
	 *            position in y direction
	 */
	private float keepDistanceFromCats(float x, float y) {
		// TODO: Verify keepDistanceFromCats
		float maxT = (float) Math.sqrt(Math.pow(Arena.max_x, 2)
				+ Math.pow(Arena.max_x, 2));
		float ret = (float) 1.0;
		for (int i = 0; i < otherCats.length; i++) {
			// T = sqrt((c(g, 1) - X).^2 + (c(g, 2) - Y).^2);
			float T = (float) Math.sqrt(Math.pow(otherCats[i][0] - x, 2)
					+ Math.pow(otherCats[i][1] - y, 2));
			// T = T.*(T<d2) + d2*(1 + 0.5*T./max(max(T))).*(T>=d2);
			if (T < D2) {
				ret = T;
			} else {
				ret = (float) (D2 * (1 + 0.5 * T / maxT));
			}
			// Z2(:, :, g) = T/max(max(T));
			ret *= (T / maxT);
		}
		return ret;
	}

	/**
	 * Get the component of the criterion function that handles the distance
	 * from the mouse.
	 * 
	 * @param x
	 *            position in x direction
	 * @param y
	 *            position in y direction
	 */
	private float keepDistanceFromMouse(float x, float y) {
		// TODO: Verify keepDistanceFromMouse
		float maxZ1 = (float) Math.sqrt(Math.pow(Arena.max_x, 2)
				+ Math.pow(Arena.max_x, 2));
		// Z1 = abs(sqrt((m(1) - X).^2 + (m(2) - Y).^2) - d1);
		float Z1 = (float) Math.abs(Math.sqrt(Math.pow(mousePos[0] - x, 2)
				+ Math.pow(mousePos[1] - y, 2))
				- D1);
		// Z1 = 1 - Z1/max(max(Z1));
		Z1 = 1 - Z1 / maxZ1;
		return Z1;
	}

	/**
	 * Get the component of the criterion function that handles the distance
	 * from the edges.
	 * 
	 * @param x
	 *            position in x direction
	 * @param y
	 *            position in y direction
	 */
	private float keepDistanceFromEdges(float x, float y) {
		// TODO: Implement keepDistanceFromEdges
		return (float) 1.0;
	}

	/**
	 * Get the component of the criterion function that handles the distance
	 * from the other cats' line of sight to the mouse.
	 * 
	 * @param x
	 *            position in x direction
	 * @param y
	 *            position in y direction
	 */
	private float avoidLineOfSight(float x, float y) {
		// TODO: Verify avoidLineOfSight
		float maxT = (float) Math.sqrt(Math.pow(Arena.max_x, 2)
				+ Math.pow(Arena.max_x, 2));
		float Z3 = (float) 1.0;
		for (int i = 0; i < otherCats.length; i++) {
			// v = m' - c(g, :)';
			float v1 = mousePos[0] - otherCats[i][0];
			float v2 = mousePos[1] - otherCats[i][1];
			// v = v/norm(v); % Direction to mouse
			float vnorm = (float) Math.sqrt(Math.pow(v1, 2) + Math.pow(v2, 2));
			v1 /= vnorm;
			v2 /= vnorm;
			// u = [v(2); -v(1)]; % Perpendicular to v
			// T = abs((u(1)*(X - c(g, 1))) + (u(2)*(Y - c(g, 2))));
			float T = Math.abs((v2 * (x - otherCats[i][0]))
					+ (v1 * (y - otherCats[i][1])));
			// T = T.*(T<d3) + d3*(T>=d3);
			// Z3(:, :, g) = T/max(max(T));
			if (T >= D3) {
				Z3 *= (D3 / maxT);
			} else {
				Z3 *= (T / maxT);
			}
		}
		return Z3;
	}

	/*
	 * Draw data (NOT brick material)
	 */
	public void draw(Graphics g) {
		// TODO: Remove graphics code from filter
		final int size = 4; // Diameter
		final int linelength = 8;

		Graphics2D g2 = (Graphics2D) g;

		// Save the current tranform
		AffineTransform oldTransform = g2.getTransform();

		// Rotate and translate the actor
		// g2.rotate(iangle, ix, iy);

		// Plot mean
		g2.setColor(Color.cyan);
		int ix = Actor.e2gX(myPos[0]);
		int iy = Actor.e2gY(myPos[1]);
		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);

		g2.setColor(Color.magenta);
		for (int i = 0; i < billboard.getNoCats() - 1; i++) {
			ix = Actor.e2gX(otherCats[i][0]);
			iy = Actor.e2gY(otherCats[i][1]);
			g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2),
					(int) size, (int) size);
		}
		float radius = (float) 0.3;
		for (float theta = 0; theta < 2 * Math.PI; theta += 0.05) {
			float x = (float) (myPos[0] + Math.cos(theta) * radius);
			float y = (float) (myPos[1] + Math.sin(theta) * radius);
			ix = Actor.e2gX(x);
			iy = Actor.e2gY(y);
			g2.setColor(new Color(sample(x, y), (float) 0.0, (float) 0.0));
			g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2),
					(int) size, (int) size);

		}
		// Reset the transformation matrix
		g2.setTransform(oldTransform);
	}

}
