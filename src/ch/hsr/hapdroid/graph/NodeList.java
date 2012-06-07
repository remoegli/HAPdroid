package ch.hsr.hapdroid.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.hsr.hapdroid.graph.node.Node;


public class NodeList<T> implements Iterable<Node<T>>{
	private List<Node<T>> mNodeList;
	
	public NodeList() {
		mNodeList = new ArrayList<Node<T>>();
	}

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

	@Override
	public Iterator<Node<T>> iterator() {
		return mNodeList.iterator();
	}

	public void clear() {
		mNodeList.clear();
	}

}
