package ch.hsr.hapdroid;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.HUD;
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
import ch.hsr.hapdroid.HAPdroidService.HAPdroidBinder;
import ch.hsr.hapdroid.graphlet.Graphlet;
import ch.hsr.hapdroid.graphlet.edge.Edge;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.IBinder;
import android.view.Display;

public class HAPdroidGraphletActivity extends BaseGameActivity implements IOnSceneTouchListener{

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;
	
	
	private Texture mTex;
	private Font mFont;
	private Graphlet mGraphlet; 
	private Camera pCamera;
	
//	private Intent mServiceIntent;
//	private boolean mBound;
//	private HAPdroidService mService;
//	private ServiceConnection mServiceConnection = new ServiceConnection() {
//		@Override
//		public void onServiceConnected(ComponentName className, IBinder service) {
//			HAPdroidBinder binder = (HAPdroidBinder) service;
//			mService = binder.getService();
//			mBound = true;
//		}
//		@Override
//		public void onServiceDisconnected(ComponentName arg0) {
//			mBound = false;
//		}
//	};
//	private HAPGraphlet mHAPgraphlet;
	
	public Engine onLoadEngine() {
		RatioResolutionPolicy pResolutionPolicy = new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT);
		pCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT); //floats pX, pY, pWidth, pHeight
		EngineOptions pEngineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, pResolutionPolicy, pCamera);
		Engine myEngine = new Engine(pEngineOptions);
		return myEngine;
	}

	public void onLoadResources() {
		mTex = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mFont = new Font(mTex, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 15, true, Color.BLACK);
		GraphletNode.setFont(mFont);
		Edge.setFont(mFont);
//		mHAPgraphlet = mService.getGraphlet();
	}

	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		mGraphlet = new Graphlet(CAMERA_WIDTH, CAMERA_HEIGHT);
		mGraphlet.setOnSceneTouchListener(this);
		mGraphlet.setTouchAreaBindingEnabled(true);
		mGraphlet.setBackground(new ColorBackground(0.8f, 0.8f, 0.8f));
		
		//final int centerX = (CAMERA_WIDTH - this.mHAPTextureRegion.getWidth()) / 2;
		//final int centerY = (CAMERA_HEIGHT - this.mHAPTextureRegion.getHeight()) / 2;
		
		//Some Info
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		Text info = new Text(0, 0, mFont, "Display Height: " + height + " Width: " + width);
		mGraphlet.attachChild(info);

		//Get nodes from graphlet
//		for(Node<?> node : mHAPgraphlet.getSrcIpList()){
//			GraphletNode graphletNode = new GraphletNode(NodeType.IP, node);
//			srcIPArea.addNode(graphletNode);
//			nodes.add(graphletNode);
//		}
//		
//		for(Node<?> node : mHAPgraphlet.getProtoList()){
//			GraphletNode graphletNode = new GraphletNode(NodeType.PROTO, node);
//			protoArea.addNode(graphletNode);
//			nodes.add(graphletNode);
//		}
//		
//		for(Node<?> node : mHAPgraphlet.getSrcPortList()){
//			GraphletNode graphletNode = new GraphletNode(NodeType.PORT, node);
//			srcPortArea.addNode(graphletNode);
//			nodes.add(graphletNode);
//		}
//		
//		for(Node<?> node : mHAPgraphlet.getDstPortList()){
//			GraphletNode graphletNode = new GraphletNode(NodeType.PORT, node);
//			dstPortArea.addNode(graphletNode);
//			nodes.add(graphletNode);
//		}
//		
//		for(Node<?> node : mHAPgraphlet.getDstIpList()){
//			GraphletNode graphletNode = new GraphletNode(NodeType.IP, node);
//			dstIPArea.addNode(graphletNode);
//			nodes.add(graphletNode);
//		}
//		
//		for(GraphletNode graphletNode : nodes){
//			Set<DefaultEdge> edges = mHAPgraphlet.edgesOf(graphletNode.getNode());
//			for(DefaultEdge edge : edges){
//				createEdge(findNode(mHAPgraphlet.getEdgeSource(edge)), findNode(mHAPgraphlet.getEdgeTarget(edge)), "[no label");
//			}
//		}
//		
		
		this.getEngine().getTextureManager().loadTexture(mTex);
        this.getEngine().getFontManager().loadFont(mFont);
		
		return mGraphlet;
	}

	public void onLoadComplete() {
		mGraphlet.update();
	}

	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		return true;
	}
	
//	private GraphletNode findNode(Node<?> node){
//		GraphletNode returnNode = null;
//		for(GraphletNode graphletNode : nodes){
//			if(node.equals(graphletNode.getNode())){
//				returnNode = graphletNode;
//				break;
//			}
//		}
//		return returnNode;
//	}
	
//	@Override
//	protected void onStart() {
//		super.onStart();
//		
//		mServiceIntent = new Intent(this, HAPdroidService.class);
//		bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
//	}
//
//	@Override
//	protected void onStop() {
//		super.onStop();
//		if (mBound) {
//			unbindService(mServiceConnection);
//			mBound = false;
//		}
//	}
//	
}