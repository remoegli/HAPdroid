package ch.hsr.hapdroid.transaction;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map;

public class NodeList<T> implements Iterable<Node<T>>{
	private Map<T, Node<T>> mNodeList;
	
	public NodeList() {
		mNodeList = new TreeMap<T, Node<T>>();
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

}
