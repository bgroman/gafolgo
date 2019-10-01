package gafolgo;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Benjamin Groman
 * This class keeps track of the FloorQuadSnapshots and implements the main algorithmic loop.
 */
public class FloorManager extends Thread {
	/**
	 * The floor as it currently stands during operation.
	 */
	private FloorQuadSnapshot floor;
	/**
	 * The best layout of the floor found so far.
	 */
	private FloorQuadSnapshot bestFloor;
	/**
	 * The affinity metric as it currently stands during operation.
	 */
	private int metric;
	/**
	 * The best value of the affinity metric found so far.
	 */
	private int bestMetric;
	/**
	 * The exchanger that all threads use to communicate solutions.
	 */
	private static final Exchanger<FloorQuadSnapshot> SWAP_SPOT = new Exchanger<FloorQuadSnapshot>();
	/**
	 * The display that this class updates. If null during execution, this attribute will be ignored.
	 */
	private FloorPanel display = null;
	/**
	 * Used to prevent drawing from occurring too frequently.
	 * Represents milliseconds.
	 */
	private long lastDraw = 0;
	/**
	 * Creates a floor manager with the given starting quadrant. Fails if given null.
	 */
	public FloorManager(FloorQuadSnapshot fqs1) {
		//floor and bestFloor are initially the same
		floor = fqs1;
		bestFloor = fqs1;
		//metric and bestMetric are the same, too, but we don't need to re-do the calculations
		metric = FloorQuadSnapshot.calculateFullMetric(fqs1);
		bestMetric = metric;
		//don't let this thread keep the program from terminating
		setDaemon(true);
	}

	/**
	 * The main worker loop of the algorithm.
	 * 1) Calculate metric.
	 * 2) Pick two random locations.
	 * 3) If same flavor, go back to 2. (Not implemented, but included for consistency with original plan numbers.)
	 * 4) Swap values.
	 * 5) Recalculate metric.
	 * 6) Keep the better layout, with a 5% chance of taking the new one anyway.
	 * 7) Revert variables as necessary. (Not present in this function.)
	 * 8) 10% chance of trying to swap a solution with another thread, with a 5% chance of keeping valid ones anyway.
	 * 9) Draw approximately once a second.
	 * 10) Go back to 2 unless interrupted.
	 */
	@Override
	public void run() {
		//get a friendly name for our random number generator
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		//get a friendly name for the dimension of the floor
		final int size = FloorQuadSnapshot.SIZE;
		//1 calculate metric performed at construction, so we don't need to do it again until we update the layout
		//check for interrupt so that we can terminate nicely
		while (!interrupted()) {
			//2 pick two random locations
			//first location
			final int row1 = rand.nextInt(size);
			final int col1 = rand.nextInt(size);
			//second location
			final int row2 = rand.nextInt(size);
			final int col2 = rand.nextInt(size);
			//3 decided not to check. ---- if same flavor, go back to 2
			//4 swap values
			//find first value
			final Flavor mac1 = floor.machine(row1, col1);
			//find second value
			final Flavor mac2 = floor.machine(row2, col2);
			//insert first value in second slot and insert second value in first slot
			//replace returns a clone with the given tweak, so we can chain calls to it
			final FloorQuadSnapshot newLayout = floor.replace(mac1, row2, col2).replace(mac2, row1, col1);
			//5 recalculate metric (happens inside the function)
			//6 if better or 5% chance, keep swap, else revert
			keepBetter(newLayout, (rand.nextInt(20)==1));
			//8 10% chance to try to swap a quadrant
			if (rand.nextInt(10)==1) {
				try {
					final FloorQuadSnapshot offer = SWAP_SPOT.exchange(floor);
					//verify we're getting the same bag of machines before updating
					if (floor.exchangeSignature == offer.exchangeSignature) {
						//5% chance to keep it regardless of improvements
						keepBetter(offer, (rand.nextInt(20)==1));
					}
				}
				catch(InterruptedException e) {
					//make sure we properly terminate the loop, since the exception will clear the status bit
					this.interrupt();
				}
			}
			//9 if display set and at least ~1000 milliseconds since last draw, queue draw.
			if (display != null && lastDraw + 1000 < System.currentTimeMillis()) {
				//using a lambda runnable
				javax.swing.SwingUtilities.invokeLater(() -> {display.update(floor, metric);});
				//update the time tracker - forgot to do this initially
				lastDraw = System.currentTimeMillis();
			}
			try {
				//waiting between swaps to avoid total load of my system.
				sleep(5);
			}
			catch(InterruptedException e) {
				//be sure we don't miss our termination signal
				this.interrupt();
			}
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
	 * Returns the last metric found. Result not guaranteed if the thread is running.
	 */
	public int getLastMetric() {
		return metric;
	}
	/**
	 * Returns the last layout found. Result not guaranteed if the thread is running.
	 */
	public FloorQuadSnapshot getLastLayout() {
		return floor;
	}
	/**
	 * Updates the working state if the given layout is an improvement over the current working state.
	 * Also keeps the best state up to date.
	 * @param newLayout the potential new floor.
	 * @param succeedAnyway if true, the newLayout will be used even if it isn't better.
	 */
	private void keepBetter(FloorQuadSnapshot newLayout, boolean succeedAnyway) {
		//calculate the new metric
		final int newMetric = FloorQuadSnapshot.calculateFullMetric(newLayout);
		//check for improvement
		if (newMetric > metric) {
			//update state because the new one is better
			metric = newMetric;
			floor = newLayout;
			//keep best state up to date as appropriate
			if (newMetric > bestMetric) {
				bestMetric = newMetric;
				bestFloor = newLayout;
			}
		}
		else if (succeedAnyway) {
			//update metric even though its worse or the same
			//(not going to get here if it was better)
			metric = newMetric;
			floor = newLayout;
		}
		//7 else revert
		else {
			//actually nothing to do here because we only modify the state in this function,
			//and we skipped the blocks that do modify it to get here
		}
	}
	/**
	 * Allows the FloorManager to be linked to the GUI and know what to update.
	 * @param panel The FloorPanel that will be updated when this FloorManager draws.
	 */
	public void setPanel(FloorPanel panel) {
		display = panel;
	}

}
