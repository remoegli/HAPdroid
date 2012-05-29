package ch.hsr.hapdroid.graphlet.node;

import org.anddev.andengine.entity.primitive.Rectangle;


public class SumNode extends Rectangle{

	private static final float LINE_WIDTH = 2.0f;
	
	public SumNode(float pX, float pY, float width, float height) {
		super(pX, pY, width, height);
		this.setColor(0, 0, 0);
		
		Rectangle innerRectangle = new Rectangle(pX+LINE_WIDTH, pY+LINE_WIDTH, width-(LINE_WIDTH*2), height-(LINE_WIDTH*2));
		this.attachChild(innerRectangle);

	}

}
