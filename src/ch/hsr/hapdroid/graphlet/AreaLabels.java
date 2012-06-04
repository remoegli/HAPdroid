package ch.hsr.hapdroid.graphlet;

import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;

public class AreaLabels extends Rectangle {

	private static Font aFont = Graphlet.getFont();
	
	public AreaLabels(float areaWidth) {
		super(0, 0, areaWidth * 5, aFont.getLineHeight() * 2);
		this.setColor(1.0f, 1.0f, 1.0f, 0.8f);
		float textY = aFont.getLineHeight() / 2;
		
		Text srcIPLabel = new Text( 0, 0, aFont,  "local IP");
		srcIPLabel.setPosition((areaWidth/2) - (srcIPLabel.getWidth()/2), textY);
		this.attachChild(srcIPLabel);
		Text protoLabel = new Text( 0, 0, aFont, "Protocol");
		protoLabel.setPosition(areaWidth + (areaWidth/2)-(protoLabel.getWidth()/2), textY);
		this.attachChild(protoLabel);
		Text srcPortLabel = new Text( 0, 0, aFont, "local Port");
		srcPortLabel.setPosition((2*areaWidth) + (areaWidth/2)-(srcPortLabel.getWidth()/2), textY);
		this.attachChild(srcPortLabel);
		Text dstPortLabel = new Text( 0, 0, aFont, "remote Port");
		dstPortLabel.setPosition((3*areaWidth) + (areaWidth/2)-(dstPortLabel.getWidth()/2), textY);
		this.attachChild(dstPortLabel);
		Text dstIPLabel = new Text( 0, 0, aFont, "remote IP");
		dstIPLabel.setPosition((4*areaWidth) + (areaWidth/2)-(dstIPLabel.getWidth()/2), textY);
		this.attachChild(dstIPLabel);
	}

}
