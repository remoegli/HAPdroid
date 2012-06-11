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
	
	public static Font getFont(){
		return sFont;
	}

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
	
	public void update(HAPGraph graphlet) {
		Log.v(LOG_TAG, "updating Graphlet");
		clear();
		hapGraphlet = graphlet;
		Log.v(LOG_TAG, "Graphlet HashCode: " + graphlet.hashCode());
		
		Log.v(LOG_TAG, "drawing Nodes");
		srcIPArea.addAllNodes(graphlet.getSrcIpList());
		protoArea.addAllNodes(graphlet.getProtoList());
		srcPortArea.addAllNodes(graphlet.getSrcPortList());
		dstPortArea.addAllNodes(graphlet.getDstPortList());
		dstIPArea.addAllNodes(graphlet.getDstIpList());
		
		Log.v(LOG_TAG, "drawing Edges");
		findEdges(srcIPArea, protoArea, BaseEdge.class);
		findEdges(protoArea, srcPortArea, BaseEdge.class);
		findEdges(srcPortArea, dstPortArea, DirectedEdge.class);
		findEdges(dstPortArea, dstIPArea, LabeledEdge.class);
		
		refreshEdges();
	}

	//TODO: Review
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

	//TODO: Review
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
	
	private void refreshEdges(){
		for(Area area : areas){
			area.updateEdges();
		}
		this.sortChildren();
	}

	private void clear(){
		for(BaseEdge edge : edges){
			this.detachChild(edge);
		}
		edges.clear();
		
		for(Area area : areas){
			area.clear();
		}
	}

	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		return true;
	}
	
}
