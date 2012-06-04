package ch.hsr.hapdroid.graphlet.edge;

import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import ch.hsr.hapdroid.transaction.Transaction;

public class DirectedEdge extends LabeledEdge {

	private DirectionType type;
	
	public DirectedEdge(GraphletNode left, GraphletNode right, Transaction trans) {
		super(left, right, trans);
		type = DirectionType.getType(trans);
		this.setColor(type.getRed(), type.getGreen(), type.getBlue());
	}

}
