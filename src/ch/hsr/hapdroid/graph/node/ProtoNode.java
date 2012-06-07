package ch.hsr.hapdroid.graph.node;

import ch.hsr.hapdroid.graph.Transaction;
import ch.hsr.hapdroid.network.Proto;

public class ProtoNode extends Node<Proto> {

	public ProtoNode(Proto value, Transaction t) {
		super(value, t);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ProtoNode){
			ProtoNode n = (ProtoNode) o;
			return n.getValue().equals(getValue());
		}
			
		return super.equals(o);
	}
}
