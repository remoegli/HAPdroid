package ch.hsr.hapdroid.graphlet.edge;

import org.anddev.andengine.entity.text.Text;

import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import ch.hsr.hapdroid.transaction.Transaction;

public class LabeledEdge extends BaseEdge {

	protected Text edgeLabel;
	
	public LabeledEdge(GraphletNode left, GraphletNode right, Transaction trans) {
		super(left, right);
		String packetsperflow;
		if(trans.getFlows().size() > 0){
			packetsperflow = Float.toString(trans.getPackets()/trans.getFlows().size());
		} else {
			packetsperflow = "-";
		}
		
		edgeLabel = new Text(0, 0, aFont, trans.getFlows().size() + "(" + packetsperflow + ")");
		this.attachChild(edgeLabel);
	}
	
	@Override
	public void update(){
		super.update();
		edgeLabel.setPosition((getX2()-getX1())/2 - (edgeLabel.getWidth()/2), ((getY2()-getY1())/2) - aFont.getLineHeight());
	}

}
