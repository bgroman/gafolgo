/**
 * 
 */
package gafolgo;

import java.io.IOException;

/**
 * @author Benjamin Groman
 *
 */
public class Gafolgo {

	/**
	 * @param args not used.
	 */
	public static void main(String[] args) {
		//testing
		FloorManager[] fms = new FloorManager[10];
		FloorQuadSnapshot startingFloor = new FloorQuadSnapshot();
		for (int i = 0; i < fms.length; i++) {
			fms[i] = new FloorManager(startingFloor);
			System.out.println(fms[i].getBestMetric());
		}
		for (FloorManager fm : fms) {
			fm.start();
		}
		//wait for user input to terminate
		try {
			//any input will do
			System.in.read();
		}
		catch (IOException e) {
			//might as well just end now anyway
		}
		for (FloorManager fm : fms) {
			fm.interrupt();
		}
		for (FloorManager fm : fms) {
			System.out.println(fm.getLastMetric() + " < " + fm.getBestMetric());
		}
	}

}
