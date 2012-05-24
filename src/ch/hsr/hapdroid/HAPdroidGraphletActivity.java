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
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.ui.activity.LayoutGameActivity;

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

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RECEIVE_NETWORK_FLOW:
				break;
			case RECEIVE_TRANSACTION_TABLE:
				break;
			case GENERATE_GRAPHLET:
				generateGraphlet();
				break;
			}

		}
	};
	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;

	private static final String LOG_TAG = "MyActivity";

	private Texture mTex;
	private Font mFont;
	private Graphlet mGraphlet; 
	private Camera pCamera;
	
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

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBtnCaptureStartStop = (Button) findViewById(id.btn_capture_start_stop);
		mBtnImport = (Button) findViewById(id.btn_file_open);
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
				synchronized(this) {
					try {
						wait(1500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				generateGraphlet();
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

	public Engine onLoadEngine() {
		RatioResolutionPolicy pResolutionPolicy = new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT);
		pCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT); //floats pX, pY, pWidth, pHeight
		EngineOptions pEngineOptions = new EngineOptions(false, ScreenOrientation.LANDSCAPE, pResolutionPolicy, pCamera);
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
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		mGraphlet = new Graphlet(CAMERA_WIDTH, CAMERA_HEIGHT);
		mGraphlet.setOnSceneTouchListener(this);
		mGraphlet.setTouchAreaBindingEnabled(true);
		mGraphlet.setBackground(new ColorBackground(0.8f, 0.8f, 0.8f));

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
		mGraphlet.attachChild(info);

	
		this.getEngine().getTextureManager().loadTexture(mTex);
		this.getEngine().getFontManager().loadFont(mFont);

		return mGraphlet;
	}

	@Override
	public void onLoadComplete() {

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
	}
	
}