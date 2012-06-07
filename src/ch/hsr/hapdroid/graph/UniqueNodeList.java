package ch.hsr.hapdroid.graph;

import ch.hsr.hapdroid.graph.node.Node;

/**
 * This class has the same functionality as {@link NodeList} with 
 * the sole exception that not only summarized nodes will be 
 * grouped but any node.
 * 
 * @author "Dominik Spengler"
 *
 * @param <T>
 */
public class UniqueNodeList<T> extends NodeList<T> {
	
	@Override
	protected boolean isSummarized(Node<T> node) {
		return true;
	}
}
