package ch.hsr.hapdroid.graph.node;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.hapdroid.graph.HAPGraph;
import ch.hsr.hapdroid.graph.Transaction;

/**
 * Represents a node of a {@link Transaction} as well as a node of
 * the {@link HAPGraph}.
 * 
 * @author "Dominik Spengler"
 *
 * @param <V> the value inside the node
 */
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
	
	private boolean addNode(Node<V> node) {
		return nodes.add(node);
	}
	
	/**
	 * Getter for the stored value.
	 * 
	 * @return
	 */
	public V getValue() {
		return value;
	}
	
	/**
	 * Setter for the stored value.
	 * 
	 * @param value
	 */
	protected void setValue(V value){
		this.value = value;
	}
	
	private int getId() {
		return id;
	}
	
	/**
	 * Whether or not the node is summarized.
	 * 
	 * @return true if the node is summarized,
	 * 		false otherwise
	 */
	public boolean isSummarized(){
		return mSummarized;
	}
	
	/**
	 * Sets the node as summarized or not.
	 */
	public void setSummarized(boolean b) {
		mSummarized = b;
	}

	private boolean isActive() {
		return active;
	}

	private void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value.toString();
	}
	
	/**
	 * Getter for the transaction the node belongs to.
	 * 
	 * @return {@link Transaction} the node belongs to
	 */
	public Transaction getTransaction() {
		return mTransaction;
	}
}
