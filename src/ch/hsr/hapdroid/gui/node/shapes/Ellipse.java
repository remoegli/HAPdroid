package ch.hsr.hapdroid.gui.node.shapes;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.collision.RectangularShapeCollisionChecker;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.shape.IShape;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.opengl.buffer.BufferObjectManager;
import org.anddev.andengine.opengl.util.GLHelper;

/**\class Ellipse
 * AndEngine doesn't provide an basic ellipse shape because it's not commonly used with OpenGL.
 * This class represents a workaround to this by calculating a vertex for each segment of the circle.
 * 
 * This code was taken from AndEngine Forums:
 * http://www.andengine.org/forums/gles1/how-do-i-draw-a-circle-t868.html
 *
 */
public class Ellipse extends Shape {
	/**
	 * A default line width bigger than 1.0f only works if the GL_LINE_SMOOTH option in the method onInitDraw() is commented out.
	 * This value is only used if no line width is provided in the constructor.
	 */
	private static final float LINEWIDTH_DEFAULT = 1.0f;
	/**
	 * If the GUI has to draw a lot of nodes the amount of segments can be reduced to improve performance.
	 * This value is only used if not provided in the constructor.
	 */
	private static final int SEGMENTS_DEFAULT = 50;
	private final EllipseVertexBuffer vertexBuffer;
	private int filledMode;
	private int segments;
	private float lineWidth;
	private float height;
	private float width;
	private Rectangle collisionRectangle;

	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param radius
	 */
	public Ellipse(float pX, float pY, float radius) {
		this(pX, pY, radius, radius);
	}

	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param radius
	 * @param filled
	 */
	public Ellipse(float pX, float pY, float radius, boolean filled) {
		this(pX, pY, radius, LINEWIDTH_DEFAULT, filled, SEGMENTS_DEFAULT);
	}

	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param radius
	 * @param lineWidth
	 * @param filled
	 */
	public Ellipse(float pX, float pY, float radius, float lineWidth,
			boolean filled) {
		this(pX, pY, radius, radius, lineWidth, filled, SEGMENTS_DEFAULT);
	}

	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param radius
	 * @param lineWidth
	 * @param filled
	 * @param segments
	 */
	public Ellipse(float pX, float pY, float radius, float lineWidth,
			boolean filled, int segments) {
		this(pX, pY, radius, radius, lineWidth, filled, segments);
	}
	
	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param radius
	 * @param segments
	 */
	public Ellipse(float pX, float pY, float radius, int segments) {
		this(pX, pY, radius, LINEWIDTH_DEFAULT, false, segments);
	}

	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param width
	 * @param height
	 */
	public Ellipse(float pX, float pY, float width, float height) {
		this(pX, pY, width, height, LINEWIDTH_DEFAULT, false, SEGMENTS_DEFAULT);
	}

	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param radius
	 * @param lineWidth
	 * @param segments
	 */
	public Ellipse(int pX, int pY, int radius, float lineWidth, int segments) {
		this(pX, pY, radius, lineWidth, false, segments);
	}

	/**
	 * 
	 * @param pX
	 * @param pY
	 * @param width
	 * @param height
	 * @param lineWidth
	 * @param filled
	 * @param segments
	 */
	public Ellipse(float pX, float pY, float width, float height, float lineWidth, boolean filled, int segments) {
		super(pX, pY);
		this.width = width;
		this.height = height;
		this.filledMode = (filled) ? GL10.GL_TRIANGLE_FAN : GL10.GL_LINE_LOOP;
		this.segments = segments;
		this.lineWidth = lineWidth;

		collisionRectangle = new Rectangle(-width, -height, width * 2, height * 2);
		collisionRectangle.setVisible(false);
		collisionRectangle.setIgnoreUpdate(true);
		attachChild(collisionRectangle);

		vertexBuffer = new EllipseVertexBuffer(segments, GL11.GL_STATIC_DRAW);
		BufferObjectManager.getActiveInstance().loadBufferObject(vertexBuffer);
		this.updateVertexBuffer();
	}

	/**
	 * @see org.anddev.andengine.entity.Entity#getSceneCenterCoordinates()
	 */
	@Override
	public float[] getSceneCenterCoordinates() {
		return this.convertLocalToSceneCoordinates(this.width * 0.5f,
				this.height * 0.5f);
	}

	/**
	 * @see org.anddev.andengine.entity.shape.IShape#getWidth()
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * @see org.anddev.andengine.entity.shape.IShape#getHeight()
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * @see org.anddev.andengine.entity.shape.IShape#getBaseWidth()
	 */
	public float getBaseWidth() {
		return width;
	}

	/**
	 * @see org.anddev.andengine.entity.shape.IShape#getBaseHeight()
	 */
	public float getBaseHeight() {
		return height;
	}

	/**
	 * @see org.anddev.andengine.entity.shape.IShape#collidesWith(org.anddev.andengine.entity.shape.IShape)
	 */
	public boolean collidesWith(IShape pOtherShape) {
		// unsupported
		return false;
	}

	/**
	 * @see org.anddev.andengine.entity.scene.Scene.ITouchArea#contains(float, float)
	 */
	public boolean contains(float pX, float pY) {
		return RectangularShapeCollisionChecker.checkContains(
				collisionRectangle, pX, pY); // TODO Ellipse Collision
	}

	/**
	 * @see org.anddev.andengine.entity.shape.Shape#onUpdateVertexBuffer()
	 */
	@Override
	protected void onUpdateVertexBuffer() {
		vertexBuffer.update(segments, getWidth(), getHeight(), filledMode);
	}

	/**
	 * @see org.anddev.andengine.entity.shape.Shape#getVertexBuffer()
	 */
	@Override
	public EllipseVertexBuffer getVertexBuffer() { // was protected
		return vertexBuffer;
	}

	/**
	 * @see org.anddev.andengine.entity.shape.Shape#isCulled(org.anddev.andengine.engine.camera.Camera)
	 */
	@Override
	protected boolean isCulled(Camera pCamera) {
		return false;
	}

	/**
	 * @see org.anddev.andengine.entity.shape.Shape#onInitDraw(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	protected void onInitDraw(final GL10 pGL) {
		super.onInitDraw(pGL);
		GLHelper.disableTextures(pGL);
		GLHelper.disableTexCoordArray(pGL);

		// enabled for nicer lines, at the expense of a limited linewidth of 1
		pGL.glEnable(GL10.GL_LINE_SMOOTH);
		
		GLHelper.lineWidth(pGL, lineWidth);
	}

	/**
	 * @see org.anddev.andengine.entity.shape.Shape#drawVertices(javax.microedition.khronos.opengles.GL10, org.anddev.andengine.engine.camera.Camera)
	 */
	@Override
	protected void drawVertices(GL10 gl, Camera pCamera) {
		gl.glDrawArrays(filledMode, 0, segments);
	}

	/**
	 * 
	 * @return
	 */
	public float getLineWidth() {
		return lineWidth;
	}

	/**
	 * 
	 * @param lineWidth
	 */
	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
	}

	/**
	 * 
	 * @param height
	 */
	public void setHeight(float height) {
		this.height = height;
		this.collisionRectangle.setHeight(height);
		this.updateVertexBuffer();
	}

	/**
	 * 
	 * @param width
	 */
	public void setWidth(float width) {
		this.width = width;
		this.collisionRectangle.setWidth(width);
		this.updateVertexBuffer();
	}
}