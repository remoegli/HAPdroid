package ch.hsr.hapdroid.graphlet.node;

import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;


public class SumNode extends Rectangle{

	private static final int RECTANGLE_Y_LABELOFFSET = -10;
	
	public SumNode(float pX, float pY, float width, float height, Font mFont, String label, float labeloffset) {
		super(pX, pY, width, height);
		this.setColor(0, 0, 0);
		
		Rectangle innerRectangle = new Rectangle(pX+2, pY+2, width-4, height-4);
		this.attachChild(innerRectangle);

		Text nodeLabel = new Text(labeloffset, RECTANGLE_Y_LABELOFFSET, mFont, label);
		this.attachChild(nodeLabel);
		
		//Sum Node
//		Rectangle outerRectangle = new Rectangle(nodeX, nodeY, nodeW, nodeH);
//		outerRectangle.setColor(0, 0, 0);
//		myScene.attachChild(outerRectangle);
//		Rectangle innerRectangle = new Rectangle(nodeX+2, nodeY+2, nodeW-4, nodeH-4);
//		myScene.registerTouchArea(innerRectangle);
//		innerRectangle.setColor(255, 255, 255);
//		myScene.attachChild(innerRectangle);
//		//Label
//		String labelSumNode = "#con=6";
//		Text label = new Text(nodeX+10, nodeY+10, mFont, labelSumNode);
//		myScene.attachChild(label);

	}

}
