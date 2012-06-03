package ch.hsr.hapdroid;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
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
		IOnSceneTouchListener {

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
	private Camera mCamera;
	
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
	private Toast mToast;

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
		
		mToast = Toast.makeText(this, 
				R.string.capture_nothing, Toast.LENGTH_SHORT);
		
		mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.load_graphlet_message_title);
        mProgressDialog.setMessage(getResources().getText(R.string.load_graphlet_message));
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
				if (mService.hasPacketsCaptured()){
					mProgressDialog.show();
					stopService(mServiceIntent);
					mService.stopNetworkCapture();
				} else {
					mService.stopForeground(true);
					mToast.show();
				}
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
			mBtnImport.setEnabled(false);
		} else {
			mBtnCaptureStartStop.setText(R.string.capture_start);
			mBtnCaptureStartStop.setOnClickListener(mOnClickStart);
			mBtnImport.setEnabled(true);
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
		mCamera = new Camera(0, 0, screenWidth, screenHeight); //floats pX, pY, pWidth, pHeight
		EngineOptions pEngineOptions = new EngineOptions(false, ScreenOrientation.LANDSCAPE, pResolutionPolicy, mCamera);
		Engine myEngine = new Engine(pEngineOptions);
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
		mGraphlet.setBackground(new ColorBackground(1f, 1f, 1f));
	
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
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		return true;
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
	
}