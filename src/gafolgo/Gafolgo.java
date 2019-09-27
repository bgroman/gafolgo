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
		FloorManager fm = new FloorManager(new FloorQuadSnapshot());
		System.out.println(fm.getBestMetric());
		fm.start();
		//wait for user input to terminate
		try {
			//any input will do
			System.in.read();
		}
		catch (IOException e) {
			//might as well just end now anyway
		}
		fm.interrupt();
		System.out.println(fm.getBestMetric());
	}

}
