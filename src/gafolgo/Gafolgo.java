/**
 * 
 */
package gafolgo;

import java.io.IOException;

import javax.swing.JFrame;

/**
 * @author Benjamin Groman
 *
 */
public class Gafolgo {
	private static JFrame myFrame;
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
		initializeGUI(fms);
		for (FloorManager fm : fms) {
			//fm.start();
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
			myFrame.dispose();
		}
		for (FloorManager fm : fms) {
			System.out.println(fm.getLastMetric() + " < " + fm.getBestMetric());
		}
	}
	private static void initializeGUI(FloorManager[] fms) {
		myFrame = new JFrame("Gafolgo");
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		for (FloorManager fm : fms) {
			myFrame.getContentPane().add(new FloorPanel(fm.getBestLayout(), fm.getBestMetric()));
		}
		myFrame.pack();
		myFrame.setVisible(true);
	}

}
