package ch.hsr.hapdroid.graphlet;

import java.util.Vector;

import ch.hsr.hapdroid.graphlet.node.GraphletNode;

public class GraphletNodeList extends Vector<GraphletNode>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8084004070651954818L;
	
	@Override
	public boolean add(GraphletNode node){
		if(!contains(node)){
			super.add(node);
			return true;
		}
		return false;
	}

	public GraphletNode getNode(GraphletNode node) {
		if(indexOf(node)>-1){
			return get(indexOf(node));
		}
		return null;
	}

}
