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
	private FloorPanel display = null;
	private long lastDraw = 0;
	/**
	 * Creates a floor manager with the given starting quadrant. Fails if given null.
	 */
	public FloorManager(FloorQuadSnapshot fqs1) {
		floor = fqs1;
		bestFloor = fqs1;
		metric = FloorQuadSnapshot.calculateFullMetric(fqs1);
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
			//5 recalculate metric (happens inside the function)
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
			//9 if display set and at least ~100 milliseconds since last draw, queue draw.
			if (display != null && lastDraw + 100 < System.currentTimeMillis()) {
				javax.swing.SwingUtilities.invokeLater(() -> {display.update(floor, metric);});
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
	 * Returns the best metric found so far.
	 */
	public int getLastMetric() {
		return metric;
	}
	/**
	 * Returns the best layout found so far.
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
		final int newMetric = FloorQuadSnapshot.calculateFullMetric(newLayout);
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
	public void setPanel(FloorPanel panel) {
		display = panel;
	}

}
