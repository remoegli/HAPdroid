package ch.hsr.hapdroid.graphlet;

import java.util.Set;
import java.util.Vector;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.jgrapht.graph.DefaultEdge;

import ch.hsr.hapdroid.HAPGraphlet;
import ch.hsr.hapdroid.graphlet.edge.Edge;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import ch.hsr.hapdroid.transaction.Transaction;

public class Graphlet extends Scene{

	private static Font aFont;
	private Vector<Area> areas;
	private Vector<Edge> edges;
	private final float CAMERA_HEIGHT;
	private final float AREA_WIDTH;
	private final float AREA_ALPHA = 0.2f;
	private Area srcIPArea;
	private Area protoArea;
	private Area srcPortArea;
	private Area dstPortArea;
	private Area dstIPArea;
	private HAPGraphlet hapGraphlet;
	
	public Graphlet(float cameraWidth, float cameraHeight){
		super();
		areas = new Vector<Area>();
		edges = new Vector<Edge>();
		CAMERA_HEIGHT = cameraHeight;
		AREA_WIDTH = cameraWidth/5;
		
		createAreas();
//		addTestContent();

	}

	private void createAreas() {
		//TODO: Remove Coloring?
		AreaLabels areaLabels = new AreaLabels(AREA_WIDTH, aFont);
		areaLabels.setZIndex(15);
		this.attachChild(areaLabels);
		
		//Areas
		srcIPArea = new Area(0, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(srcIPArea);
		srcIPArea.setColor(0, 0.2f, 0.2f, AREA_ALPHA);
		protoArea = new Area(AREA_WIDTH, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(protoArea);
		protoArea.setColor(0.5f, 0, 0, AREA_ALPHA);
		srcPortArea = new Area(AREA_WIDTH*2, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(srcPortArea);
		srcPortArea.setColor(0, 0.5f, 0, AREA_ALPHA);
		dstPortArea = new Area(AREA_WIDTH*3, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(dstPortArea);
		dstPortArea.setColor(0, 0, 0.5f, AREA_ALPHA);
		dstIPArea = new Area(AREA_WIDTH*4, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(dstIPArea);
		dstIPArea.setColor(0.2f, 0.2f, 0, AREA_ALPHA);
		
		//Attach Areas to Scene and register TouchHandler
		for(Area area : areas){
			area.setZIndex(10);
			this.attachChild(area);
			this.registerTouchArea(area);
		}
		this.sortChildren();
	}
	
	private void createEdge(GraphletNode left, GraphletNode right, String label){
		Edge edge = new Edge(left, right, label);
		edge.setZIndex(5);
		edges.add(edge);
		this.attachChild(edge);
		
		((Area)left.getParent()).addEdge(edge);
		((Area)right.getParent()).addEdge(edge);
	}
	
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		return true;
	}
	
	private void clear(){
		for(Edge edge : edges){
			this.detachChild(edge);
		}
		edges.clear();
		
		for(Area area : areas){
			area.clear();
		}
	}
	
	public void update(HAPGraphlet graphlet) {
		hapGraphlet = graphlet;
		clear();
		
		srcIPArea.addAllNodes(graphlet.getSrcIpList());
		protoArea.addAllNodes(graphlet.getProtoList());
		srcPortArea.addAllNodes(graphlet.getSrcPortList());
		dstPortArea.addAllNodes(graphlet.getDstPortList());
		dstIPArea.addAllNodes(graphlet.getDstIpList());
		
		findEdges(srcIPArea, protoArea);
		findEdges(protoArea, srcPortArea);
		findEdges(srcPortArea, dstPortArea);
		findEdges(dstPortArea, dstIPArea);
		
		refreshEdges();
	}
	
	public void addTransaction(Transaction trans){
		hapGraphlet.add(trans);
		
		GraphletNode srcIP = new GraphletNode(trans.getSrcIp());
		GraphletNode srcIPNode = srcIPArea.addNode(srcIP);
		GraphletNode proto = new GraphletNode(trans.getProto());
		GraphletNode protoNode = protoArea.addNode(proto);
		GraphletNode srcPort = new GraphletNode(trans.getSrcPort());
		GraphletNode srcPortNode = srcPortArea.addNode(srcPort);
		GraphletNode dstPort = new GraphletNode(trans.getDstPort());
		GraphletNode dstPortNode = dstPortArea.addNode(dstPort);
		GraphletNode dstIP = new GraphletNode(trans.getDstIp());
		GraphletNode dstIPNode = dstIPArea.addNode(dstIP);

		if(!(srcIPNode == null && protoNode == null)){
			createEdge(srcIPArea.getNode(srcIP), protoArea.getNode(proto), "update");
		}
		if(!(protoNode == null && srcPortNode == null)){
			createEdge(protoArea.getNode(proto), srcPortArea.getNode(srcPort), "update");
		}
		if(!(srcPortNode == null && dstPortNode == null)){
			createEdge(srcPortArea.getNode(srcPort), dstPortArea.getNode(dstPort), trans.getBytes() + "(" + trans.getPackets() + ")");
		}
		if(!(dstPortNode == null && dstIPNode == null)){
			createEdge(dstPortArea.getNode(dstPort), dstIPArea.getNode(dstIP), "[flows]([pkg/flow])");
		}
		
		refreshEdges();
	}

	private void findEdges(Area left, Area right) {
		for(int i = left.getChildCount()-1; i >= 0; i--){
			GraphletNode leftNode = (GraphletNode) left.getChild(i);
			Set<DefaultEdge> edges = hapGraphlet.edgesOf(leftNode.getNode());
			for(DefaultEdge edge : edges){
				for(int j = right.getChildCount()-1; j >= 0; j--){
					GraphletNode rightNode = (GraphletNode)right.getChild(j);
					if(hapGraphlet.getEdgeTarget(edge).equals(rightNode.getNode())){
						createEdge(leftNode, rightNode, "-");
					}
				}
			}
		}
	}

	private void refreshEdges(){
		for(Area area : areas){
			area.updateEdges();
		}
		this.sortChildren();
	}

//	private GraphletNode findNode(Node<?> node){
//	GraphletNode returnNode = null;
//	for(GraphletNode graphletNode : nodes){
//		if(node.equals(graphletNode.getNode())){
//			returnNode = graphletNode;
//			break;
//		}
//	}
//	return returnNode;
//	}		
	
	public static void setFont(Font mFont) {
		aFont = mFont;
	}

	private void addTestContent(){
		HAPGraphlet testGraphlet = new HAPGraphlet();
		//bytes packets direction srcip proto srcport dstport dstip
		String[] sArray1 = {" 12345 678 3", " 192.168.100.100", " 6", " 65128", " 80", " 69.171.234.48"};
		String[] sArray2 = {" 345 78 2", " 192.168.100.100", " 6", " 65132", " 80", " 212.35.35.35"};
		String[] sArray3 = {" 2345 142 1", " 192.168.100.100", " 17", " 32458", " 123", " 195.186.1.111"};
		testGraphlet.add(Transaction.parse(sArray1));
		testGraphlet.add(Transaction.parse(sArray2));
		testGraphlet.add(Transaction.parse(sArray3));
		
		this.update(testGraphlet);
		
		String[] updateTest = {" 12345 678 3", " 192.168.100.100", " 17", " 20568", " 80", " 195.186.1.111"};
		this.addTransaction(Transaction.parse(updateTest));
		
		String[] updateTest2 = {" 654 321 4", " 192.168.100.100", " 1", " 10423", " 1", " 195.186.1.111"};
		this.addTransaction(Transaction.parse(updateTest2));
	}
	
}
