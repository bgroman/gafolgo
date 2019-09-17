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
