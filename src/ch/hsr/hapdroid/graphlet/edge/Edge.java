package ch.hsr.hapdroid.graphlet.edge;

import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;

import android.util.Log;

import ch.hsr.hapdroid.graphlet.node.GraphletNode;

public class Edge extends Line{

	private static final int EDGE_X_LABELOFFSET = -10;
	private static final int EDGE_Y_LABELOFFSET = -20;
	private static Font aFont;
	private GraphletNode leftNode;
	private GraphletNode rightNode;
	private Text edgeLabel;
	
	public Edge(GraphletNode left, GraphletNode right, String label){
		super(0, 0, 0, 0);
		this.setColor(0, 0, 0);
		leftNode = left;
		rightNode = right;
		edgeLabel = new Text(0, 0, aFont, label);
		this.attachChild(edgeLabel);
	}

	public void update() {
		Log.v("MyActivity", "Updating Edge: " + edgeLabel.getText() + " on z-index " + this.getZIndex());
		
		//Calculate both ends on the line relative to the area
		final float[] leftNodeCoordinates = getParent().convertSceneToLocalCoordinates(leftNode.convertLocalToSceneCoordinates(leftNode.getX(), leftNode.getY()), new float[2]);
		final float[] rightNodeCoordinates = getParent().convertSceneToLocalCoordinates(rightNode.convertLocalToSceneCoordinates(rightNode.getX(), rightNode.getY()), new float[2]);
		float leftNodeX = leftNodeCoordinates[0]-leftNode.getX();
		float leftNodeY = leftNodeCoordinates[1]-leftNode.getY();
		float rightNodeX = rightNodeCoordinates[0]-rightNode.getX();
		float rightNodeY = rightNodeCoordinates[1]-rightNode.getY();
		
		//Set the lable to the middle of the edge		
		edgeLabel.setPosition((rightNodeX-leftNodeX)/2+EDGE_X_LABELOFFSET, (rightNodeY-leftNodeY)/2+EDGE_Y_LABELOFFSET);
		this.setPosition(leftNodeX, leftNodeY, rightNodeX, rightNodeY);
	}

	public static void setFont(Font mFont) {
		aFont = mFont;		
	}
	
}
