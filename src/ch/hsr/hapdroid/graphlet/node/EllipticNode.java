package ch.hsr.hapdroid.graphlet.node;

import ch.hsr.hapdroid.graphlet.node.shapes.Ellipse;


public class EllipticNode extends Ellipse{

	private static final int SEGMENTS_DEFAULT = 50;
	private static final float LINE_WIDTH = 2.0f;
	
	public EllipticNode(float pX, float pY, float width, float height){
		this(pX, pY, width, height, LINE_WIDTH, SEGMENTS_DEFAULT);
	}
	
	public EllipticNode(float pX, float pY, float width, float height, float lineWidth, int segments) {
		super(pX, pY, width, height, lineWidth, true, segments);
		this.setColor(1.0f, 1.0f, 1.0f);
		
		Ellipse border = new Ellipse(0, 0, width, height, lineWidth, false, segments);
		border.setColor(0, 0, 0);
		this.attachChild(border);
		
	}

}
