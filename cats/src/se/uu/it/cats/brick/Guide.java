package se.uu.it.cats.brick;

import se.uu.it.cats.brick.storage.BillBoard;

public class Guide {// implements Runnable {

	public static float[] advice = new float[2];

	private BillBoard billboard;
	private int id;
	private float[] myPos;
	private float[] mousePos;
	private float[][] otherCats;
	private boolean haveMousePosition = false;
	private final float h = (float) 0.05;

	public final float D1 = (float) 0.90; // Optimal distance from mouse
	public final float D2 = (float) 0.45; // Minimal distance to objects
	public final float D3 = (float) 0.30; // Minimal distance from line of sight
	public final float D4 = (float) 0.15; // Minimal distance from arena edges
	public final float D5 = (float) 0.25; // Mouse distance "plateau" size
	// Weights on the different parts of the criterion function
	public final float W1 = (float) 1.0; // Importance of distance from cats
	public final float W2 = (float) 0.8; // Importance of distance from mouse
	public final float W3 = (float) 1.0; // Importance of distance from edges
	public final float W4 = (float) 0.8; // Importance of distance from LOS
	public final float W5 = (float) 1.0; // Importance of distance from landm.

	public final float maxMovement = (float) Math.pow(0.20, 2);
	public final float minMovement = (float) Math.pow(0.10, 2);

	public Guide(int id, BillBoard billboard) {
		this.billboard = billboard;
		this.id = id;
		myPos = new float[2];
		mousePos = new float[2];
		otherCats = new float[billboard.getNoCats() - 1][2];
	}

	public float[] getAdvice() {
		getDataFromNetwork();
		float[] advice = new float[2];
		advice[0] = -1.0f;
		advice[1] = -1.0f;
		float x = myPos[0];
		float y = myPos[1];
		float diffx = 0, diffy = 0;
		if (x > -1.0f) {
			int i = 0;
			while ((i < 40) && ((diffx * diffx + diffy * diffy) < maxMovement)) {
				float[] grad = getGradient(x, y);
				x += 0.01 * Math.signum(grad[0]);
				y += 0.01 * Math.signum(grad[1]);
				diffx = Math.abs(myPos[0] - x);
				diffy = Math.abs(myPos[1] - y);
				i++;
			}
			if ((diffx * diffx + diffy * diffy) > minMovement) {
				advice[0] = x;
				advice[1] = y;
			}
		}
		return advice;
	}

	private void getDataFromNetwork() {
		// Get the position of all cats
		float[] positions = billboard.getAbsolutePositions();
		int j = 0;
		for (int i = 0; i < billboard.getNoCats(); i++) {
			if (i == id) {
				myPos[0] = positions[i * 4];
				myPos[1] = positions[i * 4 + 1];
			} else {
				otherCats[j][0] = positions[i * 4];
				otherCats[j][1] = positions[i * 4 + 1];
				j++;
			}

		}
		// Get mouse position if variance is small enough and weight>0
		float[] meanAndCoVariance = billboard.getMeanAndCovariance();
		float maxVariance = 0.5f;
		if ((meanAndCoVariance[4] < maxVariance)
				&& (meanAndCoVariance[6] < maxVariance)
				&& (meanAndCoVariance[10] > 0)) {
			mousePos[0] = meanAndCoVariance[0];
			mousePos[1] = meanAndCoVariance[1];
			haveMousePosition = true;
		} else {
			haveMousePosition = false;
		}
	}

