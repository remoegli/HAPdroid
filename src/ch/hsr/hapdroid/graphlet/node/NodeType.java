package ch.hsr.hapdroid.graphlet.node;

public enum NodeType {
    
	IP		(60, 20, -55),
    PROTO	(20, 20, -15),
    PORT	(40, 20, -20),
    S_IP	(120, 40, +10),
    S_PORT	(80, 40, +10);
    
    private final float width;
    private final float height;
    private final float labeloffset;
    
    NodeType(float width, float height, float offset) {
        this.width = width;
        this.height = height;
        this.labeloffset = offset;
    }
    public float width() { return width; }
    public float height() { return height; }
    public float offset() { return labeloffset; }

}