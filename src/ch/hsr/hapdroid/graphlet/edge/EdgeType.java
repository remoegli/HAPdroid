package ch.hsr.hapdroid.graphlet.edge;

import android.graphics.Color;

public enum EdgeType {
    
	OUT_FLOW	(false, true, Color.RED),
    IN_FLOW	(true, false, Color.RED),
    BI_FLOW	(true, true, Color.BLACK),
    OUT_UNIBI_FLOW	(false, true, Color.GREEN),
    IN_UNIBI_FLOW	(true, false, Color.GREEN);
    
    private final boolean leftArrow;
    private final boolean rightArrow;
    private final int edgeColor;
    
    EdgeType(boolean left, boolean right, int color) {
        this.leftArrow = left;
        this.rightArrow = right;
        this.edgeColor = color;
    }
    public boolean leftArrow() { return leftArrow; }
    public boolean rightArrow() { return rightArrow; }
    public int edgeColor() { return edgeColor; }


}