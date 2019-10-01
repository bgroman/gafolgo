/**
 * 
 */
package gafolgo;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Benjamin Groman
 * This class represents a single solution graphically.
 */
class FloorPanel extends JPanel {
	/**
	 * This is just here to fix a warning.
	 */
	private static final long serialVersionUID = -7395829330322543496L;
	/**
	 * Allows the size to be used without typing FloorQuadSnapshot repeatedly.
	 */
	public static final int SIZE = FloorQuadSnapshot.SIZE;
	/**
	 * Organizes the solution elements.
	 */
	private final JPanel gridPanel;
	/**
	 * Allows the solution elements to be updated.
	 * This is done sequentially, so this can be a single-dimension array.
	 */
	JLabel[] cells = new JLabel[SIZE*SIZE];
	/**
	 * This is where the affinity metric is displayed.
	 * It is not in the gridPanel.
	 */
	JLabel tag;

	/**
	 * Creates a new FloorPanel that displays the given floor and metric.
	 */
	public FloorPanel(FloorQuadSnapshot floor, int metric) {
		//vertical orientation
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//create the grid for the solution
		gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
		//ensure the grid is included when drawing
		this.add(gridPanel);
		//the text will be set by the update() method
		tag = new JLabel();
		//make sure the tag is included when drawing
		this.add(tag);
		
		//initialize cells (must use index to actually initialize slots)
		for(int i = 0; i < cells.length; i++) {
			//the text can be almost anything, but this was reasonably square and pleasing with the default font
			cells[i] = new JLabel(" O ");
			//make the background visible - initially forgot to include
			cells[i].setOpaque(true);
			//include the cell when drawing
			gridPanel.add(cells[i]);
		}
		//first painting
		update(floor, metric);

	}
	/**
	 * Updates the displayed colors and text.
	 * @param floor The solution to be displayed.
	 * @param metric The value of the solution to be displayed.
	 */
	void update(FloorQuadSnapshot floor, int metric) {
		//keep track of this separately because the cells array has one dimension 
		int index = 0;
		//update the layout display
		for(int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; row++) {
				//background should be the color corresponding to the machine at this location
				cells[index].setBackground(getColor(floor.machine(row, col)));
				//keep the cell index on track
				index++;
			}
		}
		//old code included for reference - this worked when machines was accessible
		/*for(Flavor[] row : floor.machines) {
			for (Flavor col : row) {
				cells[index].setBackground(getColor(col));
				index++;
			}
		}*/
		//update the metric display
		tag.setText("" + metric);
	}
	/**
	 * Converts from a Flavor to the associated Color, returning black as a default.
	 * @param mac The machine to be drawn.
	 * @return The color of the machine.
	 */
	public static Color getColor(Flavor mac) {
		if (mac == Flavor.Blue) return Color.blue;
		else if (mac == Flavor.Green) return Color.green;
		else if (mac == Flavor.Yellow) return Color.yellow;
		else if (mac == Flavor.Red) return Color.red;
		else return Color.black;
	}

}
