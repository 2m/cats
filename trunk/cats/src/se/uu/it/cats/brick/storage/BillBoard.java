package se.uu.it.cats.brick.storage;

import se.uu.it.cats.brick.network.ConnectionManager;
import se.uu.it.cats.brick.network.packet.LatestSightingUpdate;
import se.uu.it.cats.brick.network.packet.MeanAndCovarianceUpdate;

public class BillBoard
{
	/** Data to be synced */
	private float[][] data;
	private float[] sightings;

	/** Floats to sync per cat */
	private final int DATA_PER_CAT = 11;

	/** Number of cats (set in constructor) */
	private final int NUMBER_OF_CATS = 3;

	public static final BillBoard instanceHolder = new BillBoard();
	
	public static BillBoard getInstance()
	{
		return instanceHolder;
	}

	private BillBoard() {            
		data = new float[NUMBER_OF_CATS][DATA_PER_CAT];
		sightings = new float[NUMBER_OF_CATS * 3];

		for (int i = 0; i < NUMBER_OF_CATS * 3; i++) {
			sightings[i] = -1;
		}

		// TODO: is initialization to some value of variable "data" needed?
	}
	
	public int getNoCats() {
		return NUMBER_OF_CATS;
	}
	
	public void setLatestSighting(int id, float x, float y, float theta) {
		// id in range [1:n]
		// set the latest sighting and send update to other devices
		setLatestSighting(id - 1, x, y, theta, true);
	}
	
	public void setLatestSighting(LatestSightingUpdate p) {
		// id in range [0:n-1]
		// got data from other device,
		// save data and do not send an update
		setLatestSighting(p.getSource(), p.getX(), p.getY(), p.getTheta(), false);
	}

	public void setLatestSighting(int id, float x, float y, float theta, boolean sendUpdate) {
		sightings[id * 3 + 0] = x;
		sightings[id * 3 + 1] = y;
		sightings[id * 3 + 2] = theta;
		
		if (sendUpdate)
			// send the update to all
			ConnectionManager.getInstance().sendPacketToAll(
					new LatestSightingUpdate(x, y, theta)
			);
	}
	
	public float[] getLatestSightings() {
		return sightings;
	}

	//TODO: For UKF, add getter for tachometer positioning and landmark sighting

	public void setMeanAndCovariance(int id, float mean_x, float mean_y,
			float mean_xv, float mean_yv, float var_xx, float var_xy,
			float var_yy, float var_xvxv, float var_xvyv, float var_yvyv,
			float weight) {
		// id in range [1:n]
		setMeanAndCovariance(id - 1, mean_x, mean_y, mean_xv, mean_yv,
				var_xx, var_xy, var_yy, var_xvxv, var_xvyv, var_yvyv, weight, true
		);
	}
	
	public void setMeanAndCovariance(MeanAndCovarianceUpdate p) {
		// id in range [0:n-1]
		setMeanAndCovariance(
				p.getSource(),
				p.getMeanX(),
				p.getMeanY(),
				p.getMeanXv(),
				p.getMeanYv(),
				p.getVarXX(),
				p.getVarXY(),
				p.getVarYY(),
				p.getVarXvXv(),
				p.getVarXvYv(),
				p.getVarYvYv(),
				p.getWeight(),
				false
		);
	}
	
	/** Set mean and covariance */
	public void setMeanAndCovariance(int id, float mean_x, float mean_y,
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
					new MeanAndCovarianceUpdate(mean_x, mean_y, mean_xv, mean_yv,
							var_xx, var_xy, var_yy, var_xvxv, var_xvyv, var_yvyv, weight)
			);
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
