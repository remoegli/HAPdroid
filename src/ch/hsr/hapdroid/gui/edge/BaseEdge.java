package ch.hsr.hapdroid.gui.edge;

import org.anddev.andengine.entity.primitive.Line;
import ch.hsr.hapdroid.gui.node.GraphletNode;

/**\class BaseEdge
 * This is the GUI representation for simple edges without label or direction.
 * 
 * @author Remo Egli
 *
 */
public class BaseEdge extends Line {
	/**
	 * The left GraphletNode this edge connects to
	 */
	protected GraphletNode leftNode;
	/**
	 * The right GraphletNode this edge connects to
	 */
	protected GraphletNode rightNode;
	
	/**
	 * 
	 * @param left The left GraphletNode this edge connects to 
	 * @param right The right GraphletNode this edge connects to
	 */
	public BaseEdge(GraphletNode left, GraphletNode right){
		super(0, 0, 0, 0);
		this.setColor(0, 0, 0);
		leftNode = left;
		rightNode = right;
	}

	/**
	 * Calculates the coordinates of both ends on the line relative to the Graphlet (or Scene)
	 */
	public void update() {

		final float[] leftNodeCoordinates = getParent().convertSceneToLocalCoordinates(leftNode.getSceneCenterCoordinates(), new float[2]);
		final float[] rightNodeCoordinates = getParent().convertSceneToLocalCoordinates(rightNode.getSceneCenterCoordinates(), new float[2]);
		this.setPosition(leftNodeCoordinates[0], leftNodeCoordinates[1], rightNodeCoordinates[0], rightNodeCoordinates[1]);
	}
}
