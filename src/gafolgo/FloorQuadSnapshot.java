package gafolgo;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Benjamin Groman
 * This class represents a quadrant of the floor.
 *
 */
class FloorQuadSnapshot {
	/**
	 * Size is the number of rows and the number of columns, not the number of machines.
	 */
	static final int SIZE = 8;
	final Flavor[][] machines;
	final int exchangeSignature;

	/**
	 * This method produces a random quadrant of the floor. It is intended for use when starting the program.
	 */
	public FloorQuadSnapshot() {
		int runningSignature = 0;
		Flavor[][] temp = new Flavor[SIZE][SIZE];
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				temp[i][j] = getRandomMachine();
				runningSignature += getExchangeValue(temp[i][j]);
			}
		}
		machines = temp;
		exchangeSignature = runningSignature;
	}
	/**
	 * This method produces a quadrant of the floor. It is intended for use when creating a derivative of an existing quadrant.
	 * @param grid The grid to initialize the machines to.
	 */
	private FloorQuadSnapshot(Flavor[][] grid) {
		machines = grid;
		int runningSignature = 0;
		for (int i = 0; i < grid.length && i < SIZE; i++) {
			for (int j = 0; j < grid[i].length && j < SIZE; j++)
			{
				runningSignature += getExchangeValue(grid[i][j]);
			}
		}
		exchangeSignature = runningSignature;
	}
	/**
	 * This method produces a new quadrant with the machine at the given position replaced with the given flavor
	 * @param newMachine The flavor of machine to insert
	 * @param row The row of the machine to replace. Will be modulated by size.
	 * @param col The column of the machine to replace. Will be modulated by size.
	 */
	public FloorQuadSnapshot replace(Flavor newMachine, int row, int col) {
		Flavor[][] current =  new Flavor[SIZE][SIZE];
		for (int i = 0; i < machines.length && i < SIZE; i++) {
			for (int j = 0; j < machines[i].length && j < SIZE; j++)
			{
				current[i][j] = machines[i][j];
			}
		}
		current[row%SIZE][col%SIZE] = newMachine;
		return new FloorQuadSnapshot(current);
	}
	/**
	 * Provides a value for machines such that the sum of the values for a quadrant is unique.
	 * @param machine The flavor of machine to return the value for.
	 */
	private static int getExchangeValue(Flavor machine) {
		switch(machine) {
		case Blue:
			return 0;
		case Green:
			return 1;
		case Red:
			return (SIZE * SIZE) + 1;
		default://Yellow
			return (((SIZE * SIZE) + 1) * SIZE * SIZE) + 1;
		}
	}
	/**
	 * This method generates a flavor of machine.
	 * @return A randomly selected flavor of machine.
	 */
	public static Flavor getRandomMachine() {
		int rand = ThreadLocalRandom.current().nextInt(4);
		switch(rand) {
		case 0:
			return Flavor.Blue;
		case 1:
			return Flavor.Green;
		case 2:
			return Flavor.Red;
		default:
			return Flavor.Yellow;
		}
	}
	/**
	 * Calculates the benefit metric from scratch. This method usess the right and down affinities.
	 * It assumes that calculateAffinity(X, Y) is the same as calculateAffinity(Y, X).
	 * It can also be interpreted to mean that the benefit derived from a machine is based solely on the neighbors to the right and down,
	 * in which case the prior assumption need not hold.
	 */
	public static int calculateFullMetric(FloorQuadSnapshot floorQuad) {
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
	 * This method calculates the affinity for two machines. The order of the parameters now makes a difference.
	 */
	private static int calculateAffinity(Flavor machine, Flavor neighbor) {
		if (machine == neighbor) return 10;
		else if (machine == Flavor.Yellow) {
			if (neighbor == Flavor.Red) return -1;
			else if (neighbor == Flavor.Green) return 8;
			else if (neighbor == Flavor.Blue) return 1;
		}
		else if (machine == Flavor.Red) {
			if (neighbor == Flavor.Yellow) return -2;
			else if (neighbor == Flavor.Green) return 50;
			else if (neighbor == Flavor.Blue) return 25;
		}
		else if (machine == Flavor.Green) {
			if (neighbor == Flavor.Yellow) return 21;
			else if (neighbor == Flavor.Red) return 50;
			else if (neighbor == Flavor.Blue) return 20;
		}
		else if (machine == Flavor.Blue) {
			if (neighbor == Flavor.Yellow) return 11;
			else if (neighbor == Flavor.Red) return 5;
			else if (neighbor == Flavor.Green) return 20;
		}
		return 0;
	}

}
