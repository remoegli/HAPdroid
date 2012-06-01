package ch.hsr.hapdroid.graphlet.edge;

import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;

public class Edge extends Line{

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
		
		//Calculate both ends on the line relative to the area
		final float[] leftNodeCoordinates = getParent().convertSceneToLocalCoordinates(leftNode.getSceneCenterCoordinates(), new float[2]);
		final float[] rightNodeCoordinates = getParent().convertSceneToLocalCoordinates(rightNode.getSceneCenterCoordinates(), new float[2]);
		float leftNodeX = leftNodeCoordinates[0];
		float leftNodeY = leftNodeCoordinates[1];
		float rightNodeX = rightNodeCoordinates[0];
		float rightNodeY = rightNodeCoordinates[1];
		
		//Set the lable to the middle of the edge		
		edgeLabel.setPosition((rightNodeX-leftNodeX)/2 - (edgeLabel.getWidth()/2), (rightNodeY-leftNodeY)/2 - (aFont.getLineHeight()*2));
		this.setPosition(leftNodeX, leftNodeY, rightNodeX, rightNodeY);
	}

	public static void setFont(Font mFont) {
		aFont = mFont;		
	}
	
}
