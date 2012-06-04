package ch.hsr.hapdroid.graphlet.edge;

import ch.hsr.hapdroid.network.Flow;
import ch.hsr.hapdroid.transaction.Transaction;
import android.graphics.Color;

public enum DirectionType {
    
	OUT_FLOW	(" ", "->", Color.RED),
    IN_FLOW	("<-", " ", Color.RED),
    BI_FLOW	("<-", "->", Color.BLACK),
    OUT_UNIBI_FLOW	(" ", "->", Color.GREEN),
    IN_UNIBI_FLOW	("<-", " ", Color.GREEN);
    
    private final String leftArrow;
    private final String rightArrow;
    private final int edgeColor;
    private final int COLOR_INT_TO_FLOAT_FACTOR = 255;
    
    DirectionType(String left, String right, int color) {
        this.leftArrow = left;
        this.rightArrow = right;
        this.edgeColor = color;
    }
    public String leftArrow() { return leftArrow; }
    public String rightArrow() { return rightArrow; }
    public float getRed() {return Color.red(edgeColor)/COLOR_INT_TO_FLOAT_FACTOR; }
    public float getGreen(){return Color.green(edgeColor)/COLOR_INT_TO_FLOAT_FACTOR; }
    public float getBlue(){return Color.blue(edgeColor)/COLOR_INT_TO_FLOAT_FACTOR; }

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