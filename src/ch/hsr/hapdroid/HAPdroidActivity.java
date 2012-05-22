package ch.hsr.hapdroid;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import ch.hsr.hapdroid.HAPdroidService.HAPdroidBinder;
import ch.hsr.hapdroid.R.id;
import ch.hsr.hapdroid.network.Packet;

public class HAPdroidActivity extends Activity {
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RECEIVE_NETWORK_FLOW:
				Packet p = (Packet) msg.obj;
				mResultView.append(p.toString());
				break;
			case RECEIVE_TRANSACTION_TABLE:
				HAPGraphlet h = mService.getGraphlet();
				mResultView.setText(h.toString());
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
	private TextView mResultView;
	private Button mCaptureStartBtn;
	private Button mCaptureStopBtn;
	private boolean mBound;
	private HAPdroidService mService;
	private Button mShowGraphletBtn;
	
	public static final StringBuilder mResult = new StringBuilder();
	public static final int RECEIVE_NETWORK_FLOW = 0;
	public static final int RECEIVE_FLOW_TABLE = 1;
	public static final int RECEIVE_TRANSACTION_TABLE = 2;
	
	private static final String LOG_TAG = "HAPdroidRootActivity";
	private Intent mServiceIntent;

	/** 
	 * Called when the activity is first created. 
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mResultView = (TextView) findViewById(id.resultView);
		mResultView.setMovementMethod(new ScrollingMovementMethod());

		mCaptureStartBtn = (Button) findViewById(id.capture_start);
		mCaptureStopBtn = (Button) findViewById(id.capture_stop);
		mShowGraphletBtn = (Button) findViewById(id.show_graphlet);
		
	}
	
	private void setOnClickListeners() {
		mCaptureStartBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startService(mServiceIntent);
				mService.startNetworkCapture();
			}
		});

		mCaptureStopBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mService.stopNetworkCapture();
				mService.stopForeground(true);
				stopService(mServiceIntent);
			}
		});
		
		mShowGraphletBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(v.getContext(), HAPdroidGraphletActivity.class));
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		mServiceIntent = new Intent(this, HAPdroidService.class);
		bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if (mBound) {
			unbindService(mServiceConnection);
			mBound = false;
		}
	}
}
