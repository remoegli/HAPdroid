package ch.hsr.hapdroid.gui;

import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;

/**\class AreaLabels
 * Holds the description text for each Area that is drawn and alligns the label
 * above the respective Area.
 * 
 * @author Remo Egli
 *
 */
public class AreaLabels extends Rectangle {

	private static Font sFont;
	
	/**
	 * 
	 * @param areaWidth Required to align the text labels
	 */
	public AreaLabels(float areaWidth) {
		super(0, 0, areaWidth * 5, sFont.getLineHeight() * 2);
		this.setColor(1.0f, 1.0f, 1.0f, 0.8f);
		float textY = sFont.getLineHeight() / 2;
		
		Text srcIPLabel = new Text( 0, 0, sFont,  "local IP");
		srcIPLabel.setPosition((areaWidth/2) - (srcIPLabel.getWidth()/2), textY);
		this.attachChild(srcIPLabel);
		Text protoLabel = new Text( 0, 0, sFont, "Protocol");
		protoLabel.setPosition(areaWidth + (areaWidth/2)-(protoLabel.getWidth()/2), textY);
		this.attachChild(protoLabel);
		Text srcPortLabel = new Text( 0, 0, sFont, "local Port");
		srcPortLabel.setPosition((2*areaWidth) + (areaWidth/2)-(srcPortLabel.getWidth()/2), textY);
		this.attachChild(srcPortLabel);
		Text dstPortLabel = new Text( 0, 0, sFont, "remote Port");
		dstPortLabel.setPosition((3*areaWidth) + (areaWidth/2)-(dstPortLabel.getWidth()/2), textY);
		this.attachChild(dstPortLabel);
		Text dstIPLabel = new Text( 0, 0, sFont, "remote IP");
		dstIPLabel.setPosition((4*areaWidth) + (areaWidth/2)-(dstIPLabel.getWidth()/2), textY);
		this.attachChild(dstIPLabel);
	}

	/**
	 * This method is require so the whole GUI can use the same font.
	 * @param aFont
	 */
	public static void setFont(Font aFont){
		sFont = aFont;
	}
}
