package ch.hsr.hapdroid;

import java.util.Set;
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
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.jgrapht.graph.DefaultEdge;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import ch.hsr.hapdroid.HAPdroidService.HAPdroidBinder;
import ch.hsr.hapdroid.R.id;
import ch.hsr.hapdroid.graphlet.Area;
import ch.hsr.hapdroid.graphlet.edge.Edge;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;
import ch.hsr.hapdroid.graphlet.node.NodeType;
import ch.hsr.hapdroid.transaction.Node;

public class HAPdroidGraphletActivity extends LayoutGameActivity implements
		IOnSceneTouchListener {
	public static final int RECEIVE_NETWORK_FLOW = 0;
	public static final int RECEIVE_FLOW_TABLE = 1;
	public static final int RECEIVE_TRANSACTION_TABLE = 2;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RECEIVE_NETWORK_FLOW:
				break;
			case RECEIVE_TRANSACTION_TABLE:
				break;
			}

		}
	};
	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;
	private static final int AREA_WIDTH = CAMERA_WIDTH / 5;

	private static final String LOG_TAG = "MyActivity";

	private Texture mTex;
	private Font mFont;
	private Scene myScene;
	private Camera pCamera;
	
	private Vector<Area> areas;
	private Intent mServiceIntent;
	private boolean mBound;
	private HAPdroidService mService;
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			HAPdroidBinder binder = (HAPdroidBinder) service;
			mService = binder.getService();
			mBound = true;

			mService.setCallbackHandler(mHandler);
			setOnClickListeners();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	private HAPGraphlet mHAPgraphlet;
	private Vector<GraphletNode> nodes;
	private Button mBtnCaptureStartStop;
	private OnClickListener mOnClickStart;
	private OnClickListener mOnClickStop;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBtnCaptureStartStop = (Button) findViewById(id.btn_capture_start_stop);
	}

	private void setOnClickListeners() {
		mOnClickStart = new OnClickListener() {

			@Override
			public void onClick(View v) {
				startService(mServiceIntent);
				mService.startNetworkCapture();
				switchStartStopButton(true);
			}
		};

		mOnClickStop = new OnClickListener() {

			@Override
			public void onClick(View v) {
				mService.stopNetworkCapture();
				stopService(mServiceIntent);
				switchStartStopButton(false);
			}
		};

		switchStartStopButton(mService.isCaptureRunning());
	}

	private void switchStartStopButton(boolean isCaptureStarted) {
		if (isCaptureStarted) {
			mBtnCaptureStartStop.setText(R.string.capture_stop);
			mBtnCaptureStartStop.setOnClickListener(mOnClickStop);
		} else {
			mBtnCaptureStartStop.setText(R.string.capture_start);
			mBtnCaptureStartStop.setOnClickListener(mOnClickStart);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		mServiceIntent = new Intent(this, HAPdroidService.class);
		bindService(mServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (mBound) {
			unbindService(mServiceConnection);
			mBound = false;
		}
	}

	@Override
	public Engine onLoadEngine() {
		Log.v(LOG_TAG, "onLoadEngine Started");

		RatioResolutionPolicy pResolutionPolicy = new RatioResolutionPolicy(
				CAMERA_WIDTH, CAMERA_HEIGHT);
		pCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT); // floats pX,
																	// pY,
																	// pWidth,
																	// pHeight

		EngineOptions pEngineOptions = new EngineOptions(false,
				ScreenOrientation.LANDSCAPE, pResolutionPolicy, pCamera);
		Engine myEngine = new Engine(pEngineOptions);
		return myEngine;
	}

	@Override
	public void onLoadResources() {
		Log.v(LOG_TAG, "onLoadResources Started");
		mTex = new BitmapTextureAtlas(1024, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mFont = new Font(mTex, Typeface.create(Typeface.DEFAULT,
				Typeface.NORMAL), 15, true, Color.BLACK);
		GraphletNode.setFont(mFont);
		Edge.setFont(mFont);
		areas = new Vector<Area>();
		nodes = new Vector<GraphletNode>();
		
		mHAPgraphlet = mService.getGraphlet();
		
	}

	@Override
	public Scene onLoadScene() {
		Log.v(LOG_TAG, "onLoadScene Started");
		this.mEngine.registerUpdateHandler(new FPSLogger());
		myScene = new Scene();
		myScene.setOnSceneTouchListener(this);
		myScene.setBackground(new ColorBackground(0.8f, 0.8f, 0.8f));

		// final int centerX = (CAMERA_WIDTH -
		// this.mHAPTextureRegion.getWidth()) / 2;
		// final int centerY = (CAMERA_HEIGHT -
		// this.mHAPTextureRegion.getHeight()) / 2;

		// Some Info
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		Text info = new Text(0, 0, mFont, "Display Height: " + height
				+ " Width: " + width);
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
		
		//Get nodes from graphlet
		for(Node<?> node : mHAPgraphlet.getSrcIpList()){
			GraphletNode graphletNode = new GraphletNode(NodeType.IP, node);
			srcIPArea.addNode(graphletNode);
			nodes.add(graphletNode);
		}
		
		for(Node<?> node : mHAPgraphlet.getProtoList()){
			GraphletNode graphletNode = new GraphletNode(NodeType.PROTO, node);
			protoArea.addNode(graphletNode);
			nodes.add(graphletNode);
		}
		
		for(Node<?> node : mHAPgraphlet.getSrcPortList()){
			GraphletNode graphletNode = new GraphletNode(NodeType.PORT, node);
			srcPortArea.addNode(graphletNode);
			nodes.add(graphletNode);
		}
		
		for(Node<?> node : mHAPgraphlet.getDstPortList()){
			GraphletNode graphletNode = new GraphletNode(NodeType.PORT, node);
			dstPortArea.addNode(graphletNode);
			nodes.add(graphletNode);
		}
		
		for(Node<?> node : mHAPgraphlet.getDstIpList()){
			GraphletNode graphletNode = new GraphletNode(NodeType.IP, node);
			dstIPArea.addNode(graphletNode);
			nodes.add(graphletNode);
		}
		
		for(GraphletNode graphletNode : nodes){
			Set<DefaultEdge> edges = mHAPgraphlet.edgesOf(graphletNode.getNode());
			for(DefaultEdge edge : edges){
				createEdge(findNode(mHAPgraphlet.getEdgeSource(edge)), findNode(mHAPgraphlet.getEdgeTarget(edge)), "[no label");
			}
		}
		
		//CreateNodes
//		GraphletNode srcipNode1 = new GraphletNode(NodeType.IP, "192.168.100.100");
//		GraphletNode protoNode1 = new GraphletNode(NodeType.PROTO, "TCP");
//		GraphletNode srcportNode1 = new GraphletNode(NodeType.PORT, "65128");
//		GraphletNode dstportNode1 = new GraphletNode(NodeType.PORT, "80");
//		GraphletNode dstipNode1 = new GraphletNode(NodeType.IP, "69.171.234.48");
//
//		GraphletNode protoNode2 = new GraphletNode(NodeType.PROTO, "UDP");
//		GraphletNode srcportNode2 = new GraphletNode(NodeType.PORT, "32123");
//		
//		//Add nodes to area
//		srcIPArea.addNode(srcipNode1);
//		protoArea.addNode(protoNode1);
//		protoArea.addNode(protoNode2);
//		srcPortArea.addNode(srcportNode1);
//		srcPortArea.addNode(srcportNode2);
//		dstPortArea.addNode(dstportNode1);
//		dstIPArea.addNode(dstipNode1);

		//Create Edges
//		createEdge(srcipNode1, protoNode1, "1.1");
//		createEdge(srcipNode1, protoNode2, "1.2");
//		createEdge(protoNode1, srcportNode1, "2.1");
//		createEdge(protoNode2, srcportNode2, "2.2");
//		createEdge(srcportNode1, dstportNode1, "3.1");
//		createEdge(srcportNode2, dstportNode1, "3.2"); 
//		createEdge(dstportNode1, dstipNode1, "4");
//		
		
		
		this.getEngine().getTextureManager().loadTexture(mTex);
		this.getEngine().getFontManager().loadFont(mFont);

		return myScene;
	}

	@Override
	public void onLoadComplete() {
		Log.v(LOG_TAG, "onLoadComplete Started");
		for(Area area : areas){
			area.updateEdges();
		}
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// Log.v(TAG, "onSceneTouchEvent started");
		return true;
	}

	private void createEdge(GraphletNode left, GraphletNode right, String label) {
		Edge edge = new Edge(left, right, label);
		((Area) left.getParent()).addEdge(edge);
		((Area) right.getParent()).addEdge(edge);
	}

	private GraphletNode findNode(Node<?> node) {
		GraphletNode returnNode = null;
		for (GraphletNode graphletNode : nodes) {
			if (node.equals(graphletNode.getNode())) {
				returnNode = graphletNode;
				break;
			}
		}
		return returnNode;

	}

	@Override
	protected int getLayoutID() {
		return R.layout.hud;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.view_graphlet;
	}

}