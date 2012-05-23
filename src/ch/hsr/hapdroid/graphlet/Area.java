package ch.hsr.hapdroid.graphlet;

import java.util.Vector;

import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.input.touch.TouchEvent;

import ch.hsr.hapdroid.graphlet.edge.Edge;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;


public class Area extends Rectangle{
	
	private static float NODESPACING = 10;
	private static float NODEHEIGHT = 40;
	
	private boolean mGrabbed = false;
	private float firstTouchY;
	private float initialY;
	private final float initHeight;
	private Vector<GraphletNode> nodes;
	private Vector<Edge> edges;
	
	public Area(float pX, float pY, float pWidth, float pHeight) {
		super(pX, pY, pWidth, pHeight);
		initHeight = pHeight;
		nodes = new Vector<GraphletNode>();
		edges = new Vector<Edge>();
	}

	public void addNode(GraphletNode node){
		nodes.add(node);
		updateNodePositions();
		this.attachChild(node);
	}
	
	private void updateNodePositions() {		
		super.setHeight((nodes.size()*(NODEHEIGHT+NODESPACING))+NODESPACING); //Every node with it's spacing plus the extra spacing at the top
		super.setPosition(this.getX(), (initHeight/2)-(this.getHeight()/2)); //Set the area to display center
		
		float nodeY = NODESPACING + NODEHEIGHT/2;
		for(GraphletNode node : nodes){
			node.setPosition(this.getWidth()/2, nodeY);
			nodeY = nodeY + NODEHEIGHT + NODESPACING;
		}
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
	
	@Override
	public void setPosition(final float pX, final float pY) {
		super.setPosition(pX, pY);
		updateEdges();
	}
	
	public void addEdge(Edge edge){
		edges.add(edge);
		if(!edge.hasParent()){
			this.attachChild(edge);
		}
	}
	
	public void updateEdges(){
		for(Edge edge : edges){
			edge.update();
		}
	}
}

