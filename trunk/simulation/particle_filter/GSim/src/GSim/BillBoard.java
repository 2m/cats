package GSim;

/**
 * Skeleton for the networking interface as seen by the filters
 * 
 * @author Fredrik Wahlberg
 * 
 */
public class BillBoard {
	// FIXME: See how much needs to be synced for ukf

	/** Data to be synced */
	private float[][] data;
	private float[] sightings;
	/** Floats to sync per cat */
	private final int DATA_PER_CAT = 11;
	/** Number of cats (set in constructor) */
	public final int NUMBER_OF_CATS;

	public float[] position;

	public BillBoard(int noOfCats) {
		NUMBER_OF_CATS = noOfCats;
		data = new float[NUMBER_OF_CATS][DATA_PER_CAT];
		sightings = new float[NUMBER_OF_CATS * 3];
		for (int i = 0; i < NUMBER_OF_CATS * 3; i++) {
			sightings[i] = -1;
		}
		position = new float[NUMBER_OF_CATS * 3];
	}

	public void setLatestSighting(int id, float x, float y, float theta) {
		// id in range [1:n]
		sightings[(id - 1) * 3 + 0] = x;
		sightings[(id - 1) * 3 + 1] = y;
		sightings[(id - 1) * 3 + 2] = theta;
	}

	// TODO: For UKF, add getter for tachometer positioning and landmark
	// sighting

	public void setAbsolutePosition(int id, float x, float y, float angle) {
		position[(id - 1) * 3 + 0] = x;
		position[(id - 1) * 3 + 1] = y;
		position[(id - 1) * 3 + 2] = angle;
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
		int i = id - 1;
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
		// ret = {m_x, m_y, m'_x, m'_y, xx, xy, yy, x'x', x'y', y'y'}
		float[] ret = new float[DATA_PER_CAT];
		float total_weight = 0;
		for (int j = 0; j < NUMBER_OF_CATS; j++) {
			total_weight += data[j][10];
		}
		ret[10] = total_weight;
		for (int i = 0; i < DATA_PER_CAT - 1; i++) {
			ret[i] = 0;
			for (int j = 0; j < NUMBER_OF_CATS; j++) {
				// Add weighted data
				ret[i] += data[j][10] * data[j][i];
			}
			// Normalise weights
			if (total_weight == 0) {
				ret[i] = 0;
			} else {
				ret[i] /= total_weight;
			}
		}
		return ret;
	}
}
