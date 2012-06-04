package ch.hsr.hapdroid.graphlet.edge;

import org.anddev.andengine.entity.text.Text;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import ch.hsr.hapdroid.transaction.Transaction;

public class DirectedEdge extends LabeledEdge {

	public DirectedEdge(GraphletNode left, GraphletNode right, Transaction trans) {
		super(left, right, trans);
	}
	
	protected Text getText(){
		return new Text(30, 0, aFont, type.leftArrow() + transaction.getBytes() + "(" + transaction.getPackets() + ")" + type.rightArrow());
	}
		
}
