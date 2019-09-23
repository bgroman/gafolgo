package gafolgo;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Benjamin Groman
 * This class represents a quadrant of the floor.
 *
 */
class FloorQuadSnapshot {
	private static final int SIZE = 3;
	private final Flavor[][] machines;

	/**
	 * This method produces a random quadrant of the floor. It is intended for use when starting the program.
	 */
	public FloorQuadSnapshot() {
		Flavor[][] temp = new Flavor[SIZE][SIZE];
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				temp[i][j] = getRandomMachine();
			}
		}
		machines = temp;
	}
	/**
	 * This method produces a quadrant of the floor. It is intended for use when creating a derivative of an existing quadrant.
	 * @param grid The grid to initialize the machines to.
	 */
	private FloorQuadSnapshot(Flavor[][] grid) {
		machines = grid;
	}
	/**
	 * This method produces a new quadrant with the machine at the given position replaced with the given flavor
	 * @param newMachine The flavor of machine to insert
	 * @param row The row of the machine to replace. Will be modulated by size.
	 * @param col The column of the machine to replace. Will be modulated by size.
	 */
	public FloorQuadSnapshot replace(Flavor newMachine, int row, int col) {
		Flavor[][] current = machines;
		current[row%SIZE][col%SIZE] = newMachine;
		return new FloorQuadSnapshot(current);
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

}
