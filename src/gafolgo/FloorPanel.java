/**
 * 
 */
package gafolgo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Benjamin Groman
 *
 */
class FloorPanel extends JPanel {
	private static final long serialVersionUID = -7395829330322543496L;
	public static final int SIZE = FloorQuadSnapshot.SIZE;
	
	JPanel gridPanel;
	JLabel[] cells = new JLabel[SIZE*SIZE];
	JLabel tag;

	/**
	 * Creates a new FloorPanel that displays the given floor and metric.
	 */
	public FloorPanel(FloorQuadSnapshot floor, int metric) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
		this.add(gridPanel);
		tag = new JLabel();
		this.add(tag);
		
		//initialize cells
		for(int i = 0; i < cells.length; i++) {
			cells[i] = new JLabel();
			cells[i].setMinimumSize(new Dimension(10, 10));
		}
		//first painting
		update(floor, metric);

	}
	/**
	 * Updates the displayed colors and text
	 */
	void update(FloorQuadSnapshot floor, int metric) {
		int index = 0;
		for(Flavor[] row : floor.machines) {
			for (Flavor col : row) {
				cells[index].setBackground(getColor(col));
				index++;
			}
		}
		tag.setText("" + metric);
	}
	/**
	 * Converts from a Flavor to the associated Color, returning black as a default.
	 * @param mac The machine to be drawn
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
