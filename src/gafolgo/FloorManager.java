/**
 * 
 */
package gafolgo;

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
		//1 calculate metric
		//2 pick two random locations
		//3 if same flavor, go back to 2
		//4 swap values
		//5 recalculate metric
		//6 if better or 10% chance, keep swap
		//7 else revert
		//8 10% chance to try to swap a quadrant
		//9 if a 10 swaps since last draw, draw.
		//10 go back to 2

	}
	/**
	 * Calculates the benefit metric from scratch. This method takes the right and down affinities.
	 * It assumes that calculateAffinity(X, Y) is the same as calculateAffinity(Y, X).
	 */
	private int calculateFullMetric() {
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
			if (neighbor == Flavor.Red) return -100;
			else if (neighbor == Flavor.Green) return 8;
			else if (neighbor == Flavor.Blue) return 1;
		}
		else if (machine == Flavor.Red) {
			if (neighbor == Flavor.Yellow) return -100;
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
