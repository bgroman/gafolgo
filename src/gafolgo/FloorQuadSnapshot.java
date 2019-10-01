package gafolgo;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Benjamin Groman
 * This class initially represented each of four quadrants of the floor.
 * It now represents the whole floor, which is still a quadrant.
 */
final class FloorQuadSnapshot {
	/**
	 * Size is the number of rows and the number of columns, not the number of machines.
	 */
	static final int SIZE = 8;
	/**
	 * The final modifier does not prevent the individual machines from being modified.
	 * This led to a bug where the same array would be used by all modifications of a FloorQuadSnapshot,
	 * causing it to eventually become all one color.
	 * This has been fixed by preventing direct access and performing copying at the appropriate times.
	 */
	private final Flavor[][] machines;
	/**
	 * Two FloorQuadSnapshots may be exchanged without changing the problem space if their exchangeSignatures match.
	 */
	public final int exchangeSignature;

	/**
	 * This method produces a random quadrant of the floor. It is intended for use when starting the program.
	 */
	public FloorQuadSnapshot() {
		//keep track of the signature as we go
		int runningSignature = 0;
		//create a temporary array of the static size
		Flavor[][] temp = new Flavor[SIZE][SIZE];
		//for every row
		for (int i = 0; i < SIZE; i++) {
			//for every column/slot
			for (int j = 0; j < SIZE; j++) {
				//initialize to a random machine
				temp[i][j] = getRandomMachine();
				//update the signature with that machine
				runningSignature += getExchangeValue(temp[i][j]);
			}
		}
		//set the final attributes
		machines = temp;
		exchangeSignature = runningSignature;
	}
	/**
	 * This method produces a quadrant of the floor. It is intended for use when creating a derivative of an existing quadrant.
	 * @param grid The grid to initialize the machines to.
	 */
	private FloorQuadSnapshot(Flavor[][] grid) {
		//this is allowed only because replace() already handled creating a new array
		machines = grid;
		//keep track of the signature as we go
		int runningSignature = 0;
		//for every row that is within the allowed size and the actual given array
		for (int i = 0; i < grid.length && i < SIZE; i++) {
			//for every column/slot that is within the allowed size and the actual given array
			for (int j = 0; j < grid[i].length && j < SIZE; j++)
			{
				//update the signature
				runningSignature += getExchangeValue(grid[i][j]);
			}
		}
		//set the final attribute
		exchangeSignature = runningSignature;
	}
	/**
	 * This method produces a new quadrant with the machine at the given position replaced with the given flavor.
	 * @param newMachine The flavor of machine to insert.
	 * @param row The row of the machine to replace. Will be modulated by size.
	 * @param col The column of the machine to replace. Will be modulated by size.
	 * @return A new FloorQuadSnapshot independent of the original, with one machine tweaked.
	 */
	public FloorQuadSnapshot replace(Flavor newMachine, int row, int col) {
		//create a new array so we don't share the old one
		Flavor[][] current =  new Flavor[SIZE][SIZE];
		//for every row that is within the allowed size and the original array
		for (int i = 0; i < machines.length && i < SIZE; i++) {
			//for every column/slot that is within the allowed size and the original array
			for (int j = 0; j < machines[i].length && j < SIZE; j++)
			{
				//copy the machine.
				//machines are enumeration values, so they can't be modified to break our immutability
				current[i][j] = machines[i][j];
			}
		}
		//update that one machine that was requested to be different, modulating by size
		current[row%SIZE][col%SIZE] = newMachine;
		//use the constructor to establish the exchangeSignature
		return new FloorQuadSnapshot(current);
	}
	/**
	 * Now that machines is private, we need a way to access it without modifying it.
	 * @param row The row of the machine to retrieve. Will be modulated by size.
	 * @param col The column of the machine to retrieve. Will be modulated by size.
	 */
	public Flavor machine(int row, int col) {
		return machines[row%SIZE][col%SIZE];
	}
	/**
	 * Provides a value for machines such that the sum of the values for a quadrant is unique.
	 * This uniqueness can be quantified by stating that two quadrants have the same total exchange value
	 * if and only if they have the same number of each flavor of machine.
	 * @param machine The flavor of machine to return the value for.
	 * @return The exchange value of the given machine.
	 */
	private static int getExchangeValue(Flavor machine) {
		//the formula for calculating what each machine is worth is:
		//fill the floor solely with the next highest-valued machine, calculate the sum, and add one
		//a good compiler will probably just substitute the actual values and avoid doing all that multiplication at runtime
		switch(machine) {
		case Blue:
			//start with one rather than zero to prevent two different sizes of blue floors from having the same total exchange value
			return 1;
		case Green:
			return (SIZE * SIZE) + 1;
		case Red:
			return (((SIZE * SIZE) + 1) * SIZE * SIZE) + 1;
		default://Yellow
			return (((((SIZE * SIZE) + 1) * SIZE * SIZE) + 1) * SIZE * SIZE) + 1;
		}
	}
	/**
	 * This method generates a flavor of machine.
	 * @return A randomly selected flavor of machine.
	 */
	public static Flavor getRandomMachine() {
		//using ThreadLocalRandom even though we don't expect this to be called during multi-threaded operation
		int rand = ThreadLocalRandom.current().nextInt(4);
		//this is currently set up for equal opportunity
		//it could be changed later to provide different weightings
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
	 * Calculates the benefit metric from scratch. This method uses the right and down affinities.
	 * It assumes that either calculateAffinity(X, Y) is the same as calculateAffinity(Y, X)
	 * or that the benefit derived from a machine is based solely on the neighbors to the right and down.
	 */
	public static int calculateFullMetric(FloorQuadSnapshot floorQuad) {
		//start our running total
		int metric = 0;
		//this is purely for ease of programming
		//the compiler will probably just substitute the actual value further down
		final int edge = FloorQuadSnapshot.SIZE - 1;
		//for every row, not including the bottom
		for (int i = 0; i < edge; i++) {
			//main affinities
			//for each cell within the row, not including the rightmost
			for (int j = 0; j < edge; j++) {
				//vertical (down) affinity
				metric += calculateAffinity(floorQuad.machines[i][j], floorQuad.machines[i+1][j]);
				//horizontal (right) affinity
				metric += calculateAffinity(floorQuad.machines[i][j], floorQuad.machines[i][j+1]);
			}
			
			//edge affinities (take advantage of being square)
			//right edge (down) affinity
			metric += calculateAffinity(floorQuad.machines[i][edge], floorQuad.machines[i+1][edge]);
			//bottom edge (right) affinity
			metric += calculateAffinity(floorQuad.machines[edge][i], floorQuad.machines[edge][i+1]);
			
			//corner affinity - no benefit derived from the lower right machine itself, no matter what it is
			//the neighbors will care about it, though
		}
		//return our total
		return metric;
	}
	/**
	 * This method calculates the affinity for two machines.
	 * The order of the parameters now makes a difference.
	 * Expected optimum: red and green checker board with blue edging and yellow low and/or on the right.
	 * @param machine The machine whose affinity is being queried.
	 * @param neighbor The machine the first machine is evaluating.
	 * @return The affinity value.
	 */
	private static int calculateAffinity(Flavor machine, Flavor neighbor) {
		//flat 10 for the same flavor
		if (machine == neighbor) return 10;
		//yellow's opinions - the weakest spread
		else if (machine == Flavor.Yellow) {
			if (neighbor == Flavor.Red) return -1;
			else if (neighbor == Flavor.Green) return 8;
			else if (neighbor == Flavor.Blue) return 1;
		}
		//red's opinions
		else if (machine == Flavor.Red) {
			if (neighbor == Flavor.Yellow) return -2;
			else if (neighbor == Flavor.Green) return 50;
			else if (neighbor == Flavor.Blue) return 25;
		}
		//green's opinions - the strongest spread
		else if (machine == Flavor.Green) {
			if (neighbor == Flavor.Yellow) return 21;
			else if (neighbor == Flavor.Red) return 50;
			else if (neighbor == Flavor.Blue) return 20;
		}
		//blue's opinions
		else if (machine == Flavor.Blue) {
			if (neighbor == Flavor.Yellow) return 11;
			else if (neighbor == Flavor.Red) return 5;
			else if (neighbor == Flavor.Green) return 20;
		}
		//this should never be reached, but it exists in case Flavor gets modified
		return 0;
	}

}
