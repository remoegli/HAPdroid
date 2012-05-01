package ch.hsr.hapdroid.graphlet.nodes;

import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;

import ch.hsr.hapdroid.graphlet.shapes.Ellipse;


public class EllipticNode extends Ellipse implements Node{

	private static final int ELLIPSE_Y_LABELOFFSET = -10;
	private static final int SEGMENTS_DEFAULT = 50;
	private static boolean FILLED_DEFAULT = false;
	private static final float LINE_WIDTH = 2.0f;
	
	public EllipticNode(float pX, float pY, float width, float height, Font mFont, String label, float labeloffset){
		this(pX, pY, width, height, LINE_WIDTH, FILLED_DEFAULT, SEGMENTS_DEFAULT, mFont, label, labeloffset);
	}
	
	public EllipticNode(float pX, float pY, float width, float height, float lineWidth, boolean filled, int segments, Font mFont, String label, float labeloffset) {
		super(pX, pY, width, height, lineWidth, filled, segments);
		this.setColor(0, 0, 0);
		
		Text nodeLabel = new Text(labeloffset, ELLIPSE_Y_LABELOFFSET, mFont, label);
		this.attachChild(nodeLabel);
	}

}
