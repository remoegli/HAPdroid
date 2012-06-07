package ch.hsr.hapdroid.gui.edge;

import ch.hsr.hapdroid.graph.Transaction;
import ch.hsr.hapdroid.network.Flow;
import android.graphics.Color;

/**
 * This enum represents properties for directed edges. 
 * @author Remo Egli
 */
public enum DirectionType {
    
	/**
	 * Data Filed bla?
	 */
	OUT_FLOW	(" ", "->", Color.RED), /**< Outgoing unidirectional flow */
    /**
     * more data field bla
     */
	IN_FLOW	("<-", " ", Color.RED), /**< Incoming unidirectional flow */
    BI_FLOW	("<-", "->", Color.BLACK), /**< Bidirectional flow */
    OUT_UNIBI_FLOW	(" ", "->", Color.GREEN),  /**< Outgoing unidirectional flows in the presence of bidirectional flows */
    IN_UNIBI_FLOW	("<-", " ", Color.GREEN);  /**< Incoming unidirectional flows in the presence of bidirectional flows */
    
	
    private final String leftArrow;
    private final String rightArrow;
    private final int edgeColor;
    private final int COLOR_INT_TO_FLOAT_FACTOR = 255;
    
    /**
     * What happens if I write here again?
     * @param left some bla about left
     * @param right some bla about right
     * @param color and dont forget the freaking color
     */
    DirectionType(String left, String right, int color) {
        this.leftArrow = left;
        this.rightArrow = right;
        this.edgeColor = color;
    }
    
    /**
     * This method is intended to return a boolean so the DirectedEdge class knows if the respective Arrow should be drawn or not.
     * The feature of graphical arrows was postponed to future releases and therefore replaced with a simple String representation.
     * @return leftArrow Returns the String representation of an arrow to the left if available for this DirectionType
     */
    public String leftArrow() { return leftArrow; }
    /**
     * @see leftArrow()
     * @return rightArrow Returns the String representation of an arrow to the right if available for this DirectionType
     */
    public String rightArrow() { return rightArrow; }
    public float getRed() {return Color.red(edgeColor)/COLOR_INT_TO_FLOAT_FACTOR; }
    public float getGreen(){return Color.green(edgeColor)/COLOR_INT_TO_FLOAT_FACTOR; }
    public float getBlue(){return Color.blue(edgeColor)/COLOR_INT_TO_FLOAT_FACTOR; }

    /** \brief Returns the DirectionType of a Transaction.
     * Returns the DirectionType enum of the Transaction as defined in the Flow class using the respective public constants.
     * @see Flow.java
     * @param trans The Transaction to get the direction info from.
     * @return The DirectionType
     */
    public static DirectionType getType(Transaction trans){
    	switch(trans.getDirection()){
    	case Flow.TYPE_OUTGOING:
    		return OUT_FLOW;
    	case Flow.TYPE_INCOMING:
    		return IN_FLOW;
    	case Flow.TYPE_BIFLOW:
    		return BI_FLOW;
    	default:
    		return BI_FLOW;
    	}
    }
}