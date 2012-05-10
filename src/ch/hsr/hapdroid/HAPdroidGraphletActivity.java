package ch.hsr.hapdroid;

import java.util.Vector;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import ch.hsr.hapdroid.graphlet.Area;
import ch.hsr.hapdroid.graphlet.edge.Edge;
import ch.hsr.hapdroid.graphlet.node.Node;
import ch.hsr.hapdroid.graphlet.node.NodeType;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;

public class HAPdroidGraphletActivity extends BaseGameActivity implements IOnSceneTouchListener{

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;
	private static final int AREA_WIDTH = CAMERA_WIDTH/5;
	
	private static final String TAG = "MyActivity";
	
	private Texture mTex;
	private Font mFont;
	private Scene myScene; 
	private Camera pCamera;
	
	private Vector<Area> areas;
	
	public Engine onLoadEngine() {
		
		Log.v(TAG, "onLoadEngine Started");
		
		RatioResolutionPolicy pResolutionPolicy = new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT);
		pCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT); //floats pX, pY, pWidth, pHeight
		
		EngineOptions pEngineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, pResolutionPolicy, pCamera);
		Engine myEngine = new Engine(pEngineOptions);
		return myEngine;
	}

	public void onLoadResources() {
		Log.v(TAG, "onLoadResources Started");
		mTex = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mFont = new Font(mTex, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 15, true, Color.BLACK);
		Node.setFont(mFont);
		Edge.setFont(mFont);
		areas = new Vector<Area>();
	}

	public Scene onLoadScene() {
		Log.v(TAG, "onLoadScene Started");
		this.mEngine.registerUpdateHandler(new FPSLogger());
		myScene = new Scene();
		myScene.setOnSceneTouchListener(this);
		myScene.setBackground(new ColorBackground(0.8f, 0.8f, 0.8f));
		
		//final int centerX = (CAMERA_WIDTH - this.mHAPTextureRegion.getWidth()) / 2;
		//final int centerY = (CAMERA_HEIGHT - this.mHAPTextureRegion.getHeight()) / 2;
		
		//Some Info
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		Text info = new Text(0, 0, mFont, "Display Height: " + height + " Width: " + width);
		myScene.attachChild(info);
		
		//Areas
		Area srcIPArea = new Area(0, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(srcIPArea);
		srcIPArea.setColor(0, 0.2f, 0.2f, 0.5f);
		Area protoArea = new Area(AREA_WIDTH, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(protoArea);
		protoArea.setColor(0.5f, 0, 0, 0.5f);
		Area srcPortArea = new Area(AREA_WIDTH*2, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(srcPortArea);
		srcPortArea.setColor(0, 0.5f, 0, 0.5f);
		Area dstPortArea = new Area(AREA_WIDTH*3, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(dstPortArea);
		dstPortArea.setColor(0, 0, 0.5f, 0.5f);
		Area dstIPArea = new Area(AREA_WIDTH*4, 0, AREA_WIDTH, CAMERA_HEIGHT);
		areas.add(dstIPArea);
		dstIPArea.setColor(0.2f, 0.2f, 0, 0.5f);
		
		//Attach Areas to Scene and register TouchHandler
		for(Area area : areas){
			myScene.attachChild(area);
			myScene.registerTouchArea(area);
		}
		
		//CreateNodes
		Node srcipNode1 = new Node(NodeType.IP, "192.168.100.100");
		Node protoNode1 = new Node(NodeType.PROTO, "TCP");
		Node srcportNode1 = new Node(NodeType.PORT, "65128");
		Node dstportNode1 = new Node(NodeType.PORT, "80");
		Node dstipNode1 = new Node(NodeType.IP, "69.171.234.48");

		Node protoNode2 = new Node(NodeType.PROTO, "UDP");
		Node srcportNode2 = new Node(NodeType.PORT, "32123");
		
		//Add nodes to area
		srcIPArea.addNode(srcipNode1);
		protoArea.addNode(protoNode1);
		protoArea.addNode(protoNode2);
		srcPortArea.addNode(srcportNode1);
		srcPortArea.addNode(srcportNode2);
		dstPortArea.addNode(dstportNode1);
		dstIPArea.addNode(dstipNode1);

		//Create Edges
		createEdge(srcipNode1, protoNode1, "1.1");
		createEdge(srcipNode1, protoNode2, "1.2");
		createEdge(protoNode1, srcportNode1, "2.1");
		createEdge(protoNode2, srcportNode2, "2.2");
		createEdge(srcportNode1, dstportNode1, "3.1");
		createEdge(srcportNode2, dstportNode1, "3.2"); 
		createEdge(dstportNode1, dstipNode1, "4");
		
		
		
		this.getEngine().getTextureManager().loadTexture(mTex);
        this.getEngine().getFontManager().loadFont(mFont);
		
		return myScene;
	}

	public void onLoadComplete() {
		Log.v(TAG, "onLoadComplete Started");
		for(Area area : areas){
			area.updateEdges();
		}
	}

	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		//Log.v(TAG, "onSceneTouchEvent started");
		return true;
	}
   
	private void createEdge(Node left, Node right, String label){
		Edge edge = new Edge(left, right, label);
		((Area)left.getParent()).addEdge(edge);
		((Area)right.getParent()).addEdge(edge);
	}
	
}