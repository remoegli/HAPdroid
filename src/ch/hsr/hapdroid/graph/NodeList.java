package ch.hsr.hapdroid.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.hsr.hapdroid.graph.node.Node;

/**
 * A list of {@link Node} where summarized nodes are grouped.
 * 
 * Summarized nodes are not added to the list if they already exist.
 * 
 * @author "Dominik Spengler"
 *
 * @param <T> the type of {@link Node} elements inside the list
 */
public class NodeList<T> implements Iterable<Node<T>>{
	private List<Node<T>> mNodeList;
	
	public NodeList() {
		mNodeList = new ArrayList<Node<T>>();
	}

	/**
	 * Adds the node to the list.
	 * 
	 * If a {@link Node} describing node already exists inside the list,
	 * that {@link Node} will be returned. Describes in this Context means
	 * that it is a summarized node with the same type and value.
	 * 
	 * @param node the {@link Node} to be added
	 * @return the {@link Node} just added or the {@link Node} inside the list
	 * 		that describes node
	 */
	public Node<T> add(Node<T> node) {
		Node<T> n = get(node);
		if (n == null){
			mNodeList.add(node);
			n = node;
		}
		return n;
	}
	
	private Node<T> get(Node<T> node) {
		if (!isSummarized(node))
			return null;
		
		for (Node<T> n : mNodeList){
			if (n.equals(node))
				return n;
		}
		return null;
	}

	protected boolean isSummarized(Node<T> node) {
		return node.isSummarized();
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Node<T>> iterator() {
		return mNodeList.iterator();
	}

	/**
	 * Clear all nodes from the list.
	 */
	public void clear() {
		mNodeList.clear();
	}

}
