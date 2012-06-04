package ch.hsr.hapdroid.transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DuplicateNodeList<T> extends NodeList<T> {
	
	private List<Node<T>> mNodeList;

	public DuplicateNodeList() {
		mNodeList = new ArrayList<Node<T>>();
	}

	@Override
	public Node<T> add(Node<T> node) {
		mNodeList.add(node);
		return node;
	}
	
	@Override
	public void clear() {
		mNodeList.clear();
	}

	@Override
	public Iterator<Node<T>> iterator() {
		return mNodeList.iterator();
	}

}
