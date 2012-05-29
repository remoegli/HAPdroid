package ch.hsr.hapdroid.graphlet;

import java.util.Set;
import java.util.Vector;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.input.touch.TouchEvent;
import org.jgrapht.graph.DefaultEdge;

import ch.hsr.hapdroid.HAPGraphlet;
import ch.hsr.hapdroid.graphlet.edge.Edge;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import ch.hsr.hapdroid.transaction.Transaction;

public class Graphlet extends Scene{

	private Vector<Area> areas;
	private final int CAMERA_HEIGHT;
	private final int AREA_WIDTH;
	private Area srcIPArea;
	private Area protoArea;
	private Area srcPortArea;
	private Area dstPortArea;
	private Area dstIPArea;
	private HAPGraphlet hapGraphlet;
	
	public Graphlet(int cameraWidth, int cameraHeight){
		super();
		areas = new Vector<Area>();
		CAMERA_HEIGHT = cameraHeight;
		AREA_WIDTH = cameraWidth/5;
		
		createAreas();
		addTestContent();
		
	}

	private void createAreas() {
		//TODO: Remove Coloring
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
		//TODO: fix Edge duplication
		Edge edge = new Edge(left, right, label);
		edge.setZIndex(5);
		this.attachChild(edge);
		
		((Area)left.getParent()).addEdge(edge);
		((Area)right.getParent()).addEdge(edge);
	}
	
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		return true;
	}
	
	
	public void update(HAPGraphlet graphlet) {
		hapGraphlet = graphlet;
		
		srcIPArea.addAllNodes(graphlet.getSrcIpList());
		protoArea.addAllNodes(graphlet.getProtoList());
		srcPortArea.addAllNodes(graphlet.getSrcPortList());
		dstPortArea.addAllNodes(graphlet.getDstPortList());
		dstIPArea.addAllNodes(graphlet.getDstIpList());
				
//		for(GraphletNode graphletNode : nodes){
//			Set<DefaultEdge> edges = graphlet.edgesOf(graphletNode.getNode());
//			for(DefaultEdge edge : edges){
//				createEdge(findNode(graphlet.getEdgeSource(edge)), findNode(graphlet.getEdgeTarget(edge)), "[no label]");
//			}
//		}
		
		findEdges(srcIPArea, protoArea);
		findEdges(protoArea, srcPortArea);
		findEdges(srcPortArea, dstPortArea);
		findEdges(dstPortArea, dstIPArea);
		
		refreshEdges();
		
	}
	
	public void addTransaction(Transaction trans){
		//TODO: change back to true/false because of edge duplication
		GraphletNode srcIP = srcIPArea.addNode(new GraphletNode(trans.getSrcIp()));
		GraphletNode proto = protoArea.addNode(new GraphletNode(trans.getProto()));
		GraphletNode srcPort = srcPortArea.addNode(new GraphletNode(trans.getSrcPort()));
		GraphletNode dstPort = dstPortArea.addNode(new GraphletNode(trans.getDstPort()));
		GraphletNode dstIP = dstIPArea.addNode(new GraphletNode(trans.getDstIp()));
		
		createEdge(srcIP, proto, "update");
		createEdge(proto, srcPort, "update");
		createEdge(srcPort, dstPort, trans.getBytes() + "(" + trans.getPackets() + ")");
		createEdge(dstPort, dstIP, "[flows]([pkg/flow])");
		
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
	
	private void addTestContent(){
		HAPGraphlet testGraphlet = new HAPGraphlet();
		//bytes packets direction srcip proto srcport dstport dstip
		String[] sArray1 = {" 12345 678 3", " 192.168.100.100", " 1", " 65128", " 80", " 69.171.234.48"};
		String[] sArray2 = {" 345 78 2", " 192.168.100.100", " 1", " 65132", " 80", " 212.35.35.35"};
		String[] sArray3 = {" 2345 142 1", " 192.168.100.100", " 2", " 32458", " 123", " 195.186.1.111"};
		testGraphlet.add(Transaction.parse(sArray1));
		testGraphlet.add(Transaction.parse(sArray2));
		testGraphlet.add(Transaction.parse(sArray3));
		
		this.update(testGraphlet);
		
		String[] updateTest = {" 12345 678 3", " 192.168.100.100", " 1", " 20568", " 80", " 195.186.1.111"};
		this.addTransaction(Transaction.parse(updateTest));
		
		
//		//CreateNodes
//		GraphletNode srcipNode1 = new GraphletNode(NodeType.IP, "192.168.100.100");
//		GraphletNode protoNode1 = new GraphletNode(NodeType.PROTO, "TCP");
//		GraphletNode srcportNode1 = new GraphletNode(NodeType.PORT, "65128");
//		GraphletNode dstportNode1 = new GraphletNode(NodeType.PORT, "80");
//		GraphletNode dstipNode1 = new GraphletNode(NodeType.IP, "69.171.234.48");
//		GraphletNode protoNode2 = new GraphletNode(NodeType.PROTO, "UDP");
//		GraphletNode srcportNode2 = new GraphletNode(NodeType.PORT, "32123");
//		
//		//Add nodes to area
//		srcIPArea.addNode(srcipNode1);
//		protoArea.addNode(protoNode1);
//		protoArea.addNode(protoNode2);
//		srcPortArea.addNode(srcportNode1);
//		srcPortArea.addNode(srcportNode2);
//		dstPortArea.addNode(dstportNode1);
//		dstIPArea.addNode(dstipNode1);
//	
//		//Create Edges
//		createEdge(srcipNode1, protoNode1, "1.1");
//		createEdge(srcipNode1, protoNode2, "1.2");
//		createEdge(protoNode1, srcportNode1, "2.1");
//		createEdge(protoNode2, srcportNode2, "2.2");
//		createEdge(srcportNode1, dstportNode1, "3.1");
//		createEdge(srcportNode2, dstportNode1, "3.2"); 
//		createEdge(dstportNode1, dstipNode1, "4");
	
	}
	
}
