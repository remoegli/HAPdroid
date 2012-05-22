package ch.hsr.hapdroid;

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
import ch.hsr.hapdroid.network.FlowTable;
import ch.hsr.hapdroid.network.NetworkStreamHandlerTask;
import ch.hsr.hapdroid.network.Packet;
import ch.hsr.hapdroid.transaction.Transaction;

import com.stericson.RootTools.RootTools;

public class HAPdroidService extends Service {

	private static final String EXECUTABLE = "flowdump ";
	private static final String CAPTURE_WLAN_CMD = "flowdump -i wlan0 ";
	private static final String CAPTURE_MOBILE_CMD = "flowdump -i vsnet0 ";

	public class HAPdroidBinder extends Binder {
		public HAPdroidService getService() {
			return HAPdroidService.this;
		}
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String s;
			switch(msg.what){
			case RECIEVE_PACKET:
				s = (String) msg.obj;
				Packet p = Packet.parsePacket(s);
				handlePacket(p);
				break;
			case RECIEVE_PACKET_FINISH:
				break;
			case RECIEVE_TRANSACTION:
				s = (String) msg.obj;
				handleTransaction(s);
				break;
			case RECIEVE_TRANSACTION_FINISH:
				finishGettingTransactions();
				break;
			}
		}
	};

	HAPdroidBinder mBinder = new HAPdroidBinder();
	private Handler mCallbackHandler;
	private NetworkStreamHandlerTask mNetworkCapture;
	private FlowTable mFlowTable;
	private NetworkStreamHandlerTask mTransactionCapture;
	private HAPGraphlet mHAPGraphlet;
	private Notification mNotification;
	private String[] mTransactionString;
	private int mTransactionStringPos;
	private boolean mHasRoot;
	
	public static final int RECIEVE_PACKET = 0;
	public static final int RECIEVE_PACKET_FINISH = 1;
	public static final int RECIEVE_TRANSACTION = 2;
	public static final int RECIEVE_TRANSACTION_FINISH = 3;
	
	private static final String LOG_TAG = "HAPdroidService";
	private static final String SERVER_TRANSACTIONS = "LocalServTrans";
	private static final String SERVER_PACKETS = "LocalServPackets";
	private static final int NOTIFICATION_ID = 42;

	private void handlePacket(Packet p) {
		mFlowTable.add(p);
		Log.d(LOG_TAG, "packet recieved");
		Message msg = new Message();
		msg.what = HAPdroidActivity.RECEIVE_NETWORK_FLOW;
		msg.obj = p;
		if(mCallbackHandler != null)
			mCallbackHandler.sendMessage(msg);
	}
	
	private void handleTransaction(String s){
		Log.d(LOG_TAG, "partial transaction recieved: " + s);
		if (s.length() > 1 && s.charAt(0) == 't'){
			Transaction t = Transaction.parse(mTransactionString);
			if (t != null)
				Log.d(LOG_TAG, "Parsed transaction: " + t.toString());
			mHAPGraphlet.add(t);
			resetTransactionString();
		}
		if (s.length() > 1 && s.charAt(0) == '-'){
			stopTransactionServer();
			return;
		}
		
		mTransactionString[mTransactionStringPos++] = s;
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
				"flowdump"))
			Toast.makeText(getApplicationContext(), "Unable to install binary",
					Toast.LENGTH_LONG);
		Log.d(LOG_TAG, "Binary successfully installed");
	}

	private void initNotification() {
		mNotification = new Notification(R.drawable.ic_notification, "HAPdroid Network Capture", System.currentTimeMillis());
		
		Intent notificationIntent = new Intent(this, HAPdroidActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
		mNotification.setLatestEventInfo(this, getText(R.string.notification_title),
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
	
	public HAPGraphlet getGraphlet(){
		return mHAPGraphlet;
	}

	private void getTransactions() {
		startTransactionServer();
		while (!mTransactionCapture.isReady()){
			sleep(500);
		}
		HAPvizLibrary.getTransactions(mFlowTable.toByteArray(), SERVER_TRANSACTIONS);
//		HAPvizLibrary.getTransactions("/sdcard/flows.gz", SERVER_TRANSACTIONS, "10.0.0.1", "255.255.255.255");
//		stopTransactionServer();
	}

	protected void finishGettingTransactions() {
		mCallbackHandler.sendEmptyMessage(HAPdroidActivity.RECEIVE_TRANSACTION_TABLE);
//		Log.d(LOG_TAG, "TransactionTable: " + mHAPGraphlet.toString());
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

	private void stopTransactionServer() {
		if (mTransactionCapture != null &&
				mTransactionCapture.getStatus() == Status.RUNNING)
			mTransactionCapture.stopServer();
	}

	public void startNetworkCapture() throws UnsupportedOperationException{
		if (!mHasRoot){
			throw new UnsupportedOperationException();
		}
		
		startNetworkCaptureServer();
		while (!mNetworkCapture.isReady()) {
			sleep(500);
		}
		startWlanCapture();
		startMobileCapture();
		
		startForeground(NOTIFICATION_ID, mNotification);
	}
	
	public void startExecutableCapture(String params){
		startNetworkCaptureServer();
		while (!mNetworkCapture.isReady()) {
			sleep(500);
		}
		startExecutable(params);
		
		// we cant start the service in foreground because this will
		// break testing with a NPE. See android issue 12122
		//startForeground(NOTIFICATION_ID, mNotification);
	}
	
	private void startExecutable(String params){
//		RootTools.useRoot = false;
		try {
			RootTools.runBinary(getApplicationContext(), EXECUTABLE + params,
					"-s " + SERVER_PACKETS);
			Log.d(LOG_TAG, "executable started with params: " + params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
//			RootTools.useRoot = true;
		}
	}

	/**
	 * Sleep for a given time.
	 * 
	 * Currently this is sort of a hack. But unfortunately it is
	 * necessary in order to make sure the ServerSocket is ready
	 * when the clien processes get called.
	 * 
	 * @param ms
	 */
	private void sleep(long ms) {
		synchronized(this) {
			try {
				wait(ms);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void startNetworkCaptureServer() {
		if (mNetworkCapture != null) {
			if (mNetworkCapture.getStatus() != Status.FINISHED) {
				return;
			}
		}
		mNetworkCapture = new NetworkStreamHandlerTask(SERVER_PACKETS, 
				mHandler, RECIEVE_PACKET, RECIEVE_PACKET_FINISH);
		mNetworkCapture.execute();
	}

	private void startMobileCapture() {
		try {
			RootTools.runBinary(getApplicationContext(), CAPTURE_MOBILE_CMD,
					"-s " + SERVER_PACKETS);
			Log.d(LOG_TAG, "Mobile capture started");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void startWlanCapture() {
		try {
			RootTools.runBinary(getApplicationContext(), CAPTURE_WLAN_CMD,
					"-s " + SERVER_PACKETS);
			Log.d(LOG_TAG, "WLAN capture started");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopNetworkCapture() {
		stopWlanCapture();
		stopMobileCapture();
		stopNetworkCaptureServer();
		getTransactions();
		
		stopForeground(true);
	}

	private void stopWlanCapture() {
		if (RootTools.isProcessRunning(CAPTURE_WLAN_CMD)){
				RootTools.killProcess(CAPTURE_WLAN_CMD);
		}
	}

	private void stopMobileCapture() {
		if (RootTools.isProcessRunning(CAPTURE_MOBILE_CMD)){
				RootTools.killProcess(CAPTURE_MOBILE_CMD);
		}
	}

	private void stopNetworkCaptureServer() {
		if (mNetworkCapture != null &&
				mNetworkCapture.getStatus() == Status.RUNNING)
			mNetworkCapture.stopServer();
	}

	public void clearCapture() {
		mFlowTable.clear();
		mHAPGraphlet.clear();
	}

	public FlowTable getFlowTable() {
		return mFlowTable;
	}

	public boolean isCaptureRunning() {
		if (mNetworkCapture == null)
			return false;
		return mNetworkCapture.getStatus() == Status.RUNNING;
	}

}
