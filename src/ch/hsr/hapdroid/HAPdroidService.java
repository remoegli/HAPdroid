package ch.hsr.hapdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.app.Service;
import android.content.Intent;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import ch.hsr.hapdroid.network.FlowTable;
import ch.hsr.hapdroid.network.Packet;

import com.stericson.RootTools.RootTools;

public class HAPdroidService extends Service {

	private static final String CAPTURE_WLAN_CMD = "flowdump wlan0 ";
	private static final String CAPTURE_MOBILE_CMD = "flowdump vsnet0 ";

	public class HAPdroidBinder extends Binder {
		public HAPdroidService getService() {
			return HAPdroidService.this;
		}
	}

	HAPdroidBinder mBinder = new HAPdroidBinder();
	private Handler mHandler;
	private NetworkCaptureTask mNetworkCapture;
	private FlowTable mFlowTable;

	private static final String LOG_TAG = "HAPdroidService";

	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "Service onCreate()");
		super.onCreate();
		RootTools.debugMode = true;

		installBinary();
		mFlowTable = new FlowTable();
	}

	private void installBinary() {
		if (!RootTools.installBinary(getApplicationContext(), R.raw.flowdump,
				"flowdump"))
			Toast.makeText(getApplicationContext(), "Unable to install binary",
					Toast.LENGTH_LONG);
		Log.d(LOG_TAG, "Binary successfully installed");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(LOG_TAG, "Service onBind()");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(LOG_TAG, "Service onUnbind()");
		setCallbackHandler(null);
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mNetworkCapture != null) {
			if (mNetworkCapture.getStatus() == Status.FINISHED) {
				mNetworkCapture = new NetworkCaptureTask();
				mNetworkCapture.execute();
			}
			return Service.START_STICKY;
		}
		mNetworkCapture = new NetworkCaptureTask();
		mNetworkCapture.execute();
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "Service onDestroy()");
		super.onDestroy();
	}

	private void sendFlowTable() {
		Message msg = new Message();
		msg.what = HAPdroidRootActivity.RECEIVE_FLOW_TABLE;
		msg.obj = mFlowTable;
		if (mHandler != null)
			mHandler.sendMessage(msg);
	}

	public void setCallbackHandler(Handler aHandler) {
		mHandler = aHandler;
		Log.d(LOG_TAG, "Service CallbackHandler set");
	}

	public void startWlanCapture() {
		try {
			RootTools.runBinary(getApplicationContext(), CAPTURE_WLAN_CMD,
					NetworkCaptureTask.SERVER_NAME);
			Log.d(LOG_TAG, "WLAN capture started");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopWlanCapture() {
		if (RootTools.isProcessRunning(CAPTURE_WLAN_CMD)){
				RootTools.killProcess(CAPTURE_WLAN_CMD);
		}
	}

	public void startMobileCapture() {
		try {
			RootTools.runBinary(getApplicationContext(), CAPTURE_MOBILE_CMD,
					NetworkCaptureTask.SERVER_NAME);
			Log.d(LOG_TAG, "Mobile capture started");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopMobileCapture() {
		if (RootTools.isProcessRunning(CAPTURE_MOBILE_CMD)){
				RootTools.killProcess(CAPTURE_MOBILE_CMD);
		}
	}

	public void stopCapture() {
		stopWlanCapture();
		stopMobileCapture();
		mNetworkCapture.stopServer();
	}

	private class NetworkCaptureTask extends AsyncTask<Void, String, Void> {
		private LocalServerSocket mServerSocket;
		private BufferedReader mReader;
		private InputStream mInputStream;
		private Message mMessage;
		private boolean mServerShouldStop;

		public static final String SERVER_NAME = "NetCaptureServ";
		private static final String LOG_TAG = "NetworkCaptureTask";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mServerShouldStop = false;
			Log.d(LOG_TAG, "NetworkCapture server started");
		}

		public void stopServer() {
			mServerShouldStop = true;
			shutdown();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				mServerSocket = new LocalServerSocket(SERVER_NAME);
			} catch (IOException e) {
				Log.e(LOG_TAG, "Creation of server failed: " + e.getMessage());
				e.printStackTrace();
			}

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

			mFlowTable.add(p);
		}

		@Override
		protected void onPostExecute(Void result) {
			shutdown();
		}

		private void shutdown() {
			if (mServerSocket != null) {
				try {
					mInputStream.close();
					mServerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.d(LOG_TAG, "NetworkCapture server stoped");
			}
			sendFlowTable();
			stopSelf();
		}

	}
}
