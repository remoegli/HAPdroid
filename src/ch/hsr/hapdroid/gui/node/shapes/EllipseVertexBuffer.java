package ch.hsr.hapdroid.gui.node.shapes;

import javax.microedition.khronos.opengles.GL10;
 
import org.anddev.andengine.opengl.util.FastFloatBuffer;
import org.anddev.andengine.opengl.vertex.VertexBuffer;
import org.anddev.andengine.util.MathUtils;

/**\class EllipseVertexBuffer
 * The EllipseVertexBuffer is in charge of calculating a vertex for each segment of the ellipse.
 * 
 * This code was taken from AndEngine Forums:
 * http://www.andengine.org/forums/gles1/how-do-i-draw-a-circle-t868.html
 *
 */
public class EllipseVertexBuffer extends VertexBuffer {

	/**
	 * 
	 * @param segments Amount of vertices used to draw the circle.
	 * @param pDrawType
	 */
  public EllipseVertexBuffer(int segments, int pDrawType) {
    super(segments * 2, pDrawType, false); //TODO Managed = false comes with new andengine version
  }
 
  void update(int segments, float width, float height, int filledMode) {
    final int[] vertices = this.mBufferData;
    int count = 0;
   
    switch(filledMode){  
    case GL10.GL_LINE_LOOP:
        for (float i = 0; i < 360.0f; i += (360.0f / segments)) {
            vertices[count++] = Float
                .floatToRawIntBits((float) (Math.cos(MathUtils.degToRad(i)) * width));
            vertices[count++] = Float
                .floatToRawIntBits((float) (Math.sin(MathUtils.degToRad(i)) * height));
          }
        break;
       
    case GL10.GL_TRIANGLE_FAN:
        for (float i = 0; i < 360.0f; i += (360.0f / segments)) {
            vertices[count++] = Float
                .floatToRawIntBits((float) (Math.cos(MathUtils.degToRad(360-i)) * width));
            vertices[count++] = Float
                .floatToRawIntBits((float) (Math.sin(MathUtils.degToRad(360-i)) * height));
          }
        break;         
    }
   
    final FastFloatBuffer buffer = this.getFloatBuffer();
    buffer.position(0);
    buffer.put(vertices);
    buffer.position(0);
 
    super.setHardwareBufferNeedsUpdate();
  }
}