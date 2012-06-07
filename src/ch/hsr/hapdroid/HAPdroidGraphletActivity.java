package ch.hsr.hapdroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ch.hsr.hapdroid.HAPdroidService.HAPdroidBinder;
import ch.hsr.hapdroid.R.id;
import ch.hsr.hapdroid.graphlet.Graphlet;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class HAPdroidGraphletActivity extends LayoutGameActivity implements
		IOnSceneTouchListener, IScrollDetectorListener,
		IPinchZoomDetectorListener {

	/**
	 * Message identifier for receiving network flow
	 */
	public static final int RECEIVE_NETWORK_FLOW = 0;
	/**
	 * Message identifier for receiving flow table
	 */
	public static final int RECEIVE_FLOW_TABLE = 1;
	/**
	 * Message identifier for receiving transaction table
	 */
	public static final int RECEIVE_TRANSACTION_TABLE = 2;
	/**
	 * Message identifier for generating graphlet
	 */
	public static final int GENERATE_GRAPHLET = 3;

	private static final int PICK_FILE = 0;
	private static final Pattern IP_ADDRESS = Pattern
			.compile("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
					+ "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
					+ "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
					+ "|[1-9][0-9]|[0-9]))");
	private static final String SAVE_FILENAME = "capture.gz";
	private static final Object FILE_EXTENSION_CFLOW = ".gz";
	private static final Object FILE_EXTENSION_PCAP = ".pcap";

	private static final String LOG_TAG = "HAPdroidGraphletActivity";

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
	private Button mBtnCaptureStartStop;
	private OnClickListener mOnClickStart;
	private OnClickListener mOnClickStop;
	private Button mBtnImport;
	private ProgressDialog mProgressDialog;
	private AlertDialog mWrongFileDialog;
	private TextView mTxtStart;
	private TextView mTxtEnd;
	private Toast mToast;
	private Button mBtnExport;
	private AlertDialog mIPInputDialog;
	private EditText mIPEditText;
	private Toast mWrongIPToast;
	private String mFilePath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBtnCaptureStartStop = (Button) findViewById(id.btn_capture_start_stop);
		mBtnImport = (Button) findViewById(id.btn_file_open);
		mBtnExport = (Button) findViewById(R.id.btn_file_save);
		mTxtStart = (TextView) findViewById(R.id.text_starttime);
		mTxtEnd = (TextView) findViewById(R.id.text_endtime);

		mToast = Toast.makeText(this, R.string.capture_nothing,
				Toast.LENGTH_SHORT);

		mProgressDialog = createProgressDialog();
		mWrongFileDialog = createWrongFileDialog();
		mIPInputDialog = createIPInputDialog();
	}

	private AlertDialog createIPInputDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		mIPEditText = new EditText(getApplicationContext());
		mIPEditText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		mWrongIPToast = Toast.makeText(getApplicationContext(),
				R.string.input_ip_wrong, Toast.LENGTH_SHORT);
		builder.setMessage(R.string.input_ip)
				.setView(mIPEditText)
				.setCancelable(false)
				.setPositiveButton(R.string.input_ip_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (correctIPFormat()) {
									mProgressDialog.show();
									mService.importPcapFile(mFilePath, mIPEditText.getText().toString());
								} else {
									mIPEditText.setText("");
									mWrongIPToast.show();
								}
							}
						})
				.setNegativeButton(R.string.input_ip_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		return builder.create();
	}

	private boolean correctIPFormat() {
		return IP_ADDRESS.matcher(mIPEditText.getText()).matches();
	}

	private AlertDialog createWrongFileDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.wrong_file_message).setCancelable(true);
		return builder.create();
	}

	private ProgressDialog createProgressDialog() {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setTitle(R.string.load_graphlet_message_title);
		dialog.setMessage(getResources()
				.getText(R.string.load_graphlet_message));
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);

		return dialog;
	}

	private void setOnClickListeners() {
		mOnClickStart = new OnClickListener() {
			@Override
			public void onClick(View v) {
				startService(mServiceIntent);
				mService.startNetworkCapture();
				switchStartStopButton();
			}
		};

		mOnClickStop = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mService.hasPacketsCaptured()) {
					mProgressDialog.show();
					stopService(mServiceIntent);
					mService.stopNetworkCapture();
				} else {
					mService.stopForeground(true);
					mToast.show();
				}
				switchStartStopButton();
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

		mBtnExport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				File file = new File(Environment.getExternalStorageDirectory(),
						SAVE_FILENAME);
				FileOutputStream fos;
				GZIPOutputStream gos;
				byte[] data = mService.getFlowTable().toByteArray();
				try {
					fos = new FileOutputStream(file);
					gos = new GZIPOutputStream(fos);
					gos.write(data);
					gos.flush();
					gos.close();
					fos.close();
					makeSaveToast(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		switchStartStopButton();
		switchSaveButton();
	}

	private void makeSaveToast(File file) {
		String msg = getResources().getString(R.string.file_save_successfull);
		Toast toast = Toast.makeText(this, " " + msg + file.getAbsolutePath(),
				Toast.LENGTH_LONG);
		toast.show();
	}

	private void switchSaveButton() {
		mBtnExport.setEnabled(!mService.getFlowTable().isEmpty());
	}

	private void switchStartStopButton() {
		boolean isCaptureStarted = mService.isCaptureRunning();
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_FILE) {
			if (resultCode == RESULT_OK) {
				handleFileImport(data
						.getCharSequenceExtra(FileImportActivity.FILE_KEY));
			}
		}
	}

	private void handleFileImport(CharSequence file) {
		mFilePath = file.toString();
		if (isPcap(mFilePath)) {
			mIPInputDialog.show();
		} else if (isCflow(mFilePath)) {
			mProgressDialog.show();
			mService.importCflowFile(mFilePath);
		} else {
			mWrongFileDialog.show();
		}
	}

	private boolean isCflow(String filePath) {
		String ext = getFileExtension(filePath);
		return ext.equals(FILE_EXTENSION_CFLOW);
	}

	private boolean isPcap(String filePath) {
		String ext = getFileExtension(filePath);
		return ext.equals(FILE_EXTENSION_PCAP);
	}

	private String getFileExtension(String filePath) {
		String ext = FileUtils.getExtension(filePath);
		Log.d(LOG_TAG, "File extension: " + ext);
		return ext;
	}

	private void generateGraphlet() {
		Log.d(LOG_TAG, "generating graphlet");
		mGraphlet.update(mService.getGraphlet());
		mTxtStart.setText(mService.getStartTime());
		mTxtEnd.setText(mService.getEndTime());
		switchSaveButton();
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

		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey("screenWidth")
				&& bundle.containsKey("screenHeight")) {
			this.screenWidth = bundle.getInt("screenWidth");
			this.screenHeight = bundle.getInt("screenHeight");
		} else {
			DisplayMetrics outMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
			screenWidth = outMetrics.widthPixels;
			screenHeight = outMetrics.heightPixels;
		}

		Log.v(LOG_TAG, "Screen width/height: " + screenWidth + "/"
				+ screenHeight);

		RatioResolutionPolicy pResolutionPolicy = new RatioResolutionPolicy(
				screenWidth, screenHeight);
		mZoomCamera = new ZoomCamera(0, 0, screenWidth, screenHeight); // floats
																		// pX,
																		// pY,
																		// pWidth,
																		// pHeight
		mZoomCamera.setBoundsEnabled(true);
		mZoomCamera.setBounds(0, screenWidth, 0, screenHeight);
		EngineOptions pEngineOptions = new EngineOptions(false,
				ScreenOrientation.LANDSCAPE, pResolutionPolicy, mZoomCamera);
		Engine myEngine = new Engine(pEngineOptions);

		try {
			if (MultiTouch.isSupported(this)) {
				myEngine.setTouchController(new MultiTouchController());
			} else {
				Toast.makeText(
						this,
						"Sorry your device does NOT support MultiTouch!\n\n(No PinchZoom is possible!)",
						Toast.LENGTH_LONG).show();
			}
		} catch (final MultiTouchException e) {
			Toast.makeText(
					this,
					"Sorry your Android Version does NOT support MultiTouch!\n\n(No PinchZoom is possible!)",
					Toast.LENGTH_LONG).show();
		}

		return myEngine;
	}

	@Override
	public void onLoadResources() {
		mTex = new BitmapTextureAtlas(1024, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mFont = new Font(mTex, Typeface.create(Typeface.DEFAULT,
				Typeface.NORMAL), 15, true, Color.BLACK);
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		mGraphlet = new Graphlet(screenWidth, screenHeight, mFont);
		mGraphlet.setOnSceneTouchListener(this);
		mGraphlet.setTouchAreaBindingEnabled(true);
		// mGraphlet.setOnAreaTouchTraversalFrontToBack();
		mGraphlet.setBackground(new ColorBackground(1f, 1f, 1f));

		this.mScrollDetector = new SurfaceScrollDetector(this);
		if (MultiTouch.isSupportedByAndroidVersion()) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLoadComplete() {
		mServiceIntent = new Intent(this, HAPdroidService.class);
		bindService(mServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (this.mPinchZoomDetector != null) {
			this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

			if (this.mPinchZoomDetector.isZooming()) {
				this.mScrollDetector.setEnabled(false);
			} else {
				if (pSceneTouchEvent.isActionDown()) {
					this.mScrollDetector.setEnabled(true);
				}
				this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
			}
		} else {
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onScroll(ScrollDetector pScollDetector, TouchEvent pTouchEvent,
			float pDistanceX, float pDistanceY) {
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY
				/ zoomFactor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pSceneTouchEvent) {
		this.mPinchZoomStartedCameraZoomFactor = this.mZoomCamera
				.getZoomFactor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPinchZoom(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pTouchEvent, float pZoomFactor) {
		if ((this.mPinchZoomStartedCameraZoomFactor * pZoomFactor) >= 1.0f) {
			this.mZoomCamera
					.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor
							* pZoomFactor);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pTouchEvent, float pZoomFactor) {
		if ((this.mPinchZoomStartedCameraZoomFactor * pZoomFactor) >= 1.0f) {
			this.mZoomCamera
					.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor
							* pZoomFactor);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getLayoutID() {
		return R.layout.hud;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.view_graphlet;
	}

}