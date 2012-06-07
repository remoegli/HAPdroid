package ch.hsr.hapdroid.graph.node;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.hapdroid.graph.Transaction;


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
	
	protected void setValue(V value){
		this.value = value;
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
	
	public Transaction getTransaction() {
		return mTransaction;
	}
}