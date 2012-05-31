package ch.hsr.hapdroid.graphlet;

import java.util.Vector;

import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.input.touch.TouchEvent;
import ch.hsr.hapdroid.graphlet.edge.Edge;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import ch.hsr.hapdroid.transaction.Node;
import ch.hsr.hapdroid.transaction.NodeList;

public class Area extends Rectangle{
	
	private static final String LOGTAG = "hapdroid.Area";
	private static float NODESPACING = 10;
	private boolean mGrabbed = false;
	private float firstTouchY;
	private float initialY;
	private final float initHeight;
	private GraphletNodeList nodes;
	private Vector<Edge> edges;
	
	public Area(float pX, float pY, float pWidth, float pHeight) {
		super(pX, pY, pWidth, pHeight);
		initHeight = pHeight;
		nodes = new GraphletNodeList();
		edges = new Vector<Edge>();
	}

	public void addAllNodes(NodeList<?> nodeList) {
		for( Node<?> node : nodeList){
			GraphletNode graphletNode = new GraphletNode(node);
			this.addNode(graphletNode);
		}
	}

	/**
	 *
	 * must return null so the graphlet can get the existing node
	 *
	 * @param node
	 * @return
	 */
	public GraphletNode addNode(GraphletNode node){
		if(nodes.add(node)){
			this.attachChild(node);
			updateNodePositions();
			return node;
		}
		return null;
	}
	
	public GraphletNode getNode(GraphletNode node){
		return nodes.getNode(node);
	}
	
	public void addEdge(Edge edge){
		edges.add(edge);
	}

	public void updateEdges(){
		for(Edge edge : edges){
			edge.update();
		}
	}

	private void updateNodePositions() {
		//Every node with it's spacing plus the extra spacing at the top
		float height = NODESPACING;
		for(GraphletNode node : nodes){
			height = height + node.getHeight() + NODESPACING;
		}
		super.setHeight(height); 
		super.setPosition(this.getX(), (initHeight/2)-(this.getHeight()/2)); //Set the area to display center
		
		float nodeY = NODESPACING + nodes.firstElement().getHeight()/2;
		for(GraphletNode node : nodes){
			node.setPosition(this.getWidth()/2, nodeY);
			nodeY = nodeY + node.getHeight() + NODESPACING;
		}
	}
	
	@Override
	public void setPosition(final float pX, final float pY) {
		super.setPosition(pX, pY);
		updateEdges();
	}
	
	public void clear(){
		detachChildren();
		nodes.clear();
		edges.clear();
	}
	
	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		switch(pSceneTouchEvent.getAction()) {
			case TouchEvent.ACTION_DOWN:
				//this.setScale(1.25f);
				if(!this.mGrabbed){
					firstTouchY = pSceneTouchEvent.getY();
					initialY = this.getY();
					this.mGrabbed = true;
				}
				break;
			case TouchEvent.ACTION_MOVE:
				if(this.mGrabbed) {
					this.setPosition(this.getX(), initialY + (pSceneTouchEvent.getY() - firstTouchY));
				}
				break;
			case TouchEvent.ACTION_UP:
				if(this.mGrabbed) {
					this.mGrabbed = false;
					//this.setScale(1.0f);
				}
				break;
		}
		return true;
	}
}

