package ch.hsr.hapdroid.graphlet.edge;

import org.anddev.andengine.entity.text.Text;

import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import ch.hsr.hapdroid.transaction.Transaction;

public class LabeledEdge extends BaseEdge {

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
		edgeLabel.setPosition((getX2()-getX1())/2 - (edgeLabel.getWidth()/2), ((getY2()-getY1())/2) - aFont.getLineHeight());
	}
	
	protected Text getText(){
		String packetsperflow;
		if(transaction.getFlows().size() > 0){
			packetsperflow = Float.toString(transaction.getPackets()/transaction.getFlows().size());
		} else {
			packetsperflow = "-";
		}
		return new Text(0, 0, aFont, transaction.getFlows().size() + "(" + packetsperflow + ")");
	}

}
