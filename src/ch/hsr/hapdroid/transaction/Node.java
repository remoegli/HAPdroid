package ch.hsr.hapdroid.transaction;

import java.util.ArrayList;
import java.util.List;

public class Node<V>{
	private int id;
	private boolean active;
	private V value;
	private List<Node<V>> nodes;
	private boolean mSummarized;
	private Transaction mTransaction;

	public Node(V value, Transaction t) {
		this.value = value;
		nodes = new ArrayList<Node<V>>();
		mSummarized = false;
		mTransaction = t;
	}
	
	public boolean addNode(Node<V> node) {
		return nodes.add(node);
	}
	
	public V getValue() {
		return value;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isSummarized(){
		return mSummarized;
	}
	
	public void setSummarized(boolean b) {
		mSummarized = b;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Node<?>){
			Node<?> n = (Node<?>) o;
			return n.getValue().equals(getValue());
		}
			
		return super.equals(o);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
}
