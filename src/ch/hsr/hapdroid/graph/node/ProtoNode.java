package ch.hsr.hapdroid.graph.node;

import ch.hsr.hapdroid.graph.Transaction;
import ch.hsr.hapdroid.network.Proto;

/**
 * Special handling of the equals method.
 * 
 * @author "Dominik Spengler"
 *
 */
public class ProtoNode extends Node<Proto> {

	public ProtoNode(Proto value, Transaction t) {
		super(value, t);
	}

	/**
	 * Make sure the equals method compares the stored 
	 * {@link Proto} value.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof ProtoNode){
			ProtoNode n = (ProtoNode) o;
			return n.getValue().equals(getValue());
		}
			
		return super.equals(o);
	}
}
