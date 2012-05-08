package ch.hsr.hapdroid.graphlet.edge;

import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;

import android.util.Log;

import ch.hsr.hapdroid.graphlet.node.Node;

public class Edge extends Line{

	private static Font aFont;
	private Node leftNode;
	private Node rightNode;
	private Text edgeLabel;
	
	public Edge(Node left, Node right, String label){
		super(0, 0, 0, 0);
		this.setColor(0, 0, 0);
		leftNode = left;
		rightNode = right;
		edgeLabel = new Text(0, 0, aFont, label);
		this.attachChild(edgeLabel);
	}

	//Shared float arrays HAVE TO be initialized!	
	public void update() {
		//Update left Point
		float[] leftNodeSceneCoordinates = new float[2];
		leftNode.convertLocalToSceneCoordinates(leftNode.getX(), leftNode.getY(), leftNodeSceneCoordinates);
		Log.v("MyActivity", "Left Node: "+ leftNodeSceneCoordinates[0] + "/" + leftNodeSceneCoordinates[1]);

		
		float[] rightNodeSceneCoordinates = new float[2];
		rightNode.convertLocalToSceneCoordinates(rightNode.getX(), rightNode.getY(), rightNodeSceneCoordinates);
		Log.v("MyActivity", "Right Node: "+ rightNodeSceneCoordinates[0] + "/" + rightNodeSceneCoordinates[1]);	
//			float rightPointX = neighborSceneCoordinates[0] - nodeSceneCoordinates[0];
//			float rightPointY = neighborSceneCoordinates[1] - nodeSceneCoordinates[1];
//					
		super.setPosition(leftNodeSceneCoordinates[0], leftNodeSceneCoordinates[1], rightNodeSceneCoordinates[0], rightNodeSceneCoordinates[1]);		
	}

	public static void setFont(Font mFont) {
		aFont = mFont;		
	}
	
}
