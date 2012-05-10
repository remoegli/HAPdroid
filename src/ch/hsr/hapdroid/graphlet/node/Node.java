package ch.hsr.hapdroid.graphlet.node;

import org.anddev.andengine.entity.primitive.BaseRectangle;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.opengl.font.Font;

import android.util.Log;

public class Node extends BaseRectangle {
	
	private static Font aFont;
	private NodeType nodeType;
	private String nodeLabel;
	private Shape mShape;
	
	
	public Node(NodeType type, String label) {
		super(0, 0, type.width(), type.height());
		nodeType = type;
		nodeLabel = label;
		this.setColor(0, 0, 0, 0);
		mShape = getShape();
		this.attachChild(mShape);
		Log.v("MyActivity", "Node \"" + label + "\" created on z-index" + this.getZIndex());
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
	}
	
	private Shape getShape(){
		Shape nShape;
		switch(nodeType){
		case IP:
		case PROTO:
		case PORT:
			nShape = new EllipticNode(0, 0, nodeType.width(), nodeType.height(), aFont, nodeLabel, nodeType.offset());
			break;
		case S_IP:
		case S_PORT:
			nShape = new SumNode(this.getWidth()/2, this.getHeight()/2, nodeType.width(), nodeType.height(), aFont, nodeLabel, nodeType.offset());
			break;
		default:
			nShape = new EllipticNode(this.getWidth()/2, this.getHeight()/2, 60, 20, aFont, "unknown NodeType", -55);
			nShape.setColor(1.0f, 0, 0);
			break;
		}
		return nShape;
}

	public static void setFont(Font mFont) {
		aFont = mFont;
	}
	
}
