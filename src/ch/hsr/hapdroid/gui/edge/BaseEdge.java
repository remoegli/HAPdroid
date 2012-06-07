package ch.hsr.hapdroid.gui.edge;

import org.anddev.andengine.entity.primitive.Line;
import ch.hsr.hapdroid.gui.node.GraphletNode;

public class BaseEdge extends Line {
	protected GraphletNode leftNode;
	protected GraphletNode rightNode;
	
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
