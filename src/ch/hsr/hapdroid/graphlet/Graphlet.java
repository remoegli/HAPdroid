package ch.hsr.hapdroid.graphlet;

import java.util.Vector;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.input.touch.TouchEvent;

import ch.hsr.hapdroid.graphlet.edge.Edge;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import ch.hsr.hapdroid.graphlet.node.NodeType;

public class Graphlet extends Scene{

	private Vector<Area> areas;
//	private Vector<GraphletNode> nodes;
	
//	private final int CAMERA_WIDTH;
	private final int CAMERA_HEIGHT;
	private final int AREA_WIDTH;
	private Area srcIPArea;
	private Area protoArea;
	private Area srcPortArea;
	private Area dstPortArea;
	private Area dstIPArea;
	
	public Graphlet(int cameraWidth, int cameraHeight){
		super();
		areas = new Vector<Area>();
//		nodes = new Vector<GraphletNode>();
//		CAMERA_WIDTH = cameraWidth;
		CAMERA_HEIGHT = cameraHeight;
		AREA_WIDTH = cameraWidth/5;
		
		createAreas();
		addTestContent();
		
	}

	private void createAreas() {
		//Areas
		srcIPArea = new Area(0, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(srcIPArea);
		srcIPArea.setColor(0, 0.2f, 0.2f, 0.5f);
		protoArea = new Area(AREA_WIDTH, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(protoArea);
		protoArea.setColor(0.5f, 0, 0, 0.5f);
		srcPortArea = new Area(AREA_WIDTH*2, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(srcPortArea);
		srcPortArea.setColor(0, 0.5f, 0, 0.5f);
		dstPortArea = new Area(AREA_WIDTH*3, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(dstPortArea);
		dstPortArea.setColor(0, 0, 0.5f, 0.5f);
		dstIPArea = new Area(AREA_WIDTH*4, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(dstIPArea);
		dstIPArea.setColor(0.2f, 0.2f, 0, 0.5f);
		
		//Attach Areas to Scene and register TouchHandler
		for(Area area : areas){
			area.setZIndex(10);
			this.attachChild(area);
			this.registerTouchArea(area);
		}
	}
	
	private void createEdge(GraphletNode left, GraphletNode right, String label){
		Edge edge = new Edge(left, right, label);
		edge.setZIndex(5);
		this.attachChild(edge);
		
		((Area)left.getParent()).addEdge(edge);
		((Area)right.getParent()).addEdge(edge);
	}
	
	public void update(){
		for(Area area : areas){
			area.updateEdges();
		}
		this.sortChildren();
	}
	
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		return true;
	}
	
	
	private void addTestContent(){
		//CreateNodes
		GraphletNode srcipNode1 = new GraphletNode(NodeType.IP, "192.168.100.100");
		GraphletNode protoNode1 = new GraphletNode(NodeType.PROTO, "TCP");
		GraphletNode srcportNode1 = new GraphletNode(NodeType.PORT, "65128");
		GraphletNode dstportNode1 = new GraphletNode(NodeType.PORT, "80");
		GraphletNode dstipNode1 = new GraphletNode(NodeType.IP, "69.171.234.48");

		GraphletNode protoNode2 = new GraphletNode(NodeType.PROTO, "UDP");
		GraphletNode srcportNode2 = new GraphletNode(NodeType.PORT, "32123");
		
		//Add nodes to area
		srcIPArea.addNode(srcipNode1);
		protoArea.addNode(protoNode1);
		protoArea.addNode(protoNode2);
		srcPortArea.addNode(srcportNode1);
		srcPortArea.addNode(srcportNode2);
		dstPortArea.addNode(dstportNode1);
		dstIPArea.addNode(dstipNode1);

		//Create Edges
		createEdge(srcipNode1, protoNode1, "1.1");
		createEdge(srcipNode1, protoNode2, "1.2");
		createEdge(protoNode1, srcportNode1, "2.1");
		createEdge(protoNode2, srcportNode2, "2.2");
		createEdge(srcportNode1, dstportNode1, "3.1");
		createEdge(srcportNode2, dstportNode1, "3.2"); 
		createEdge(dstportNode1, dstipNode1, "4");

	}
	
}
