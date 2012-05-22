package ch.hsr.hapdroid.transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NodeList<T> implements Iterable<Node<T>>{
	private Map<T, Node<T>> mNodeList;
	
	public NodeList() {
		mNodeList = new HashMap<T, Node<T>>();
	}

	public Node<T> add(Node<T> node) {
		Node<T> n = mNodeList.get(node.getValue());
		if(n == null){
			mNodeList.put(node.getValue(), node);
			n = node;
		}
		else{
			n.addNode(node);
		}
		
		return n;
	}

	@Override
	public Iterator<Node<T>> iterator() {
		return mNodeList.values().iterator();
	}

	public void clear() {
		mNodeList.clear();
	}

}
