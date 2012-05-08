package ch.hsr.hapdroid.transaction;

import java.util.ArrayList;
import java.util.List;

public class Node<V>{
	private int id;
	private boolean active;
	private V value;
	private List<Node<V>> nodes;

	public Node(int id, V value) {
		this.value = value;
		this.id = id;
		nodes = new ArrayList<Node<V>>();
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
		return nodes.size() > 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Node<?>){
			Node<?> n = (Node<?>) o;
			return n.getId() == this.getId();
		}
			
		return super.equals(o);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
}
