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
import ch.hsr.hapdroid.network.FlowTable;
import ch.hsr.hapdroid.network.Packet;

public class HAPdroidRootActivity extends Activity {
	private Handler mHandler = new Handler() {
	
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RECEIVE_NETWORK_FLOW:
				String packet = msg.obj.toString();
				mResultView.append(Packet.parsePacket(packet).toString());
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
	private TextView mResultView;
	private Button mCaptureWlanBtn;
	private Button mCaptureMobileBtn;
	private Button mStopCaptureBtn;
	private boolean mBound;
	private HAPdroidService mService;
	private FlowTable mFlowTable;

	public static final StringBuilder mResult = new StringBuilder();
	public static final int RECEIVE_NETWORK_FLOW = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mFlowTable = new FlowTable();

		mResultView = (TextView) findViewById(id.resultView);
		mResultView.setMovementMethod(new ScrollingMovementMethod());

		mCaptureWlanBtn = (Button) findViewById(id.capturewlan_btn);
		mCaptureMobileBtn = (Button) findViewById(id.capturemobile_btn);
		mStopCaptureBtn = (Button) findViewById(id.stop_capture);
	}

	protected void addToFlow(String packet) {
		mFlowTable.add(Packet.parsePacket(packet));
	}

	private void setOnClickListeners() {
		mCaptureWlanBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mService.startWlanCapture();
			}
		});

		mCaptureMobileBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mService.startMobileCapture();
			}
		});

		mStopCaptureBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mService != null) {
					mService.stopCapture();
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, HAPdroidService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
		startService(intent);
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
