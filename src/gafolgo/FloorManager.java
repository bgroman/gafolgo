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

	private FloorQuadSnapshot upLeft;
	private FloorQuadSnapshot upRight;
	private FloorQuadSnapshot downLeft;
	private FloorQuadSnapshot downRight;
	/**
	 * Creates a floor manager with the given starting quadrants.
	 * Quadrants are given in the order upper left, upper right, lower left, lower right.
	 */
	public FloorManager(FloorQuadSnapshot fqs1, FloorQuadSnapshot fqs2, FloorQuadSnapshot fqs3, FloorQuadSnapshot fqs4) {
		upLeft = fqs1;
		upRight = fqs2;
		downLeft = fqs3;
		downRight = fqs4;
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
			final FloorQuadSnapshot[] fallbacks = {upLeft, upRight, downLeft, downRight};
			//2 pick two random locations
			final int row1 = rand.nextInt(size*2);
			final int col1 = rand.nextInt(size*2);
			final int row2 = rand.nextInt(size*2);
			final int col2 = rand.nextInt(size*2);
			//3 not worth checking at this time ---- if same flavor, go back to 2
			//4 swap values
			//find first value
			final Flavor mac1;
			if (row1 < size) {
				if (col1 < size) {
					mac1 = upLeft.machines[row1][col1];
				}
				else {
					mac1 = upRight.machines[row1][col1-size];
				}
			}
			else {
				if (col1 < FloorQuadSnapshot.SIZE) {
					mac1 = downLeft.machines[row1-size][col1];
				}
				else {
					mac1 = downRight.machines[row1-size][col1-size];
				}
			}
			//find second value and insert first value
			final Flavor mac2;
			if (row2 < size) {
				if (col2 < size) {
					mac2 = upLeft.machines[row2][col2];
					upLeft = upLeft.replace(mac1, row2, col2);
				}
				else {
					mac2 = upRight.machines[row2][col2-size];
					upRight = upRight.replace(mac1, row2, col2-size);
				}
			}
			else {
				if (col2 < FloorQuadSnapshot.SIZE) {
					mac2 = downLeft.machines[row2-size][col2];
					downLeft = downLeft.replace(mac1, row2-size, col2);
				}
				else {
					mac2 = downRight.machines[row2-size][col2-size];
					downRight = downRight.replace(mac1, row2-size, col2-size);
				}
			}
			//insert second value in first slot
			if (row1 < size) {
				if (col1 < size) {
					upLeft = upLeft.replace(mac2, row1, col1);
				}
				else {
					upRight = upRight.replace(mac2, row1, col1-size);
				}
			}
			else {
				if (col1 < FloorQuadSnapshot.SIZE) {
					downLeft = downLeft.replace(mac2, row1-size, col1);
				}
				else {
					downRight = downRight.replace(mac2, row1-size, col1-size);
				}
			}
			//5 recalculate metric
			final int newMetric = calculateFullMetric();
			//6 if better or 10% chance, keep swap
			if (newMetric > baseMetric || rand.nextInt(20)==1) {
				//update metric
				baseMetric = newMetric;
			}
			//7 else revert
			else {
				upLeft = fallbacks[0];
				upRight = fallbacks[1];
				downLeft = fallbacks[2];
				downRight = fallbacks[3];
			}
			//8 10% chance to try to swap a quadrant
				//not yet implemented
			//9 if 10 swaps since last draw, draw.
				//not yet implemented
			//10 go back to 2
		}
	}
	/**
	 * Calculates the benefit metric from scratch. This method takes the right and down affinities.
	 * It assumes that calculateAffinity(X, Y) is the same as calculateAffinity(Y, X).
	 * It can also be interpreted to mean that the benefit derived from a machine is based solely on the neighbors to the right and down,
	 * in which case the prior assumption need not hold.
	 */
	int calculateFullMetric() {
		int metric = 0;
		final int size = FloorQuadSnapshot.SIZE;
		final int size2 = size * 2;
		final int edge = size2 - 1;
		//create an array with all the machines
		Flavor[][] floor = new Flavor[size2][size2];
		//this implementation is not optimized for memory management, but rather for loop complexity
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				floor[i][j] = upLeft.machines[i][j];
				floor[i][j + size] = upRight.machines[i][j];
				floor[i + size][j] = downLeft.machines[i][j];
				floor[i + size][j + size] = downRight.machines[i][j];
			}
		}
		for (int i = 0; i < edge; i++) {
			//main affinities
			for (int j = 0; j < edge; j++) {
				//vertical
				metric += calculateAffinity(floor[i][j], floor[i+1][j]);
				//horizontal
				metric += calculateAffinity(floor[i][j], floor[i][j+1]);
			}
			//right edge
			metric += calculateAffinity(floor[i][edge], floor[i+1][edge]);
			//bottom edge
			metric += calculateAffinity(floor[edge][i], floor[edge][i+1]);
		}
		return metric;
	}
	/**
	 * This method calculates the affinity for two machines. The order of the parameters is not supposed to make a difference.
	 */
	private int calculateAffinity(Flavor machine, Flavor neighbor) {
		if (machine == neighbor) return 0;
		else if (machine == Flavor.Yellow) {
			if (neighbor == Flavor.Red) return -10;
			else if (neighbor == Flavor.Green) return 8;
			else if (neighbor == Flavor.Blue) return 1;
		}
		else if (machine == Flavor.Red) {
			if (neighbor == Flavor.Yellow) return -10;
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
