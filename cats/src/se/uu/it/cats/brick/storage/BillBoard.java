package se.uu.it.cats.brick.storage;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.Identity;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.ConnectionManager;
import se.uu.it.cats.brick.network.packet.AbsolutePositionUpdate;
import se.uu.it.cats.brick.network.packet.LatestSightingUpdate;
import se.uu.it.cats.brick.network.packet.MeanAndCovarianceUpdate;

public class BillBoard {
	/** Data to be synced */
	private float[][] data;
	private float[] sightings;
	private float[] positions;

	private int latestDataUpdate = 0;
	private int latestSightingUpdate = 0;

	/** Floats to sync per cat */
	private final int DATA_PER_CAT = 11;

	/** Number of cats (set in constructor) */
	private final int NUMBER_OF_CATS = Identity.CAT_COUNT;
	
	private final boolean CHECK_TIMESTAMPS = false;

	public static final BillBoard instanceHolder = new BillBoard();

	public static BillBoard getInstance() {
		return instanceHolder;
	}

	private BillBoard() {
		data = new float[NUMBER_OF_CATS][DATA_PER_CAT];
		sightings = new float[NUMBER_OF_CATS * 4];
		positions = new float[NUMBER_OF_CATS * 4];

		for (int i = 0; i < NUMBER_OF_CATS * 4; i++) {
			sightings[i] = -1;
		}

		// TODO: is the initialization of variable "positions" needed? 
		// TODO: is the initialization of variable "data" needed?

	}

	public int getNoCats() {
		return NUMBER_OF_CATS;
	}

	public void setLatestSighting(int id, float x, float y, float theta, int timestamp) {
		// id in range [1:n]
		// set the latest sighting and send update to other devices
		setLatestSighting(id, x, y, theta, timestamp, true);
	}

	public void setLatestSighting(LatestSightingUpdate p) {
		// id in range [0:n-1]
		// got data from other device,
		// check if it is new
		if (!CHECK_TIMESTAMPS || p.getTimestamp() > latestSightingUpdate) {
			// save data and do not send an update
			setLatestSighting(p.getSource(), p.getX(), p.getY(), p.getTheta(), p.getTimestamp(),
					false);
			latestDataUpdate = p.getTimestamp();
		}
	}

	private void setLatestSighting(int id, float x, float y, float theta, int timestamp,
			boolean sendUpdate) {
		sightings[id * 4 + 0] = x;
		sightings[id * 4 + 1] = y;
		sightings[id * 4 + 2] = theta;
		sightings[id * 4 + 3] = timestamp;
		//System.out.println("timestamp latest s. "+timestamp);
		if (sendUpdate)
			// send the update to all
			ConnectionManager.getInstance().sendPacketToAll(
					new LatestSightingUpdate(x, y, theta, Clock.timestamp()));
	}

	public float[] getLatestSightings() {
		return sightings;
	}
	
	public void setAbsolutePosition(int id, float x, float y, float theta, int timestamp) {
		// id in range [1:n]
		// set the absolute position and send update to other devices
		setAbsolutePosition(id, x, y, theta, timestamp, true);
	}

	public void setAbsolutePosition(AbsolutePositionUpdate p) {
		// id in range [0:n-1]
		// got data from other device,
		// check if it is new
		if (!CHECK_TIMESTAMPS || p.getTimestamp() > latestSightingUpdate) {
			// save data and do not send an update
			setAbsolutePosition(p.getSource(), p.getX(), p.getY(), p.getTheta(), p.getTimestamp(),
					false);
			latestDataUpdate = p.getTimestamp();
		}
	}

	private void setAbsolutePosition(int id, float x, float y, float theta, int timestamp,
			boolean sendUpdate) {
		positions[id * 4 + 0] = x;
		positions[id * 4 + 1] = y;
		positions[id * 4 + 2] = theta;
		positions[id * 4 + 3] = timestamp;

		if (sendUpdate)
			// send the update to all
			ConnectionManager.getInstance().sendPacketToAll(
					new AbsolutePositionUpdate(x, y, theta, Clock.timestamp()));
	}

	public float[] getAbsolutePositions() {
		return positions;
	}

	// TODO: For UKF, add getter for tachometer positioning and landmark
	// sighting

	public void setMeanAndCovariance(int id, float mean_x, float mean_y,
			float mean_xv, float mean_yv, float var_xx, float var_xy,
			float var_yy, float var_xvxv, float var_xvyv, float var_yvyv,
			float weight) {
		// id in range [0:n-1]
		setMeanAndCovariance(id, mean_x, mean_y, mean_xv, mean_yv, var_xx,
				var_xy, var_yy, var_xvxv, var_xvyv, var_yvyv, weight, true);
	}

	public void setMeanAndCovariance(MeanAndCovarianceUpdate p) {
		// id in range [0:n-1]
		if (!CHECK_TIMESTAMPS || p.getTimestamp() > latestDataUpdate) {
			setMeanAndCovariance(p.getSource(), p.getMeanX(), p.getMeanY(), p
					.getMeanXv(), p.getMeanYv(), p.getVarXX(), p.getVarXY(), p
					.getVarYY(), p.getVarXvXv(), p.getVarXvYv(),
					p.getVarYvYv(), p.getWeight(), false);
			latestDataUpdate = p.getTimestamp();
		}
	}

	/** Set mean and covariance */
	private void setMeanAndCovariance(int id, float mean_x, float mean_y,
			float mean_xv, float mean_yv, float var_xx, float var_xy,
			float var_yy, float var_xvxv, float var_xvyv, float var_yvyv,
			float weight, boolean sendUpdate) {

		data[id][0] = mean_x;
		data[id][1] = mean_y;
		data[id][2] = mean_xv;
		data[id][3] = mean_yv;
		data[id][4] = var_xx;
		data[id][5] = var_xy;
		data[id][6] = var_yy;
		data[id][7] = var_xvxv;
		data[id][8] = var_xvyv;
		data[id][9] = var_yvyv;
		data[id][10] = weight;

		if (sendUpdate)
			// send the update to all
			ConnectionManager.getInstance().sendPacketToAll(
					new MeanAndCovarianceUpdate(mean_x, mean_y, mean_xv,
							mean_yv, var_xx, var_xy, var_yy, var_xvxv,
							var_xvyv, var_yvyv, weight, Clock.timestamp()));
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
