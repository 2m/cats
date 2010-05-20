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
	public float D1 = (float) 1.0; // Opitmal distance from mouse
	public float D2 = (float) 0.30; // Min distance to cats
	public float D3 = (float) 0.30; // Min distance from line of sight
	public float D4 = (float) 0.10; // Min distance from arena edges
	// Weights on the different parts of the criterion function
	public float W1 = (float) 0.5;
	public float W2 = (float) 1.0;
	public float W3 = (float) 0.8;
	public float W4 = (float) 1.0;

	public Guide(int id, BillBoard billboard) {
		this.billboard = billboard;
		this.id = id;
		myPos = new float[2];
		mousePos = new float[2];
		otherCats = new float[billboard.getNoCats() - 1][2];
		System.out.println("Guide with id " + id + " initialised");
	}

	private void getDataFromNetwork() {
		// Get the position of all cats
		float[] sightings = billboard.getLatestSightings();
		int j = 0;
		for (int i = 1; i <= billboard.getNoCats(); i++) {
			if (i == id) {
				myPos[0] = sightings[(i - 1) * 4];
				myPos[1] = sightings[(i - 1) * 4 + 1];

			} else {
				otherCats[j][0] = sightings[(i - 1) * 4];
				otherCats[j][1] = sightings[(i - 1) * 4 + 1];
				j++;
			}

		}
		// Get mouse position if variance is small enough
		float[] meanAndCoVariance = billboard.getMeanAndCovariance();
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

	public float[] getGradient(float x, float y) {
		float[] ret = new float[2];
		float[][] list = new float[5][2];
		float d = (float) 0.1;
		/*
		 * list[0][0] = (float) x; list[0][1] = (float) y;
		 */
		list[1][0] = (float) x + d;
		list[1][1] = (float) y;
		list[2][0] = (float) x - d;
		list[2][1] = (float) y;
		list[3][0] = (float) x;
		list[3][1] = (float) y + d;
		list[4][0] = (float) x;
		list[4][1] = (float) y - d;
		float[] f = sampleList(list);
		ret[0] = f[1] - f[2];
		ret[1] = f[3] - f[4];
		return ret;
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
		float a = (1 - W1) + W1 * keepDistanceFromCats(x, y);
		float b = (1 - W2) + W2 * keepDistanceFromMouse(x, y);
		float c = (1 - W3) + W3 * keepDistanceFromEdges(x, y);
		float d = (1 - W4) + W4 * avoidLineOfSight(x, y);
		if (haveMousePosition) {
			return a * b * c * d;
		} else {
			return a * b;
		}
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
		float maxT = (float) Math.sqrt(Math.pow(D2, 2) + Math.pow(D2, 2));
		float ret = (float) 1.0;
		for (int i = 0; i < otherCats.length; i++) {
			if ((otherCats[i][0] >= 0) && ((otherCats[i][1] >= 0))) {
				// T = sqrt((c(g, 1) - X).^2 + (c(g, 2) - Y).^2);
				float T = (float) Math.sqrt(Math.pow(otherCats[i][0] - x, 2)
						+ Math.pow(otherCats[i][1] - y, 2));
				// T = T.*(T<d2) + d2*(1 + 0.5*T./max(max(T))).*(T>=d2);
				// Z2(:, :, g) = T/max(max(T));
				if (T < D2) {
					ret *= T / maxT;
				} else {
					ret *= (float) 1.0;
					// ret = (float) (D2 * (1 + 0.5 * T / maxT));
				}
			}

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
		float ret = (float) 1.0;
		if ((x - Arena.min_x) < D4) {
			ret *= (x - Arena.min_x) / D4;
		} else if ((Arena.max_x - x) < D4) {
			ret *= (Arena.max_x - x) / D4;
		}
		if ((y - Arena.min_y) < D4) {
			ret *= (y - Arena.min_y) / D4;
		} else if ((Arena.max_y - y) < D4) {
			ret *= (Arena.max_y - y) / D4;
		}
		if (ret < 0) {
			ret = (float) 0.0;
		}
		return ret;
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
		float maxT = (float) Math.sqrt(Math.pow(D3, 2) + Math.pow(D3, 2));
		float Z3 = (float) 1.0;
		for (int i = 0; i < otherCats.length; i++) {
			if ((otherCats[i][0] >= 0) && ((otherCats[i][1] >= 0))) {
				// v = m' - c(g, :)';
				float v1 = mousePos[0] - otherCats[i][0];
				float v2 = mousePos[1] - otherCats[i][1];
				// v = v/norm(v); % Direction to mouse
				float vnorm = (float) Math.sqrt(Math.pow(v1, 2)
						+ Math.pow(v2, 2));
				v1 /= vnorm;
				v2 /= vnorm;
				// u = [v(2); -v(1)]; % Perpendicular to v
				// T = abs((u(1)*(X - c(g, 1))) + (u(2)*(Y - c(g, 2))));
				float T = Math.abs((v2 * (x - otherCats[i][0]))
						+ (-v1 * (y - otherCats[i][1])));
				// T = T.*(T<d3) + d3*(T>=d3);
				// Z3(:, :, g) = T/max(max(T));
				if (T >= D3) {
					Z3 *= (D3 / maxT);
				} else {
					Z3 *= (T / maxT);
				}
			}
		}
		return Z3;
	}

	/*
	 * Draw data (NOT brick material)
	 */
	public void draw(Graphics g) {
		final int size = 4; // Diameter
		// final int linelength = 8;

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

		// TODO: Find a better way to give feedback here
		g2.setColor(Color.magenta);
		for (int i = 0; i < billboard.getNoCats() - 1; i++) {
			ix = Actor.e2gX(otherCats[i][0]);
			iy = Actor.e2gY(otherCats[i][1]);
			/*
			 * g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int)
			 * size, (int) size);
			 */
		}

		for (float radius = (float) 0.1; radius <= 0.35; radius += 0.1) {
			for (float theta = 0; theta < 2 * Math.PI; theta += 0.3) {
				float x = (float) (myPos[0] + Math.cos(theta) * radius);
				float y = (float) (myPos[1] + Math.sin(theta) * radius);
				ix = Actor.e2gX(x);
				iy = Actor.e2gY(y);
				float s = sample(x, y);
				if (s >= 1f) {
					s = 1.0f;
				}
				g2.setColor(new Color(s, (float) 0.0, (float) 0.0));
				/*
				 * g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2),
				 * (int) size, (int) size);
				 */
			}
		}
		// Reset the transformation matrix
		g2.setTransform(oldTransform);
	}
}
