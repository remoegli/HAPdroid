package ch.hsr.hapdroid.gui.edge;

import org.anddev.andengine.entity.text.Text;

import ch.hsr.hapdroid.graph.Transaction;
import ch.hsr.hapdroid.gui.node.GraphletNode;

public class DirectedEdge extends LabeledEdge {

	public DirectedEdge(GraphletNode left, GraphletNode right, Transaction trans) {
		super(left, right, trans);
	}
	
	protected Text getText(){
		return new Text(30, 0, sFont, type.leftArrow() + transaction.getBytes() + "(" + transaction.getPackets() + ")" + type.rightArrow());
	}
		
}
