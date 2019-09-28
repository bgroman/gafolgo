/**
 * 
 */
package gafolgo;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Benjamin GromanGr
 *
 */
public class FloorManager extends Thread {

	private FloorQuadSnapshot floor, bestFloor;
	private int metric, bestMetric;
	private static final Exchanger<FloorQuadSnapshot> SWAP_SPOT = new Exchanger<FloorQuadSnapshot>();
	/**
	 * Creates a floor manager with the given starting quadrant. Fails if given null.
	 */
	public FloorManager(FloorQuadSnapshot fqs1) {
		floor = fqs1;
		bestFloor = fqs1;
		metric = calculateFullMetric(fqs1);
		bestMetric = metric;
		setDaemon(true);
	}

	/**
	 * The main worker loop of the algorithm.
	 */
	@Override
	public void run() {
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		final int size = FloorQuadSnapshot.SIZE;
		//1 calculate metric performed at construction
		while (!interrupted()) {
			//2 pick two random locations
			final int row1 = rand.nextInt(size);
			final int col1 = rand.nextInt(size);
			final int row2 = rand.nextInt(size);
			final int col2 = rand.nextInt(size);
			//3 not worth checking at this time ---- if same flavor, go back to 2
			//4 swap values
			//find first value
			final Flavor mac1 = floor.machines[row1][col1];
			//find second value
			final Flavor mac2 = floor.machines[row2][col2];
			//insert first value in second slot and insert second value in first slot
			//replace returns a clone with the given tweak, so we can chain calls to it
			final FloorQuadSnapshot newLayout = floor.replace(mac1, row2, col2).replace(mac2, row1, col1);
			//5 recalculate metric
			final int newMetric = calculateFullMetric(newLayout);
			//6 if better or 5% chance, keep swap, else revert
			keepBetter(newLayout, (rand.nextInt(20)==1));
			//8 10% chance to try to swap a quadrant
			if (rand.nextInt(10)==1) {
				try {
					final FloorQuadSnapshot offer = SWAP_SPOT.exchange(floor);
					//verify we're getting the same bag of machines before updating
					if (floor.exchangeSignature == offer.exchangeSignature) {
						//5% chance to keep it anyway
						keepBetter(offer, (rand.nextInt(20)==1));
					}
				}
				catch(InterruptedException e) {
					//make sure we properly terminate the loop, since the exception will clear the status bit
					this.interrupt();
				}
			}
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
	 * Updates the working state if the given layout is an improvement over the current working state.
	 * Also keeps the best state up to date.
	 * @param newLayout the potential new floor.
	 * @param succeedAnyway if true, the newLayout will be used even if it isn't better.
	 */
	private void keepBetter(FloorQuadSnapshot newLayout, boolean succeedAnyway) {
		final int newMetric = calculateFullMetric(newLayout);
		if (newMetric > metric) {
			//update metric because its better
			metric = newMetric;
			floor = newLayout;
			//keep best metric up to date
			if (newMetric > bestMetric) {
				bestMetric = newMetric;
				bestFloor = newLayout;
			}
		}
		else if (succeedAnyway) {
			//update metric even though its worse or the same
			metric = newMetric;
			floor = newLayout;
		}
		//7 else revert
		else {
			//actually nothing to do here because we only modify the state in this function
		}
	}
	/**
	 * Calculates the benefit metric from scratch. This method usess the right and down affinities.
	 * It assumes that calculateAffinity(X, Y) is the same as calculateAffinity(Y, X).
	 * It can also be interpreted to mean that the benefit derived from a machine is based solely on the neighbors to the right and down,
	 * in which case the prior assumption need not hold.
	 */
	private int calculateFullMetric(FloorQuadSnapshot floorQuad) {
		int metric = 0;
		final int edge = FloorQuadSnapshot.SIZE - 1;
		for (int i = 0; i < edge; i++) {
			//main affinities
			for (int j = 0; j < edge; j++) {
				//vertical
				metric += calculateAffinity(floorQuad.machines[i][j], floorQuad.machines[i+1][j]);
				//horizontal
				metric += calculateAffinity(floorQuad.machines[i][j], floorQuad.machines[i][j+1]);
			}
			//right edge
			metric += calculateAffinity(floorQuad.machines[i][edge], floorQuad.machines[i+1][edge]);
			//bottom edge
			metric += calculateAffinity(floorQuad.machines[edge][i], floorQuad.machines[edge][i+1]);
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
