package ch.hsr.hapdroid.gui.node;

import ch.hsr.hapdroid.gui.node.shapes.Ellipse;

/**\class EllipticNode
 * EllipticNode contains the graphical representation of regular nodes by creating an overlay of two ellipses.
 * 
 * @author Remo Egli
 */
public class EllipticNode extends Ellipse{
	/**
	 * A default line width bigger than 1.0f only works if the GL_LINE_SMOOTH option in the method Ellipse.onInitDraw() is commented out.
	 */
	private static final float LINE_WIDTH = 2.0f;
	/**
	 * If the GUI has to draw a lot of nodes the amount of segments can be reduced to improve performance.
	 */
	private static final int SEGMENTS_DEFAULT = 50;
	
	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param width
	 * @param height
	 */
	public EllipticNode(float pX, float pY, float width, float height){
		this(pX, pY, width, height, LINE_WIDTH, SEGMENTS_DEFAULT);
	}
	
	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param width
	 * @param height
	 * @param lineWidth
	 * @param segments
	 */
	public EllipticNode(float pX, float pY, float width, float height, float lineWidth, int segments) {
		super(pX, pY, width, height, lineWidth, true, segments);
		this.setColor(1.0f, 1.0f, 1.0f);
		
		Ellipse border = new Ellipse(0, 0, width, height, lineWidth, false, segments);
		border.setColor(0, 0, 0);
		this.attachChild(border);
		
	}

}
