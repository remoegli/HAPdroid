package ch.hsr.hapdroid.graphlet;

import java.util.Vector;

import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;

import ch.hsr.hapdroid.graphlet.nodes.EllipticNode;
import ch.hsr.hapdroid.graphlet.nodes.Node;
import ch.hsr.hapdroid.graphlet.nodes.NodeType;


public class Area extends Rectangle{
	
	private static float NODESPACING = 10;
	private static float NODEHEIGHT = 40;
	private Font aFont;
	private boolean mGrabbed = false;
	private float firstTouchY;
	private float initialY;
	private final float initHeight;
	private Vector<Node> nodes;
	
	public Area(float pX, float pY, float pWidth, float pHeight, Font mFont) {
		super(pX, pY, pWidth, pHeight);
		initHeight = pHeight;
		aFont = mFont;
		nodes = new Vector<Node>();
	}

	public void addNode(NodeType type, String label){
		EllipticNode node = new EllipticNode(this.getWidth()/2, this.getHeight()/2, type.width(), type.height(), aFont, label, type.offset());
		nodes.add(node);
		updateNodePositions();
		attachChild(node);
	}

	private void updateNodePositions() {		
		super.setHeight((nodes.size()*(NODEHEIGHT+NODESPACING))+NODESPACING); //Every node with it's spacing plus the extra spacing at the top
		super.setPosition(this.getX(), (initHeight/2)-(this.getHeight()/2)); //Set the area to display center
		
		float nodeY = NODESPACING + NODEHEIGHT/2;
		for(Node node : nodes){
			node.setPosition(this.getWidth()/2, nodeY);
			nodeY = nodeY + NODEHEIGHT + NODESPACING;
		}
		
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		switch(pSceneTouchEvent.getAction()) {
			case TouchEvent.ACTION_DOWN:
				//this.setScale(1.25f); bla
				if(!this.mGrabbed){
					firstTouchY = pSceneTouchEvent.getY();
					initialY = this.getY();
					this.mGrabbed = true;
				}
				break;
			case TouchEvent.ACTION_MOVE:
				if(this.mGrabbed) {
					this.setPosition(this.getX(), initialY + (pSceneTouchEvent.getY() - firstTouchY));
				}
				break;
			case TouchEvent.ACTION_UP:
				if(this.mGrabbed) {
					this.mGrabbed = false;
					//this.setScale(1.0f);
				}
				break;
		}
		return true;
	}	
}

