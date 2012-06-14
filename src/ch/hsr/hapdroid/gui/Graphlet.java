package ch.hsr.hapdroid.gui;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.jgrapht.graph.DefaultEdge;

import android.util.Log;

import ch.hsr.hapdroid.graph.HAPGraph;
import ch.hsr.hapdroid.gui.edge.BaseEdge;
import ch.hsr.hapdroid.gui.edge.DirectedEdge;
import ch.hsr.hapdroid.gui.edge.LabeledEdge;
import ch.hsr.hapdroid.gui.node.GraphletNode;

/**\class Graphlet
 * Graphlet is the top level container of all the other GUI elements.
 * A GUI object which is not attached to the Graphlet or one of its children
 * is not drawn.
 * 
 * @author Remo Egli
 *
 */
public class Graphlet extends Scene{

	private static final String LOG_TAG = "HAPdroid.Graphlet";
	private static Font sFont;
	private Vector<Area> areas;
	private Vector<BaseEdge> edges;
	private final float CAMERA_HEIGHT;
	private final float AREA_WIDTH;
	private final float AREA_ALPHA = 0.2f;
	private final float COLOR_DIV = 255;
	private Area srcIPArea;
	private Area protoArea;
	private Area srcPortArea;
	private Area dstPortArea;
	private Area dstIPArea;
	private HAPGraph hapGraphlet;
	
	/**
	 * 
	 * @param cameraWidth
	 * @param cameraHeight
	 * @param aFont
	 */
	public Graphlet(float cameraWidth, float cameraHeight, Font aFont){
		super();
		areas = new Vector<Area>();
		edges = new Vector<BaseEdge>();
		CAMERA_HEIGHT = cameraHeight;
		AREA_WIDTH = cameraWidth/5;
		sFont = aFont;
		
		AreaLabels.setFont(sFont);
		GraphletNode.setFont(sFont);
		LabeledEdge.setFont(sFont);
		
		createAreas();
	}
	
	/**
	 * Creates and aligns an area for each field of the Berkeley socket model:
	 * source IP, protocol, source port, destination port, destination IP
	 */
	private void createAreas() {
		AreaLabels areaLabels = new AreaLabels(AREA_WIDTH);
		areaLabels.setZIndex(15);
		this.attachChild(areaLabels);
		
		//Areas
		srcIPArea = new Area(0, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(srcIPArea);
		srcIPArea.setColor(117/COLOR_DIV, 12/COLOR_DIV, 232/COLOR_DIV, AREA_ALPHA);
		protoArea = new Area(AREA_WIDTH, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(protoArea);
		protoArea.setColor(255/COLOR_DIV, 37/COLOR_DIV, 13/COLOR_DIV, AREA_ALPHA);
		srcPortArea = new Area(AREA_WIDTH*2, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(srcPortArea);
		srcPortArea.setColor(0/COLOR_DIV, 192/COLOR_DIV, 255/COLOR_DIV, AREA_ALPHA);
		dstPortArea = new Area(AREA_WIDTH*3, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(dstPortArea);
		dstPortArea.setColor(21/COLOR_DIV, 232/COLOR_DIV, 12/COLOR_DIV, AREA_ALPHA);
		dstIPArea = new Area(AREA_WIDTH*4, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(dstIPArea);
		dstIPArea.setColor(255/COLOR_DIV, 209/COLOR_DIV, 8/COLOR_DIV, AREA_ALPHA);
		
		//Attach Areas to Scene and register TouchHandler
		for(Area area : areas){
			area.setZIndex(10);
			this.attachChild(area);
			this.registerTouchArea(area);
		}
		this.sortChildren();
	}
	
	/**
	 * 
	 * @param graph The Graphlet will render and display this graph data structure
	 */
	public void update(HAPGraph graph) {
		Log.v(LOG_TAG, "updating Graphlet");
		clear();
		hapGraphlet = graph;
		Log.v(LOG_TAG, "Graphlet HashCode: " + graph.hashCode());
		
		Log.v(LOG_TAG, "drawing Nodes");
		srcIPArea.addAllNodes(graph.getSrcIpList());
		protoArea.addAllNodes(graph.getProtoList());
		srcPortArea.addAllNodes(graph.getSrcPortList());
		dstPortArea.addAllNodes(graph.getDstPortList());
		dstIPArea.addAllNodes(graph.getDstIpList());
		
		Log.v(LOG_TAG, "drawing Edges");
		findEdges(srcIPArea, protoArea, BaseEdge.class);
		findEdges(protoArea, srcPortArea, BaseEdge.class);
		findEdges(srcPortArea, dstPortArea, DirectedEdge.class);
		findEdges(dstPortArea, dstIPArea, LabeledEdge.class);
		
		refreshEdges();
	}

	/**
	 * This method is used to determine all the edges which connect the nodes
	 * of two specific areas.
	 * 
	 * @param left The left Area containing nodes
	 * @param right The right Area containing nodes
	 * @param edgetype Depending on which type of node (IP, port, protocol) the area contains different edges are used 
	 */
	private void findEdges(Area left, Area right, Class<?> edgetype) {
		Iterator<GraphletNode> iterator = left.getNodeIterator();
		while(iterator.hasNext()){
			GraphletNode leftNode = iterator.next();
			Log.v(LOG_TAG, "finding edge for node: " + leftNode.getNode().toString());
			Set<DefaultEdge> edges = hapGraphlet.edgesOf(leftNode.getNode());
			for(DefaultEdge edge : edges){
				GraphletNode rightNode = right.getNode(new GraphletNode(hapGraphlet.getEdgeTarget(edge)));
				if(rightNode != null){
					Log.v(LOG_TAG, "creating edge for node: " + leftNode.getNode().toString() + "/" + rightNode.getNode().toString());
					createEdge(leftNode, rightNode, edgetype);
				}
			}
		}
	}

	/**
	 * 
	 * @param left The left GraphletNode the edge connects to 
	 * @param right The right GraphletNode the edge connects to
	 * @param edgetype The type of edge it represents
	 */
	private void createEdge(GraphletNode left, GraphletNode right, Class<?> edgetype){
		BaseEdge edge;
		if(edgetype.equals(DirectedEdge.class)){
			Log.v(LOG_TAG, "creating a DirectedEdge");
			edge = new DirectedEdge(left, right, left.getNode().getTransaction());
		} else if(edgetype.equals(LabeledEdge.class)){
			Log.v(LOG_TAG, "creating a LabeledEdge");
			edge = new LabeledEdge(left, right, left.getNode().getTransaction());
		} else{
			Log.v(LOG_TAG, "creating a BaseEdge");
			edge = new BaseEdge(left, right);
		}
		
		edge.setZIndex(5);
		edges.add(edge);
		this.attachChild(edge);
		
		((Area)left.getParent()).addEdge(edge);
		((Area)right.getParent()).addEdge(edge);
	}
	
	/**
	 * Recalculates the position of each edge
	 */
	private void refreshEdges(){
		for(Area area : areas){
			area.updateEdges();
		}
		this.sortChildren();
	}

	/**
	 * Removes the graphlet and all related objects
	 */
	private void clear(){
		for(BaseEdge edge : edges){
			this.detachChild(edge);
		}
		edges.clear();
		
		for(Area area : areas){
			area.clear();
		}
	}

	/**
	 * Enables the Graphlet to receive TouchEvents
	 * 
	 * @param pScene
	 * @param pSceneTouchEvent
	 * @return
	 */
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		return true;
	}
	
}
