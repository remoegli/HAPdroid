package ch.hsr.hapdroid;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import ch.hsr.hapdroid.graphlet.Area;
import ch.hsr.hapdroid.graphlet.nodes.NodeType;

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
		
	}

	public Scene onLoadScene() {
		Log.v(TAG, "onLoadScene Started");
		myScene = new Scene();
		myScene.setOnSceneTouchListener(this);
		myScene.setBackground(new ColorBackground(1.0f, 1.0f, 1.0f));
		
		//final int centerX = (CAMERA_WIDTH - this.mHAPTextureRegion.getWidth()) / 2;
		//final int centerY = (CAMERA_HEIGHT - this.mHAPTextureRegion.getHeight()) / 2;
		
		//Some Info
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		Text info = new Text(0, 0, mFont, "Display Height: " + height + " Width: " + width);
		myScene.attachChild(info);
		
		//Areas
		Area srcIPArea = new Area(0, 0, AREA_WIDTH, CAMERA_HEIGHT, mFont);
		srcIPArea.setColor(0, 0.2f, 0.2f, 0.5f);
		Area protoArea = new Area(AREA_WIDTH, 0, AREA_WIDTH, CAMERA_HEIGHT, mFont);
		protoArea.setColor(0.5f, 0, 0, 0.5f);
		Area srcPortArea = new Area(AREA_WIDTH*2, 0, AREA_WIDTH, CAMERA_HEIGHT, mFont);
		srcPortArea.setColor(0, 0.5f, 0, 0.5f);
		Area dstPortArea = new Area(AREA_WIDTH*3, 0, AREA_WIDTH, CAMERA_HEIGHT, mFont);
		dstPortArea.setColor(0, 0, 0.5f, 0.5f);
		Area dstIPArea = new Area(AREA_WIDTH*4, 0, AREA_WIDTH, CAMERA_HEIGHT, mFont);
		dstIPArea.setColor(0.2f, 0.2f, 0, 0.5f);
		
		myScene.attachChild(srcIPArea);
		myScene.attachChild(protoArea);
		myScene.attachChild(srcPortArea);
		myScene.attachChild(dstPortArea);
		myScene.attachChild(dstIPArea);

		myScene.registerTouchArea(srcIPArea);
		myScene.registerTouchArea(protoArea);
		myScene.registerTouchArea(srcPortArea);
		myScene.registerTouchArea(dstPortArea);
		myScene.registerTouchArea(dstIPArea);
		
		srcIPArea.addNode(NodeType.IP, "192.168.100.100");
		protoArea.addNode(NodeType.PROTO, "TCP");
		protoArea.addNode(NodeType.PROTO, "UDP");
		srcPortArea.addNode(NodeType.PORT, "65128");
		srcPortArea.addNode(NodeType.PORT, "32123");
		for(int i=20129; i < (20129+50); i++){
			srcPortArea.addNode(NodeType.PORT, Integer.toString(i));
		}
		dstPortArea.addNode(NodeType.PORT, "80");
		dstPortArea.addNode(NodeType.PORT, "53");
		dstIPArea.addNode(NodeType.IP, "69.171.234.48");
		dstIPArea.addNode(NodeType.IP, "192.168.100.1");
		dstIPArea.addNode(NodeType.IP, "192.168.100.10");
		
		//Edges
//		Line srcIP2Proto = new Line(srcIPX+srcIPW, srcIPY, protoX-protoW, protoY, LINE_WIDTH);
//		srcIP2Proto.setColor(0,0,0);
//		myScene.attachChild(srcIP2Proto);
//		
//		Line Proto2srcPort1 = new Line(protoX+protoW, protoY, srcPortX-srcPortW, srcPortY, LINE_WIDTH);
//		Proto2srcPort1.setColor(0,0,0);
//		myScene.attachChild(Proto2srcPort1);
//		
//		Line srcPort12dstPort1 = new Line(srcPortX+srcPortW, srcPortY, dstPortX-dstPortW, dstPortY, LINE_WIDTH);
//		srcPort12dstPort1.setColor(0,0,0);
//		myScene.attachChild(srcPort12dstPort1);
//		
//		Line dstPort12dstIP1 = new Line(dstPortX+dstPortW, dstPortY, dstIPX-dstIPW, dstIPY, LINE_WIDTH);
//		dstPort12dstIP1.setColor(0,0,0);
//		myScene.attachChild(dstPort12dstIP1);
		
		//Sum Node
//		final float nodeX = 300.0f;
//		final float nodeY = 100.0f;
//		final float nodeW = 120.0f;
//		final float nodeH = 40.0f;
//		Rectangle outerRectangle = new Rectangle(nodeX, nodeY, nodeW, nodeH);
//		outerRectangle.setColor(0, 0, 0);
//		myScene.attachChild(outerRectangle);
//		Rectangle innerRectangle = new Rectangle(nodeX+2, nodeY+2, nodeW-4, nodeH-4){
//			boolean mGrabbed = false;
//
//			@Override
//			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
//				switch(pSceneTouchEvent.getAction()) {
//					case TouchEvent.ACTION_DOWN:
//						this.setScale(1.25f);
//						this.mGrabbed = true;
//						break;
//					case TouchEvent.ACTION_MOVE:
//						if(this.mGrabbed) {
//							this.setPosition(pSceneTouchEvent.getX() - nodeW / 2, pSceneTouchEvent.getY() - nodeH / 2);
//						}
//						break;
//					case TouchEvent.ACTION_UP:
//						if(this.mGrabbed) {
//							this.mGrabbed = false;
//							this.setScale(1.0f);
//						}
//						break;
//				}
//				return true;
//			}
//		};
//		myScene.registerTouchArea(innerRectangle);
//		innerRectangle.setColor(255, 255, 255);
//		myScene.attachChild(innerRectangle);
//		//Label
//		String labelSumNode = "#con=6";
//		Text label = new Text(nodeX+10, nodeY+10, mFont, labelSumNode);
//		myScene.attachChild(label);
		
		this.getEngine().getTextureManager().loadTexture(mTex);
        this.getEngine().getFontManager().loadFont(mFont);
		
		return myScene;
	}

	public void onLoadComplete() {
		Log.v(TAG, "onLoadComplete Started");
	}

	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		//Log.v(TAG, "onSceneTouchEvent started");
		return true;
	}
   
	
	
}