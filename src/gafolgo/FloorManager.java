/**
 * 
 */
package gafolgo;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Benjamin GromanGr
 *
 */
public class FloorManager implements Runnable {

	private FloorQuadSnapshot floor, bestFloor;
	private int bestMetric;
	/**
	 * Creates a floor manager with the given starting quadrant.
	 */
	public FloorManager(FloorQuadSnapshot fqs1) {
		floor = fqs1;
		bestFloor = fqs1;
		bestMetric = calculateFullMetric();
	}

	/**
	 * The main worker loop of the algorithm.
	 */
	@Override
	public void run() {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		final int size = FloorQuadSnapshot.SIZE;
		//1 calculate metric
		int baseMetric = calculateFullMetric();
		while (!Thread.interrupted()) {
			final FloorQuadSnapshot fallback = floor;
			//2 pick two random locations
			final int row1 = rand.nextInt(size);
			final int col1 = rand.nextInt(size);
			final int row2 = rand.nextInt(size);
			final int col2 = rand.nextInt(size);
			//3 not worth checking at this time ---- if same flavor, go back to 2
			//4 swap values
			//find first value
			final Flavor mac1 = floor.machines[row1][col1];
			//find second value and insert first value
			final Flavor mac2 = floor.machines[row2][col2];
			floor = floor.replace(mac1, row2, col2);
			//insert second value in first slot
			floor = floor.replace(mac2, row1, col1);
			//5 recalculate metric
			final int newMetric = calculateFullMetric();
			//6 if better or 10% chance, keep swap
			if (newMetric > baseMetric) {
				//update metric because its better
				baseMetric = newMetric;
				//keep best metric up to date
				if (newMetric > bestMetric) {
					bestMetric = newMetric;
					bestFloor = floor;
				}
			}
			else if (rand.nextInt(20)==1) {
				//update metric even though its worse
				baseMetric = newMetric;
			}
			//7 else revert
			else {
				floor = fallback;
			}
			//8 10% chance to try to swap a quadrant
				//not yet implemented
			//9 if 10 swaps since last draw, draw.
				//not yet implemented
			//10 go back to 2
		}
	}
	/**
	 * Returns the best metric found so far.
	 */
	public int getBestMetric() {
		return bestMetric;
	}
	/**
	 * Returns the best layout found so far.
	 */
	public FloorQuadSnapshot getBestLayout() {
		return bestFloor;
	}
	/**
	 * Calculates the benefit metric from scratch. This method takes the right and down affinities.
	 * It assumes that calculateAffinity(X, Y) is the same as calculateAffinity(Y, X).
	 * It can also be interpreted to mean that the benefit derived from a machine is based solely on the neighbors to the right and down,
	 * in which case the prior assumption need not hold.
	 */
	private int calculateFullMetric() {
		int metric = 0;
		final int edge = FloorQuadSnapshot.SIZE - 1;
		for (int i = 0; i < edge; i++) {
			//main affinities
			for (int j = 0; j < edge; j++) {
				//vertical
				metric += calculateAffinity(floor.machines[i][j], floor.machines[i+1][j]);
				//horizontal
				metric += calculateAffinity(floor.machines[i][j], floor.machines[i][j+1]);
			}
			//right edge
			metric += calculateAffinity(floor.machines[i][edge], floor.machines[i+1][edge]);
			//bottom edge
			metric += calculateAffinity(floor.machines[edge][i], floor.machines[edge][i+1]);
		}
		return metric;
	}
	/**
	 * This method calculates the affinity for two machines. The order of the parameters is not supposed to make a difference.
	 */
	private int calculateAffinity(Flavor machine, Flavor neighbor) {
		if (machine == neighbor) return 10;
		else if (machine == Flavor.Yellow) {
			if (neighbor == Flavor.Red) return -1;
			else if (neighbor == Flavor.Green) return 8;
			else if (neighbor == Flavor.Blue) return 1;
		}
		else if (machine == Flavor.Red) {
			if (neighbor == Flavor.Yellow) return -1;
			else if (neighbor == Flavor.Green) return 50;
			else if (neighbor == Flavor.Blue) return 25;
		}
		else if (machine == Flavor.Green) {
			if (neighbor == Flavor.Yellow) return 8;
			else if (neighbor == Flavor.Red) return 50;
			else if (neighbor == Flavor.Blue) return 20;
		}
		else if (machine == Flavor.Blue) {
			if (neighbor == Flavor.Yellow) return 1;
			else if (neighbor == Flavor.Red) return 25;
			else if (neighbor == Flavor.Green) return 20;
		}
		return 0;
	}

}
