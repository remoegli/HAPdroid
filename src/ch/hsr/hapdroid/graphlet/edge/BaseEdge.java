package ch.hsr.hapdroid.graphlet.edge;

import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.opengl.font.Font;

import ch.hsr.hapdroid.graphlet.Graphlet;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;

public class BaseEdge extends Line {
	protected GraphletNode leftNode;
	protected GraphletNode rightNode;
	protected static Font aFont = Graphlet.getFont();
	
	public BaseEdge(GraphletNode left, GraphletNode right){
		super(0, 0, 0, 0);
		this.setColor(0, 0, 0);
		leftNode = left;
		rightNode = right;
	}

	public void update() {
		//Calculate both ends on the line relative to the area
		final float[] leftNodeCoordinates = getParent().convertSceneToLocalCoordinates(leftNode.getSceneCenterCoordinates(), new float[2]);
		final float[] rightNodeCoordinates = getParent().convertSceneToLocalCoordinates(rightNode.getSceneCenterCoordinates(), new float[2]);
		this.setPosition(leftNodeCoordinates[0], leftNodeCoordinates[1], rightNodeCoordinates[0], rightNodeCoordinates[1]);
	}
}
