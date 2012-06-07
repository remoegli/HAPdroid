package ch.hsr.hapdroid.graph;

import ch.hsr.hapdroid.graph.node.Node;



public class UniqueNodeList<T> extends NodeList<T> {
	
	@Override
	protected boolean isSummarized(Node<T> node) {
		return true;
	}
}
