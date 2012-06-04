package ch.hsr.hapdroid.graphlet.edge;

import ch.hsr.hapdroid.transaction.Transaction;
import android.graphics.Color;

public enum DirectionType {
    
	OUT_FLOW	(false, true, Color.RED),
    IN_FLOW	(true, false, Color.RED),
    BI_FLOW	(true, true, Color.BLACK),
    OUT_UNIBI_FLOW	(false, true, Color.GREEN),
    IN_UNIBI_FLOW	(true, false, Color.GREEN);
    
    private final boolean leftArrow;
    private final boolean rightArrow;
    private final int edgeColor;
    private final int COLOR_INT_TO_FLOAT_FACTOR = 255;
    
    DirectionType(boolean left, boolean right, int color) {
        this.leftArrow = left;
        this.rightArrow = right;
        this.edgeColor = color;
    }
    public boolean leftArrow() { return leftArrow; }
    public boolean rightArrow() { return rightArrow; }
    public float getRed() {return Color.red(edgeColor)/COLOR_INT_TO_FLOAT_FACTOR; }
    public float getGreen(){return Color.green(edgeColor)/COLOR_INT_TO_FLOAT_FACTOR; }
    public float getBlue(){return Color.blue(edgeColor)/COLOR_INT_TO_FLOAT_FACTOR; }

    public static DirectionType getType(Transaction trans){
    	switch(trans.getDirection()){
    	case 1:
    		return OUT_FLOW;
    	case 2:
    		return IN_FLOW;
    	case 3:
    		return BI_FLOW; //TODO: fix -> need direction
    	case 4:
    		return BI_FLOW;
    	case 8:
    		return BI_FLOW; //TODO: fix -> need direction
    	default:
    		return BI_FLOW;
    	}
    }
}