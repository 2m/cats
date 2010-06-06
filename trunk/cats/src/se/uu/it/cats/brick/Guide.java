package se.uu.it.cats.brick;

import se.uu.it.cats.brick.storage.BillBoard;

public class Guide {
	private BillBoard billboard;
	private int id;
	private float[] myPos;
	private float[] mousePos;
	private float[][] otherCats;
	private boolean haveMousePosition = false;
	private final float h = (float) 0.05;

	public float D1 = (float) 1.0; // Optimal distance from mouse
	public float D2 = (float) 0.40; // Minimal distance to cats
	public float D3 = (float) 0.30; // Minimal distance from line of sight
	public float D4 = (float) 0.25; // Minimal distance from arena edges
	public float D5 = (float) 0.1; // Mouse distance "plateau" size
	// Weights on the different parts of the criterion function
	public float W1 = (float) 1.0; // Importance of distance from cats
	public float W2 = (float) 0.8; // Importance of distance from mouse
	public float W3 = (float) 1.0; // Importance of distance from edges
	public float W4 = (float) 0.8; // LOS

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
		if (x >= 0) {
			int i = 0;
			while ((i < 40)
					&& (Math.sqrt(pow2(myPos[0] - x)
							+ pow2(myPos[1] - y)) < 0.25f)) {
				float[] grad = getGradient(x, y);
				x += 0.01 * Math.signum(grad[0]);
				y += 0.01 * Math.signum(grad[1]);
				i++;
			}
			Logger.println("i:"+i+" myPos[0]:"+myPos[0]+" myPos[1]"+myPos[1]+" x"+x+" y"+y);
			Logger.println("Inside guide:"+Math.sqrt(pow2(myPos[0] - x) + pow2(myPos[1] - y)));
			Logger.println(pow2(myPos[0] - x)+", "+pow2(myPos[1] - y));
			if (Math
					.sqrt(pow2(myPos[0] - x) + pow2(myPos[1] - y)) > 0.15f) {
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
		float maxVariance = 1;
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
		ret[0] = (sample(x + h, y) - sample(x - h, y)) / (2 * h);
		ret[1] = (sample(x, y + h) - sample(x, y - h)) / (2 * h);
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
		float c = (1 - W3) + W3 * keepDistanceFromEdges(x, y);
		
		//float c = (1 - W5) + W5 * keepDistanceFromLandmarks(x, y);
		// FIXME: Implement keedDistanceFromLandmarks 
		
		if (haveMousePosition) {
			float b = (1 - W2) + W2 * keepDistanceFromMouse(x, y);
			float d = (1 - W4) + W4 * avoidLineOfSight(x, y);
			return a * b * c * d;
		} else {
			return a * c;
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
		// TODO: Optimise this
		float maxT = (float) Math.sqrt(pow2(D2) + pow2(D2));
		float ret = (float) 1.0;
		for (int i = 0; i < otherCats.length; i++) {
			if ((otherCats[i][0] >= 0) && ((otherCats[i][1] >= 0))) {
				// T = sqrt((c(g, 1) - X).^2 + (c(g, 2) - Y).^2);
				float T = (float) Math.sqrt(pow2(otherCats[i][0] - x)
						+ pow2(otherCats[i][1] - y));
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
		// TODO: Optimise this
		float maxZ1 = (float) Math.sqrt(pow2(Settings.ARENA_MAX_X
				- Settings.ARENA_MIN_X)
				+ pow2(Settings.ARENA_MAX_Y - Settings.ARENA_MIN_Y));
		// Z1 = abs(sqrt((m(1) - X).^2 + (m(2) - Y).^2) - d1);
		float Z1 = (float) Math.abs(Math.sqrt(pow2(mousePos[0] - x)
				+ pow2(mousePos[1] - y))
				- D1);
		Z1 -= D5;
		if (Z1 < 0) {
			Z1 = 0;
		}
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
		// TODO: Optimise this
		float ret = (float) 1.0;
		if ((x - Settings.ARENA_MIN_X) < D4) {
			ret *= (x - Settings.ARENA_MIN_X) / D4;
		} else if ((Settings.ARENA_MAX_X - x) < D4) {
			ret *= (Settings.ARENA_MAX_X - x) / D4;
		}
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
		// TODO: Optimise this
		float maxT = (float) Math.sqrt(pow2(D3) + pow2(D3));
		float Z3 = (float) 1.0;
		for (int i = 0; i < otherCats.length; i++) {
			if ((otherCats[i][0] >= 0) && ((otherCats[i][1] >= 0))) {
				// v = m' - c(g, :)';
				float v1 = mousePos[0] - otherCats[i][0];
				float v2 = mousePos[1] - otherCats[i][1];
				// v = v/norm(v); % Direction to mouse
				float vnorm = (float) Math.sqrt(pow2(v1)
						+ pow2(v2));
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
	
	private float pow2(float x) {
		return x*x;
	}
	
}
