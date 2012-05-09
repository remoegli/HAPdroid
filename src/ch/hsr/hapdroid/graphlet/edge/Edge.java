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
		Log.v("MyActivity", "Updating Edge: " + edgeLabel.getText());
		
		final float[] leftNodeCoordinates = getParent().convertSceneToLocalCoordinates(leftNode.convertLocalToSceneCoordinates(leftNode.getX(), leftNode.getY()), new float[2]);
		final float[] rightNodeCoordinates = getParent().convertSceneToLocalCoordinates(rightNode.convertLocalToSceneCoordinates(rightNode.getX(), rightNode.getY()), new float[2]);
		
//		Log.v("MyActivity", "Left Node: "+ leftNode.getX() + "/" + leftNode.getY());
//		final float[] leftNodeCoordinates = leftNode.convertLocalToSceneCoordinates(leftNode.getX(), leftNode.getY());
//		Log.v("MyActivity", "Left Node Scene: "+ leftNodeCoordinates[0] + "/" + leftNodeCoordinates[1]);
//		float[] leftNodeCoordinates2 = new float[2];
//		getParent().convertSceneToLocalCoordinates(leftNodeCoordinates, leftNodeCoordinates2);
//		Log.v("MyActivity", "Left Node Local: "+ leftNodeCoordinates2[0] + "/" + leftNodeCoordinates2[1]);

		
//		Log.v("MyActivity", "Right Node: "+ rightNode.getX() + "/" + rightNode.getY());
//		float[] rightNodeSceneCoordinates = new float[2];
//		rightNode.convertLocalToSceneCoordinates(rightNode.getX(), rightNode.getY(), rightNodeSceneCoordinates);
//		Log.v("MyActivity", "Right Node: "+ rightNodeSceneCoordinates[0] + "/" + rightNodeSceneCoordinates[1]);	
//		float[] rightNodeSceneCoordinates2 = new float[2];
//		getParent().convertSceneToLocalCoordinates(rightNodeSceneCoordinates[0], rightNodeSceneCoordinates[1], rightNodeSceneCoordinates2);
//		Log.v("MyActivity", "Right Node: "+ rightNodeSceneCoordinates2[0] + "/" + rightNodeSceneCoordinates2[1]);
//			float rightPointX = neighborSceneCoordinates[0] - nodeSceneCoordinates[0];
//			float rightPointY = neighborSceneCoordinates[1] - nodeSceneCoordinates[1];
//					
//		this.setPosition(leftNodeCoordinates2[0]-leftNode.getX(), leftNodeCoordinates2[1]-leftNode.getY(), rightNodeSceneCoordinates2[0]-rightNode.getX(), rightNodeSceneCoordinates2[1]-rightNode.getY());	
		this.setPosition(leftNodeCoordinates[0]-leftNode.getX(), leftNodeCoordinates[1]-leftNode.getY(), rightNodeCoordinates[0]-rightNode.getX(), rightNodeCoordinates[1]-rightNode.getY());
	}

	public static void setFont(Font mFont) {
		aFont = mFont;		
	}
	
}
