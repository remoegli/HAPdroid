package ch.hsr.hapdroid.gui.edge;

import org.anddev.andengine.entity.text.Text;

import ch.hsr.hapdroid.graph.Transaction;
import ch.hsr.hapdroid.gui.node.GraphletNode;

/**\class DirectedEdge
 * The DirectedEdge extends the LabeledEdge by adding a graphical representation of the flow direction.
 * @see ch.hsr.hapdroid.gui.edge.DirectionType
 * 
 * @author Remo Egli
 *
 */
public class DirectedEdge extends LabeledEdge {

	/**
	 * 
	 * @param left The left GraphletNode this edge connects to 
	 * @param right The right GraphletNode this edge connects to
	 * @param trans The Transaction which contains the values for the edge label
	 */
	public DirectedEdge(GraphletNode left, GraphletNode right, Transaction trans) {
		super(left, right, trans);
	}
	
	/**
	 * This method is used to determine the edge label on runtime
	 * @return Returns the label text
	 */
	protected Text getText(){
		return new Text(30, 0, sFont, type.leftArrow() + transaction.getBytes() + "(" + transaction.getPackets() + ")" + type.rightArrow());
	}
		
}
