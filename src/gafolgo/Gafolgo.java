/**
 * This acronym seems sufficiently odd that there shouldn't be conflicts.
 * It stands for: Genetic Algorithm For Optimal Location of Gas Operators
 */
package gafolgo;


import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JFrame;

/**
 * @author Benjamin Groman
 * Genetic Algorithm For Optimal Location of Gas Operators -The first assignment for CSC 375 at SUNY Oswego.
 */
public class Gafolgo {
	/**
	 * The window for the GUI. Exists here so the main thread can kill it.
	 */
	private static JFrame myFrame;
	/**
	 * The number of threads to use for the algorithm. 32 was the minimum.
	 */
	private static final int numThreads = 64;
	/**
	 * The main function handles launching floor manager threads and prints the best results found,
	 * unless the GUI terminates the program before the command line does.
	 * @param args not used.
	 */
	public static void main(String[] args) {
		//declare the array
		final FloorManager[] fms = new FloorManager[numThreads];
		//establish an initial floor layout (random because the constructor is random)
		final FloorQuadSnapshot startingFloor = new FloorQuadSnapshot();
		//have to use an indexed loop so that the actual array gets initialized
		//a foreach loop would simply update the temporary variable
		for (int i = 0; i < fms.length; i++) {
			//initialize the floor manager
			fms[i] = new FloorManager(startingFloor);
			//print the metric. This line should print the same thing for every iteration of the loop
			System.out.println(fms[i].getBestMetric());
		}
		
		//set up the GUI with the floor managers
		initializeGUI(fms);
		
		//start the threads
		for (FloorManager fm : fms) {
			fm.start();
		}
		//wait for user input to terminate
		try {
			//any input will do, we don't care about the result
			//pressing enter or ending the stream will trigger the normal exit
			System.in.read();
		}
		catch (IOException e) {
			//if an exception is thrown, that's just as good a reason to move on to cleanup
		}
		//ensure this stuff gets run
		finally {
			//interrupt all the threads, which will cause them to terminate
			//since they are daemons, we can afford for the interrupt to fail to terminate any given thread
			for (FloorManager fm : fms) {
				fm.interrupt();
			}
			//close the GUI
			myFrame.dispose();
			//print the current and best metrics found by each thread
			//provides a means of determining that the best metric is being properly tracked
			for (FloorManager fm : fms) {
				System.out.println(fm.getLastMetric() + " <= " + fm.getBestMetric());
			}
		}
	}
	/**
	 * Creates the GUI so progress can be seen.
	 * Precondition: the calling thread is the only thread currently running.
	 * @param fms The set of floor managers whose statuses need to be displayed.
	 */
	private static void initializeGUI(FloorManager[] fms) {
		//new window with title
		myFrame = new JFrame("Gafolgo");
		//make sure closing the GUI terminates the whole program
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//calculate a reasonably optimal number of rows for the given number of threads
		//try to make a square, but explicitly round down because the floor panels are vertically-oriented
		//cap at (64 / (FloorPanel.SIZE + 1)) because that's the approximate max my screen can handle without squeezing the tags 
		final int rows = (int)Math.min((64 / (FloorPanel.SIZE + 1)), Math.floor(Math.sqrt(numThreads)));
		//create a layout with the number of rows from above, any number of columns, 20 pixels horizontal gap, and no vertical gap
		myFrame.setLayout(new GridLayout(rows, 0, 20, 0));
		
		//for each floor manager
		for (FloorManager fm : fms) {
			//create a floor panel with the best layout and metric,
			//which should be the same as the working layout and metric
			FloorPanel panel = new FloorPanel(fm.getBestLayout(), fm.getBestMetric());
			//add the panel to the frame
			myFrame.getContentPane().add(panel);
			//make sure the floor manager knows which panel to update
			fm.setPanel(panel);
		}
		//ensure the window shows everything without excess space
		myFrame.pack();
		//show the window
		myFrame.setVisible(true);
	}

}
