package ch.hsr.hapdroid.gui.edge;

import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;

import ch.hsr.hapdroid.graph.Transaction;
import ch.hsr.hapdroid.gui.node.GraphletNode;

/**\class LabeledEdge
 * This edge holds a label which is always placed above its center.
 */
public class LabeledEdge extends BaseEdge {

	/**
	 * The Font which is used for all edges
	 */
	protected static Font sFont;
	/**
	 * Used to calculate the position of the edge label.
	 * Width and height are available after creating the respective instance.
	 */
	protected Text edgeLabel;
	/**
	 * The values which are contained in the edgeLabel are stored in the Transaction. 
	 */
	protected Transaction transaction;
	/**
	 * Required to represent IN, OUT and BI_FLOWS
	 * @see ch.hsr.hapdroid.gui.edge.DirectionType
	 */
	protected DirectionType type;
	
	/**
	 * 
	 * @param left The left GraphletNode this edge connects to 
	 * @param right The right GraphletNode this edge connects to
	 * @param trans The Transaction which contains the values for the edge label
	 */
	public LabeledEdge(GraphletNode left, GraphletNode right, Transaction trans) {
		super(left, right);
		transaction = trans;
		
		type = DirectionType.getType(transaction);
		this.setColor(type.getRed(), type.getGreen(), type.getBlue());
		
		edgeLabel = this.getText();
		this.attachChild(edgeLabel);
	}
	
	/**
	 * @see ch.hsr.hapdroid.gui.edge.BaseEdge#update()
	 */
	@Override
	public void update(){
		super.update();
		edgeLabel.setPosition((getX2()-getX1())/2 - (edgeLabel.getWidth()/2), ((getY2()-getY1())/2) - sFont.getLineHeight());
	}
	
	/**
	 * This method is used to determine the edge label on runtime
	 * @return Returns the label text
	 */
	protected Text getText(){
		String packetsperflow;
		if(transaction.getFlows().size() > 0){
			packetsperflow = Float.toString(((float)transaction.getPackets())/transaction.getFlows().size());
		} else {
			packetsperflow = "-";
		}
		return new Text(0, 0, sFont, transaction.getFlows().size() + "(" + packetsperflow + ")");
	}

	/**
	 * This method is require so the whole GUI can use the same font.
	 * @param aFont
	 */
	public static void setFont(Font aFont){
		sFont = aFont;
	}
	
}
