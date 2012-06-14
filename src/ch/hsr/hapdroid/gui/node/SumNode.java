package ch.hsr.hapdroid.gui.node;

import org.anddev.andengine.entity.primitive.Rectangle;

/**\class SumNode
 * SumNode contains the graphical representation of summarized nodes by creating an overlay of two primitive rectangles.
 * 
 * @author Remo Egli
 */
public class SumNode extends Rectangle{

	private static final float LINE_WIDTH = 2.0f;
	
	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param width
	 * @param height
	 */
	public SumNode(float pX, float pY, float width, float height) {
		super(pX, pY, width, height);
		this.setColor(0, 0, 0);
		
		Rectangle innerRectangle = new Rectangle(pX+LINE_WIDTH, pY+LINE_WIDTH, width-(LINE_WIDTH*2), height-(LINE_WIDTH*2));
		innerRectangle.setColor(1.0f, 1.0f, 1.0f);
		this.attachChild(innerRectangle);

	}

}
