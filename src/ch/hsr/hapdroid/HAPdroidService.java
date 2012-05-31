package ch.hsr.hapdroid;

import java.io.IOException;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask.Status;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import ch.hsr.hapdroid.network.Flow;
import ch.hsr.hapdroid.network.FlowTable;
import ch.hsr.hapdroid.network.NetworkStreamHandlerTask;
import ch.hsr.hapdroid.network.Packet;
import ch.hsr.hapdroid.transaction.Transaction;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootTools.Result;

public class HAPdroidService extends Service {

	private static final String EXECUTABLE = "flowdump";
	private static final String CAPTURE_WLAN_CMD = "flowdump -i wlan0";
	private static final String CAPTURE_MOBILE_CMD = "flowdump -i vsnet0";

	public class HAPdroidBinder extends Binder {
		public HAPdroidService getService() {
			return HAPdroidService.this;
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String s;
			switch (msg.what) {
			case RECIEVE_PACKET_FINISH:
				getTransactions();
				break;
			case RECIEVE_TRANSACTION:
				s = (String) msg.obj;
				handleTransaction(s);
				break;
			case RECIEVE_TRANSACTION_FINISH:
				Log.d(LOG_TAG, "finish getting transactions");
				finishGettingTransactions();
				break;
			}
		}
	};
	private Result mNetworkResult = new Result() {


		@Override
		public void processError(String line) throws Exception {
			// TODO Auto-generated method stub

		}

		@Override
		public void process(String line) throws Exception {
			mCurrentPacket = Packet.parsePacket(line);
			handlePacket(mCurrentPacket);
		}

		@Override
		public void onFailure(Exception ex) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onComplete(int diag) {
			// TODO Auto-generated method stub
		}
	};
	private Result mExecutableResult = new Result() {

		@Override
		public void processError(String line) throws Exception {
			// TODO Auto-generated method stub

		}

		@Override
		public void process(String line) throws Exception {
			Packet p = Packet.parsePacket(line);
			if (p == null)
				return;

			mFlowTable.add(p);
		}

		@Override
		public void onFailure(Exception ex) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onComplete(int diag) {
			//because we cant start the transaction server outside the UI thread
			//we use the handler to execute the UI code
			mHandler.sendEmptyMessage(RECIEVE_PACKET_FINISH);
		}
	};
	private HAPdroidBinder mBinder = new HAPdroidBinder();
	private Handler mCallbackHandler;
	private FlowTable mFlowTable;
	private NetworkStreamHandlerTask mTransactionCapture;
	private HAPGraphlet mHAPGraphlet;
	private Notification mNotification;
	private String[] mTransactionString;
	private int mTransactionStringPos;
	private String mFileDir;
	private Packet mCurrentPacket;
	private boolean mHasRoot;
	private boolean mCaptureRunning = false;

	public static final int RECIEVE_PACKET = 0;
	public static final int RECIEVE_PACKET_FINISH = 1;
	public static final int RECIEVE_TRANSACTION = 2;
	public static final int RECIEVE_TRANSACTION_FINISH = 3;

	private static final String LOG_TAG = "HAPdroidService";
	private static final String SERVER_TRANSACTIONS = "LocalServTrans";
	private static final int NOTIFICATION_ID = 42;

	private void handlePacket(Packet p) {
		if (p == null)
			return;
		
		mFlowTable.add(p);
		Message msg = new Message();
		msg.what = HAPdroidGraphletActivity.RECEIVE_NETWORK_FLOW;
		msg.obj = p;
		Log.d(LOG_TAG, "package recieved: " + p.toString());
		if (mCallbackHandler != null)
			mCallbackHandler.sendMessage(msg);
	}

	private void handleTransaction(String s) {
		Log.d(LOG_TAG, "partial transaction recieved: " + s);
		if (s.length() > 1 && s.charAt(0) == 't') {
			parseTransaction();
		}
		if (s.length() > 1 && s.charAt(0) == '-') {
			parseTransaction();
			return;
		}

		mTransactionString[mTransactionStringPos++] = s;
	}

	private void parseTransaction() {
		Transaction t = Transaction.parse(mTransactionString);
		if (t != null)
			Log.d(LOG_TAG, "Parsed transaction: " + t.toString());
		mHAPGraphlet.add(t);
		resetTransactionString();
	}