	public float[] getGradient(float x, float y) {
		// Finite difference approximation of the gradient and (x, y)
		float[] ret = new float[2];
		ret[0] = (sample(x + h, y) - sample(x - h, y));// / (2 * h);
		ret[1] = (sample(x, y + h) - sample(x, y - h));// / (2 * h);
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
		float a = (1 - W1) + W1 * keepDistanceFromCats(x, y);
		float b = (1 - W3) + W3 * keepDistanceFromEdges(x, y);
		float c = (1 - W5) + W5 * keepDistanceFromLandmarks(x, y);
		if (haveMousePosition) {
			float d = (1 - W2) + W2 * keepDistanceFromMouse(x, y);
			float e = (1 - W4) + W4 * avoidLineOfSight(x, y);
			return a * b * c * d * e;
		} else {
			return a * b * c;
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
		float ret = (float) 1.0;
		for (int i = 0; i < otherCats.length; i++) {
			if ((otherCats[i][0] >= 0) && ((otherCats[i][1] >= 0))) {
				ret *= keepDistanceFromObject(otherCats[i][0], otherCats[i][1],
						x, y);
			}

		}
		return ret;
	}

	private float keepDistanceFromObject(float x0, float y0, float x1, float y1) {
		final float D2manhattan = D2 * 1.5f; // D2*sqrt(2)
		float diffx = Math.abs(x0 - x1);
		float diffy = Math.abs(y0 - y1);
		if ((diffx + diffy) < D2manhattan) {
			float distanceSquared = diffx * diffx + diffy * diffy;
			if (distanceSquared < (D2 * D2)) {
				return (float) (Math.sqrt(distanceSquared) / D2);
			}
		}
		return (float) 1.0;

	}

	/**
	 * Get the component of the criterion function that handles the distance
	 * from landmarks.
	 * 
	 * @param x
	 *            position in x direction
	 * @param y
	 *            position in y direction
	 */
	private float keepDistanceFromLandmarks(float x, float y) {
		float ret = (float) 1.0;
		for (int i = 0; i < Settings.NO_LANDMARKS; i++) {
			ret *= keepDistanceFromObject(Settings.LANDMARK_POSITION[i][0],
					Settings.LANDMARK_POSITION[i][1], x, y);
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
		final float maxZ1 = (float) Math.sqrt(Math.pow(Settings.ARENA_MAX_X
				- Settings.ARENA_MIN_X, 2)
				+ Math.pow(Settings.ARENA_MAX_Y - Settings.ARENA_MIN_Y, 2));
		float Z1 = (float) Math.abs(Math.sqrt((mousePos[0] - x)
				* (mousePos[0] - x) + (mousePos[1] - y) * (mousePos[1] - y))
				- D1);
		Z1 -= D5;
		if (Z1 < 0) {
			Z1 = 0;
		}
		Z1 = 1 - Z1 / maxZ1;
		return Z1 * keepDistanceFromObject(mousePos[0], mousePos[1], x, y);
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
		float ret = 1.0f;
		// Function in x direction
		if ((x - Settings.ARENA_MIN_X) < D4) {
			ret = (x - Settings.ARENA_MIN_X) / D4;
		} else if ((Settings.ARENA_MAX_X - x) < D4) {
			ret = (Settings.ARENA_MAX_X - x) / D4;
		}
		// Function in y direction
		if ((y - Settings.ARENA_MIN_Y) < D4) {
			ret *= (y - Settings.ARENA_MIN_Y) / D4;
		} else if ((Settings.ARENA_MAX_Y - y) < D4) {
			ret *= (Settings.ARENA_MAX_Y - y) / D4;
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
		final float maxT = (float) Math.sqrt(Math.pow(D3, 2) + Math.pow(D3, 2));
		float Z3 = (D3 / maxT);
		for (int i = 0; i < otherCats.length; i++) {
			if ((otherCats[i][0] >= 0) && ((otherCats[i][1] >= 0))) {
				// v = m' - c(g, :)';
				float v1 = mousePos[0] - otherCats[i][0];
				float v2 = mousePos[1] - otherCats[i][1];
				// v = v/norm(v); % Direction to mouse
				float vnorm = (float) Math.sqrt(v1 * v1 + v2 * v2);
				v1 /= vnorm;
				v2 /= vnorm;
				// u = [v(2); -v(1)]; % Perpendicular to v
				// T = abs((u(1)*(X - c(g, 1))) + (u(2)*(Y - c(g, 2))));
				float T = Math.abs((v2 * (x - otherCats[i][0]))
						+ (-v1 * (y - otherCats[i][1])));
				// T = T.*(T<d3) + d3*(T>=d3);
				// Z3(:, :, g) = T/max(max(T));
				if (T < D3) {
					Z3 = (T / maxT);
				}
			}
		}
		return Z3;
	}

	/*
	 * @Override public void run() { // TODO Auto-generated method stub while
	 * (true) { long time = Clock.timestamp(); advice = getAdvice();
	 * Logger.println("Guide time: " + (Clock.timestamp() - time)); try {
	 * Thread.sleep(1500); } catch (Exception ex) { } }
	 * 
	 * }
	 */
}
