package ch.hsr.hapdroid;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.ui.activity.LayoutGameActivity;

import android.app.ProgressDialog;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import ch.hsr.hapdroid.HAPdroidService.HAPdroidBinder;
import ch.hsr.hapdroid.R.id;
import ch.hsr.hapdroid.graphlet.Graphlet;
import ch.hsr.hapdroid.graphlet.edge.Edge;
import ch.hsr.hapdroid.graphlet.node.GraphletNode;

public class HAPdroidGraphletActivity extends LayoutGameActivity implements
		IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener {

	public static final int RECEIVE_NETWORK_FLOW = 0;
	public static final int RECEIVE_FLOW_TABLE = 1;
	public static final int RECEIVE_TRANSACTION_TABLE = 2;
	public static final int GENERATE_GRAPHLET = 3;
	public static final int PICK_FILE = 0;
	private static final String LOG_TAG = "MyActivity";
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RECEIVE_NETWORK_FLOW:
				break;
			case RECEIVE_TRANSACTION_TABLE:
				break;
			case GENERATE_GRAPHLET:
				mProgressDialog.dismiss();
				generateGraphlet();
				break;
			}
		}
	};

	private float screenWidth;
	private float screenHeight;
	private Texture mTex;
	private Font mFont;
	private Graphlet mGraphlet; 
	private ZoomCamera mZoomCamera;
	private SurfaceScrollDetector mScrollDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	
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
	private Button mBtnCaptureStartStop;
	private OnClickListener mOnClickStart;
	private OnClickListener mOnClickStop;
	private Button mBtnImport;
	private ProgressDialog mProgressDialog;
	private TextView mTxtStart;
	private TextView mTxtEnd;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBtnCaptureStartStop = (Button) findViewById(id.btn_capture_start_stop);
		mBtnImport = (Button) findViewById(id.btn_file_open);
		mTxtStart = (TextView) findViewById(R.id.text_starttime);
		mTxtEnd = (TextView) findViewById(R.id.text_endtime);
		
		mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.import_file_message_title);
        mProgressDialog.setMessage(getResources().getText(R.string.import_file_message));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
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
				mProgressDialog.show();
				mService.stopNetworkCapture();
				stopService(mServiceIntent);
				switchStartStopButton(false);
			}
		};

		mBtnImport.setOnClickListener(new OnClickListener() {
			Intent intent = new Intent();
			
			@Override
			public void onClick(View v) {
				intent.setClass(getBaseContext(), FileImportActivity.class);
				intent.setAction(Intent.ACTION_MAIN);
				startActivityForResult(intent, PICK_FILE);
			}
		});
		switchStartStopButton(mService.isCaptureRunning());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_FILE){
			if (resultCode == RESULT_OK){
				mService.importFile(data.getCharSequenceExtra(FileImportActivity.FILE_KEY));
				mProgressDialog.show();
			}
		}
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
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (mBound) {
			unbindService(mServiceConnection);
			mBound = false;
		}
	}
	
	public Engine onLoadEngine() {

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("screenWidth") && bundle.containsKey("screenHeight")) {
        	this.screenWidth  = bundle.getInt("screenWidth");
        	this.screenHeight = bundle.getInt("screenHeight");
        } else {
        	DisplayMetrics outMetrics = new DisplayMetrics();
    		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
    		screenWidth = outMetrics.widthPixels;
            screenHeight = outMetrics.heightPixels;
        }
        
        Log.v(LOG_TAG, "Screen width/height: " + screenWidth + "/" + screenHeight);
        
		RatioResolutionPolicy pResolutionPolicy = new RatioResolutionPolicy(screenWidth, screenHeight);
		mZoomCamera = new ZoomCamera(0, 0, screenWidth, screenHeight); //floats pX, pY, pWidth, pHeight
		EngineOptions pEngineOptions = new EngineOptions(false, ScreenOrientation.LANDSCAPE, pResolutionPolicy, mZoomCamera);
		Engine myEngine = new Engine(pEngineOptions);
		
		try {
			if(MultiTouch.isSupported(this)) {
				myEngine.setTouchController(new MultiTouchController());
			} else {
				Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(No PinchZoom is possible!)", Toast.LENGTH_LONG).show();
			}
		} catch (final MultiTouchException e) {
			Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(No PinchZoom is possible!)", Toast.LENGTH_LONG).show();
		}

		
		return myEngine;
	}

	@Override
	public void onLoadResources() {
		mTex = new BitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mFont = new Font(mTex, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 15, true, Color.BLACK);
		//TODO: Refactoring?
		GraphletNode.setFont(mFont);
		Edge.setFont(mFont);
		Graphlet.setFont(mFont);
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		mGraphlet = new Graphlet(screenWidth, screenHeight);
		mGraphlet.setOnSceneTouchListener(this);
		mGraphlet.setTouchAreaBindingEnabled(true);
//		mGraphlet.setOnAreaTouchTraversalFrontToBack();
		mGraphlet.setBackground(new ColorBackground(1f, 1f, 1f));
	
		this.mScrollDetector = new SurfaceScrollDetector(this);
		if(MultiTouch.isSupportedByAndroidVersion()) {
			try {
				this.mPinchZoomDetector = new PinchZoomDetector(this);
			} catch (final MultiTouchException e) {
				this.mPinchZoomDetector = null;
			}
		} else {
			this.mPinchZoomDetector = null;
		}
		
		this.getEngine().getTextureManager().loadTexture(mTex);
		this.getEngine().getFontManager().loadFont(mFont);

		return mGraphlet;
	}

	@Override
	public void onLoadComplete() {
		mServiceIntent = new Intent(this, HAPdroidService.class);
		bindService(mServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected int getLayoutID() {
		return R.layout.hud;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.view_graphlet;
	}
	
	private void generateGraphlet(){
		Log.d(LOG_TAG, "generating graphlet");
		mGraphlet.update(mService.getGraphlet());
		mTxtStart.setText(mService.getStartTime());
		mTxtEnd.setText(mService.getEndTime());
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(this.mPinchZoomDetector != null) {
			this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

			if(this.mPinchZoomDetector.isZooming()) {
				this.mScrollDetector.setEnabled(false);
			} else {
				if(pSceneTouchEvent.isActionDown()) {
					this.mScrollDetector.setEnabled(true);
				}
				this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
			}
		} else {
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		return true;
	}

	@Override
	public void onScroll(ScrollDetector pScollDetector, TouchEvent pTouchEvent,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		this.mPinchZoomStartedCameraZoomFactor = this.mZoomCamera.getZoomFactor();
	}

	@Override
	public void onPinchZoom(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pTouchEvent, float pZoomFactor) {
		// TODO Auto-generated method stub
		Log.v(LOG_TAG, "PinchZoomStarted: " + mPinchZoomStartedCameraZoomFactor + " + ZoomFactor: " + pZoomFactor + " Multiplacation: " + (this.mPinchZoomStartedCameraZoomFactor * pZoomFactor));
		if((this.mPinchZoomStartedCameraZoomFactor * pZoomFactor) >= 1.0f){
			this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
		}
	}

	@Override
	public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pTouchEvent, float pZoomFactor) {
		// TODO Auto-generated method stub
		if((this.mPinchZoomStartedCameraZoomFactor * pZoomFactor) >= 1.0f){
			this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
		}
	}
	
}