	private void resetTransactionString() {
		mTransactionString = new String[7];
		mTransactionStringPos = 0;
	}

	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "Service onCreate()");
		super.onCreate();
		RootTools.debugMode = true;

		mHasRoot = RootTools.isRootAvailable();
		RootTools.useRoot = mHasRoot;
		mFlowTable = new FlowTable();
		mHAPGraphlet = new HAPGraphlet();
		resetTransactionString();

		installBinary();
		initNotification();
	}

	private void installBinary() {
		if (!RootTools.installBinary(getApplicationContext(), R.raw.flowdump,
				EXECUTABLE, "0755"))
			Toast.makeText(getApplicationContext(), "Unable to install binary",
					Toast.LENGTH_LONG);

		try {
			mFileDir = getApplicationContext().getFilesDir().getCanonicalPath() + '/';
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initNotification() {
		mNotification = new Notification(R.drawable.ic_notification,
				"HAPdroid Network Capture", System.currentTimeMillis());

		Intent notificationIntent = new Intent(this,
				HAPdroidGraphletActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		mNotification.setLatestEventInfo(this,
				getText(R.string.notification_title),
				getText(R.string.notification_message), pendingIntent);
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
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "Service onDestroy()");
		super.onDestroy();
	}

	public void setCallbackHandler(Handler aHandler) {
		mCallbackHandler = aHandler;
		Log.d(LOG_TAG, "CallbackHandler set");
	}

	public HAPGraphlet getGraphlet() {
		return mHAPGraphlet;
	}

	private void getTransactions() {
		startTransactionServer();
		HAPvizLibrary.getTransactions(mFlowTable.toByteArray(),
				SERVER_TRANSACTIONS);
	}
	
	public List<Flow> desummarizeTransaction(Transaction t){
		return mFlowTable.getFlowsForTransaction(t);
	}

	protected void finishGettingTransactions() {
		if (mCallbackHandler != null)
			mCallbackHandler
				.sendEmptyMessage(HAPdroidGraphletActivity.GENERATE_GRAPHLET);
	}

	private void startTransactionServer() {
		if (mTransactionCapture != null) {
			if (mTransactionCapture.getStatus() != Status.FINISHED) {
				return;
			}
		}
		mTransactionCapture = new NetworkStreamHandlerTask(SERVER_TRANSACTIONS,
				mHandler, RECIEVE_TRANSACTION, RECIEVE_TRANSACTION_FINISH);
		mTransactionCapture.execute();
	}

	public void startNetworkCapture() throws UnsupportedOperationException {
		if (!mHasRoot) {
			throw new UnsupportedOperationException();
		}
		startWlanCapture();
		startMobileCapture();

		startForeground(NOTIFICATION_ID, mNotification);
		mCaptureRunning = true;
	}

	public void startExecutableCapture(String params) {
		mHAPGraphlet.clear();
		startExecutable(params);

		// we cant start the service in foreground because this will
		// break testing with a NPE. See android issue 12122
	}

	private void startExecutable(final String params) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					RootTools.sendShell(mFileDir + EXECUTABLE + " " +params,
							mExecutableResult, -1);
					Log.d(LOG_TAG, "executable started with params: " + params);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void startMobileCapture() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					RootTools.sendShell(mFileDir + CAPTURE_MOBILE_CMD,
							mNetworkResult, -1);
					Log.d(LOG_TAG, "Mobile capture started");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void startWlanCapture() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					RootTools.sendShell(mFileDir + CAPTURE_WLAN_CMD,
							mNetworkResult, -1);
					Log.d(LOG_TAG, "WLAN capture started");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void stopNetworkCapture() {
		stopWlanCapture();
		stopMobileCapture();
		getTransactions();

		stopForeground(true);
		mCaptureRunning = false;
	}

	private void stopWlanCapture() {
		if (RootTools.isProcessRunning(CAPTURE_WLAN_CMD)) {
			RootTools.killProcess(CAPTURE_WLAN_CMD);
		}
	}

	private void stopMobileCapture() {
		if (RootTools.isProcessRunning(CAPTURE_MOBILE_CMD)) {
			RootTools.killProcess(CAPTURE_MOBILE_CMD);
		}
	}

	public void clearCapture() {
		mFlowTable.clear();
		mHAPGraphlet.clear();
	}

	public FlowTable getFlowTable() {
		return mFlowTable;
	}

	public boolean isCaptureRunning() {
		return mCaptureRunning;
	}

	public void importFile(CharSequence filePath) {
		startExecutableCapture("-p " + filePath);
	}

}
