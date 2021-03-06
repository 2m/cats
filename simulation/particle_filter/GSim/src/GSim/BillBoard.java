package GSim;

/**
 * Skeleton for the networking interface as seen by the filters
 * 
 * @author Fredrik Wahlberg Edited 2010-05-20 by Edvard Zak IMPORTANT: The
 *         accuracy of the timestamps on sightings/positions are currently
 *         reduced from a thousand of second (millisecond) to roughly a hundred
 *         of a second due to using a float to store the int value, this might
 *         turn out being ok though.
 */
public class BillBoard {
	
	/** Data to be synced */
	private float[][] data;
	private float[] sightings;
	/** Floats to sync per cat */
	private final int DATA_PER_CAT = 11;
	/** Number of cats (set in constructor) */
	private final int NUMBER_OF_CATS;

	public float[] position;

	public BillBoard(int noOfCats) {
		NUMBER_OF_CATS = noOfCats;
		data = new float[NUMBER_OF_CATS][DATA_PER_CAT];
		sightings = new float[NUMBER_OF_CATS * 4];
		position = new float[NUMBER_OF_CATS * 4];
		for (int i = 0; i < NUMBER_OF_CATS * 4; i++) {
			sightings[i] = -1;
			position[i] = -1;
		}
	}

	public void setLatestSighting(int id, float x, float y, float theta,
			int timestamp) {
		// id in range [1:n]
		sightings[id * 4 + 0] = x;
		sightings[id * 4 + 1] = y;
		sightings[id * 4 + 2] = theta;
		sightings[id * 4 + 3] = timestamp;
		/*
		 * if ( (int)((float)timestamp) != timestamp)
		 * System.out.println("CONVERSION ERROR IN setLatestSighting!!!!! ( " +
		 * timestamp + " != " + (float)timestamp +" )"); else
		 * System.out.println("ok conversion in setLatestSighting ( " +
		 * timestamp + " != " + (float)timestamp +" )");
		 */
	}

	public void setAbsolutePosition(int id, float x, float y, float angle,
			int timestamp) {
		position[id * 4 + 0] = x;
		position[id * 4 + 1] = y;
		position[id * 4 + 2] = angle;
		position[id * 4 + 3] = timestamp;
	}

	public float[] getAbsolutePositions() {
		return position;
	}

	public int getNoCats() {
		return NUMBER_OF_CATS;
	}

	public float[] getLatestSightings() {
		return sightings;
	}

	/** Set mean and covariance */
	public void setMeanAndCovariance(int id, float mean_x, float mean_y,
			float mean_xv, float mean_yv, float var_xx, float var_xy,
			float var_yy, float var_xvxv, float var_xvyv, float var_yvyv,
			float weight) {
		int i = id;
		data[i][0] = mean_x;
		data[i][1] = mean_y;
		data[i][2] = mean_xv;
		data[i][3] = mean_yv;
		data[i][4] = var_xx;
		data[i][5] = var_xy;
		data[i][6] = var_yy;
		data[i][7] = var_xvxv;
		data[i][8] = var_xvyv;
		data[i][9] = var_yvyv;
		data[i][10] = weight;
	}

	/**
	 * 
	 * @return ret {m_x, m_y, m'_x, m'_y, xx, xy, yy, x'x', x'y', y'y', weight}
	 */
	public float[] getMeanAndCovariance() {
		// ret = {m_x, m_y, m'_x, m'_y, xx, xy, yy, x'x', x'y', y'y', weight}
		float[] ret = new float[DATA_PER_CAT];
		float total_weight = 0;
		for (int j = 0; j < NUMBER_OF_CATS; j++) {
			total_weight += data[j][DATA_PER_CAT - 1];
		}
		ret[DATA_PER_CAT - 1] = total_weight;
		for (int i = 0; i < (DATA_PER_CAT - 1); i++) {
			ret[i] = 0;
			for (int j = 0; j < NUMBER_OF_CATS; j++) {
				// Add weighted data
				ret[i] += data[j][DATA_PER_CAT - 1] * data[j][i];
			}
			// Normalise weights
			if (total_weight == 0) {
				ret[i] = 0;
			} else {
				ret[i] /= total_weight;
			}
		}
		ret[DATA_PER_CAT - 1] = total_weight;
		return ret;
	}
}
