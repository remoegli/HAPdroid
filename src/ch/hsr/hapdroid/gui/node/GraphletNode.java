package ch.hsr.hapdroid.gui.node;

import org.anddev.andengine.entity.primitive.BaseRectangle;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.opengl.font.Font;

import ch.hsr.hapdroid.graph.node.Node;

/**\class GraphletNode
 * This class is in charge of representing the Node it contains on the GUI.
 *  
 *  @author Remo Egli
 */
public class GraphletNode extends BaseRectangle{

	private static Font sFont;
	private Node<?> mNode;
	/**
	 * This parameter is used to provide a minimal size of a GraphletNode by means of width and height.
	 * In a future version the height optimally is based on the font size.
	 */
	private static final int NODE_HEIGHT = 30;
	
	/**
	 * The GraphletNode needs to hold a reference to the Node it represents to gain access to the label text
	 * and the isSummarized() value.
	 * @param node
	 */
	public GraphletNode(Node<?> node) {
		super(0, 0, 0, NODE_HEIGHT);
		this.mNode = node;
		this.setColor(0, 0, 0, 0);
		
		Text nodeLabel = new Text(0 , 0, sFont, mNode.toString());
		this.setWidth(nodeLabel.getWidth()+NODE_HEIGHT);
		nodeLabel.setPosition(this.getWidth()/2-nodeLabel.getWidth()/2, this.getHeight()/2-nodeLabel.getHeight()/2);
		
		Shape mShape;
		if(!mNode.isSummarized()){
			mShape = new EllipticNode(this.getWidth()/2, this.getHeight()/2, this.getWidth()/2, this.getHeight()/2);
		} else{
			mShape = new SumNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
		}
		
		this.attachChild(mShape);
		this.attachChild(nodeLabel);
	}

	/**
	 * 
	 * @return returns the data Node this GraphletNode represents 
	 */
	public Node<?> getNode(){
		return mNode;
	}
	
	/**
	 * @see org.anddev.andengine.entity.Entity#setPosition(float, float)
	 */
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x-(this.getWidth()/2), y-(this.getHeight()/2));
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o){
		if(o instanceof GraphletNode){
			return this.mNode.equals(((GraphletNode)o).getNode());
		}
		return super.equals(o);
	}
	
	/**
	 * This method is require so the whole GUI can use the same font.
	 * @param aFont
	 */
	public static void setFont(Font aFont){
		  sFont = aFont;
	}
	
}
