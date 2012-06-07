package ch.hsr.hapdroid.gui.edge;

import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;

import ch.hsr.hapdroid.graph.Transaction;
import ch.hsr.hapdroid.gui.node.GraphletNode;

public class LabeledEdge extends BaseEdge {

	protected static Font sFont;
	protected Text edgeLabel;
	protected Transaction transaction;
	protected DirectionType type;
	
	public LabeledEdge(GraphletNode left, GraphletNode right, Transaction trans) {
		super(left, right);
		transaction = trans;
		
		type = DirectionType.getType(transaction);
		this.setColor(type.getRed(), type.getGreen(), type.getBlue());
		
		edgeLabel = this.getText();
		this.attachChild(edgeLabel);
	}
	
	@Override
	public void update(){
		super.update();
		edgeLabel.setPosition((getX2()-getX1())/2 - (edgeLabel.getWidth()/2), ((getY2()-getY1())/2) - sFont.getLineHeight());
	}
	
	protected Text getText(){
		String packetsperflow;
		if(transaction.getFlows().size() > 0){
			packetsperflow = Float.toString(((float)transaction.getPackets())/transaction.getFlows().size());
		} else {
			packetsperflow = "-";
		}
		return new Text(0, 0, sFont, transaction.getFlows().size() + "(" + packetsperflow + ")");
	}

	public static void setFont(Font aFont){
		sFont = aFont;
	}
	
}
