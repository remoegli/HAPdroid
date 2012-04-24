package ch.hsr.hapdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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
				Packet p = (Packet) msg.obj;
				mResultView.append(p.toString());
				break;
			case RECEIVE_FLOW_TABLE:
				FlowTable f = (FlowTable) msg.obj;
				mResultView.setText(f.toString());
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
	private Button mTestLibBtn;

	public static final StringBuilder mResult = new StringBuilder();
	public static final int RECEIVE_NETWORK_FLOW = 0;
	public static final int RECEIVE_FLOW_TABLE = 1;
	private static final String LOG_TAG = "HAPdroidRootActivity";
	private NetworkCaptureTask task;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mResultView = (TextView) findViewById(id.resultView);
		mResultView.setMovementMethod(new ScrollingMovementMethod());

		mCaptureWlanBtn = (Button) findViewById(id.capturewlan_btn);
		mCaptureMobileBtn = (Button) findViewById(id.capturemobile_btn);
		mStopCaptureBtn = (Button) findViewById(id.stop_capture);
		mTestLibBtn = (Button) findViewById(id.libhapviz);
		
		task = new NetworkCaptureTask();
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
		mTestLibBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				task.execute();
				Log.d(LOG_TAG, "getting transactions");
				HAPvizLibrary.getTransactions("/sdcard/flows.gz", NetworkCaptureTask.SERVER_NAME, "10.0.1.3", "255.255.255.255");
				Log.d(LOG_TAG, "finished getting transactions");
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
		
		task.stopServer();
		if (mBound) {
			unbindService(mServiceConnection);
			mBound = false;
		}
	}
	
	private class NetworkCaptureTask extends AsyncTask<Void, String, Void> {
		private LocalServerSocket mServerSocket;
		private BufferedReader mReader;
		private InputStream mInputStream;
		private Message mMessage;
		private boolean mServerShouldStop;

		public static final String SERVER_NAME = "LocalServSock";
		private static final String LOG_TAG = "NetworkCaptureTask";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mServerShouldStop = false;
			try {
				mServerSocket = new LocalServerSocket(SERVER_NAME);
			} catch (IOException e) {
				Log.e(LOG_TAG, "Creation of server failed: " + e.getMessage());
				e.printStackTrace();
			}
			Log.d(LOG_TAG, "NetworkCapture server started: " + SERVER_NAME);
		}

		public void stopServer() {
			mServerShouldStop = true;
			shutdown();
		}

		@Override
		protected Void doInBackground(Void... params) {

			String line;
			Log.d(LOG_TAG, "Start reading lines");
			try {
				while (!mServerShouldStop) {
					LocalSocket sk = mServerSocket.accept();
					mInputStream = sk.getInputStream();
					mReader = new BufferedReader(new InputStreamReader(
							mInputStream));
					while ((line = mReader.readLine()) != null) {
						publishProgress(line);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(LOG_TAG, "Finished reading lines");
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			Packet p = Packet.parsePacket(values[0]);

			mMessage = new Message();
			mMessage.what = HAPdroidRootActivity.RECEIVE_NETWORK_FLOW;
			mMessage.obj = p;
			if (mHandler != null)
				mHandler.sendMessage(mMessage);
		}

		@Override
		protected void onPostExecute(Void result) {
			shutdown();
		}

		private void shutdown() {
			if (mInputStream != null) {
				try {
					mInputStream.close();
					mServerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.d(LOG_TAG, "NetworkCapture server stoped");
			};
		}

	}
}
