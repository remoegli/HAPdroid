package ch.hsr.hapdroid.graphlet;

import java.util.Iterator;
import java.util.Vector;

import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.input.touch.TouchEvent;
import ch.hsr.hapdroid.graphlet.edge.BaseEdge;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import ch.hsr.hapdroid.transaction.Node;
import ch.hsr.hapdroid.transaction.NodeList;

/**\class Area
 * Area represents a container for all GraphletNodes of a certain type (for example "remotePort"-Area holds every "remotePort"-Node).
 * It's responsible for:
 * - aligning every GraphletNode contained 
 * - handling vertical scrolling (and blocking horizontal scrolling) 
 * - triggering update on all Edges connected to GraphletNodes in this Area
 * 
 * GraphletNodes need to be "attached" to their respective Area so AndEngine handles the drawing and screen update
 *    
 * @author Remo Egli
 *
 */
public class Area extends Rectangle{
	
//	private static final String LOGTAG = "hapdroid.Area";
	private static float NODESPACING = 10; /**< spacing between nodes */
	private boolean mGrabbed = false;
	private float firstTouchY;
	private float initialY;
	private final float initHeight;
	/**
	 * This collection is used for convenience and could be replaced by directly accessing the areas child objects,
	 * where the nodes are held. Extending AndEngine by adding a method providing access to the internal collection
	 * could be another option. 
	 */
	private Vector<GraphletNode> nodes;
	/**
	 * This collection can NOT be removed since the edges are NO child objects of the Area.
	 * It improves the performance by updating only those edges which are connected to the Area instance. 
	 */
	private Vector<BaseEdge> edges;
	
	/**
	 * 
	 * @param pX x value of top left corner, relative to the position of Graphlet
	 * @param pY y value of top left corner, relative to the position of Graphlet
	 * @param pWidth Width of the Area, usually display width/number of Areas
	 * @param pHeight Height of the Area, usually height of the display frame (= display height - notification bar)
	 */
	public Area(float pX, float pY, float pWidth, float pHeight) {
		super(pX, pY, pWidth, pHeight);
		initHeight = pHeight;
		nodes = new Vector<GraphletNode>();
		edges = new Vector<BaseEdge>();
	}

	/**
	 * A helper method to move creation of GraphletNode instances from Graphlet to the Area in charge. 
	 * @param nodeList List of all Nodes to be added as a GraphletNode to this Area.
	 */
	public void addAllNodes(NodeList<?> nodeList) {
		for( Node<?> node : nodeList){
			GraphletNode graphletNode = new GraphletNode(node);
			this.addNode(graphletNode);
		}
	}

	/**
	 * Responsible for:
	 * - updating the Node collection of the Area
	 * - attaching 
	 * @param node
	 */
	private void addNode(GraphletNode node){
		nodes.add(node);
		this.attachChild(node);
		updateNodePositions();
	}
	
	public GraphletNode getNode(GraphletNode node){
		if(nodes.indexOf(node)>-1){
			return nodes.get(nodes.indexOf(node));
		}
		return null;
	}
	
	public Iterator<GraphletNode> getNodeIterator(){
		return nodes.iterator();
	}
	
	public void addEdge(BaseEdge edge){
		edges.add(edge);
	}

	public void updateEdges(){
		for(BaseEdge edge : edges){
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

