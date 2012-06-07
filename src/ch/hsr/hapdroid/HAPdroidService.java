package ch.hsr.hapdroid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

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
import ch.hsr.hapdroid.network.CaptureSource;
import ch.hsr.hapdroid.network.Flow;
import ch.hsr.hapdroid.network.FlowTable;
import ch.hsr.hapdroid.network.NetworkStreamHandlerTask;
import ch.hsr.hapdroid.network.Packet;
import ch.hsr.hapdroid.network.Timeval;
import ch.hsr.hapdroid.transaction.Transaction;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootTools.Result;

/**
 * Service class which does the capturing of network traffic in 
 * Background.
 * 
 * The HAPdroidService class manages all aspects of the network 
 * capturing. Hence it is responsible to receive and parse the packets
 * captured by the flowdump executable as well as passing those 
 * packets to the FlowTable. It provides methods for live packet 
 * capture as well as for pcap and cflow file import. 
 * For the live capture functionality, root access is needed. The
 * RootTools library project has been used for the installation and
 * execution of the binary.
 * 
 * Also it manages the conversion from the FlowTable to Transactions
 * and adding the generated Transactions to the HAPGraphlet.
 * 
 * @author Dominik Spengler
 * @see http://developer.android.com/guide/topics/fundamentals/services.html
 * @see http://code.google.com/p/roottools/
 */
public class HAPdroidService extends Service {

	private static final String EXECUTABLE = "flowdump";
	private static final String CAPTURE_WLAN_CMD = "flowdump -i wlan0";
	private static final String CAPTURE_MOBILE_CMD = "flowdump -i vsnet0";

	/**
	 * Communication interface for any client with the Service.
	 * 
	 * @author Dominik Spengler
	 * @see Binder
	 * 
	 */
	public class HAPdroidBinder extends Binder {
		/**
		 * Simple getter.
		 * 
		 * @return {@link HAPdroidService}
		 */
		public HAPdroidService getService() {
			return HAPdroidService.this;
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Transaction t;
			switch (msg.what) {
			case RECIEVE_PACKET_FINISH:
				getTransactions();
				break;
			case RECIEVE_TRANSACTION:
				t = (Transaction) msg.obj;
				handleTransaction(t);
				break;
			case RECIEVE_TRANSACTION_FINISH:
				Log.d(LOG_TAG, "finish getting transactions");
				finishGettingTransactions();
				break;
			}
		}
	};
	private NetworkResult mWlanResult;
	private NetworkResult mMobileResult;
	private NetworkResult mExecutableResult;

	private HAPdroidBinder mBinder = new HAPdroidBinder();
	private Handler mCallbackHandler;
	private FlowTable mFlowTable;
	private NetworkStreamHandlerTask mTransactionCapture;
	private HAPGraphlet mHAPGraphlet;
	private Notification mNotification;
	private String mFileDir;
	private Packet mCurrentPacket;
	private boolean mHasRoot;
	private boolean mCaptureRunning = false;

	/**
	 * Message identifier for receiving a packet from the executable. 
	 */
	public static final int RECIEVE_PACKET = 0;
	/**
	 * Message identifier for finishing to receive packets.
	 */
	public static final int RECIEVE_PACKET_FINISH = 1;
	/**
	 * Message identifier for receiving a transaction from the library.
	 */
	public static final int RECIEVE_TRANSACTION = 2;
	/**
	 * Message identifier for finishing to recieve transactions.
	 */
	public static final int RECIEVE_TRANSACTION_FINISH = 3;
	/**
	 * Message identifier used when sending a network flow.
	 */
	private static final int SEND_NETWORK_FLOW = 4;
	/**
	 * Message identifier for generating the graphlet. This is mainly
	 * used once all the transactions are received.
	 */
	public static final int GENERATE_GRAPHLET = 5;

	private static final String LOG_TAG = "HAPdroidService";
	private static final String SERVER_TRANSACTIONS = "LocalServTrans";
	private static final int NOTIFICATION_ID = 42;

	private void handlePacket(Packet p) {
		if (p == null)
			return;

		mFlowTable.add(p);
		Message msg = new Message();
		msg.what = SEND_NETWORK_FLOW;
		msg.obj = p;
		if (mCallbackHandler != null)
			mCallbackHandler.sendMessage(msg);
	}

	private void handleTransaction(Transaction t) {
		List<Flow> flowlist = mFlowTable.getFlowsForTransaction(t);
		t.setFlows(flowlist);
		Log.d(LOG_TAG, "Parsed transaction: " + t.toString());
		mHAPGraphlet.add(t);
	}

	/**
	 * 
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		RootTools.debugMode = true;

		mHasRoot = RootTools.isRootAvailable();
		RootTools.useRoot = mHasRoot;

		mFlowTable = new FlowTable();
		mHAPGraphlet = new HAPGraphlet();
		mWlanResult = new NetworkResult(CaptureSource.WLAN);
		mMobileResult = new NetworkResult(CaptureSource.MOBILE);
		mExecutableResult = new NetworkResult(CaptureSource.PCAP);

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

		Intent notificationIntent = new Intent(this, SplashActivity.class);
		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		mNotification.setLatestEventInfo(this,
				getText(R.string.notification_title),
				getText(R.string.notification_message), pendingIntent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		setCallbackHandler(null);
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Setter for the Handler used to send messages.
	 * 
	 * The Handler set in this method will be used to send messages for
	 * specified actions.
	 * 
	 * @param aHandler Handler to be notified with packages and transactions.
	 * @see #SEND_NETWORK_FLOW
	 * @see #GENERATE_GRAPHLET
	 * @see Handler
	 */
	public void setCallbackHandler(Handler aHandler) {
		mCallbackHandler = aHandler;
	}

	/**
	 * Simple getter for the HAPGraphlet.
	 * 
	 * @return {@link HAPGraphlet}
	 */
	public HAPGraphlet getGraphlet() {
		return mHAPGraphlet;
	}

	private void getTransactions() {
		startTransactionServer();
		HAPvizLibrary.getTransactions(mFlowTable.toByteArray(),
				SERVER_TRANSACTIONS);
	}

	private void finishGettingTransactions() {
		if (mCallbackHandler != null)
			mCallbackHandler
					.sendEmptyMessage(GENERATE_GRAPHLET);
		Log.d(LOG_TAG, mHAPGraphlet.toString());
		Log.d(LOG_TAG, mHAPGraphlet.showTransactions());
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

	/**
	 * Starts the network capture.
	 * 
	 * This method will start the network capture in the background. The
	 * capture will run until a call to {@link #stopNetworkCapture()} is 
	 * issued.
	 * 
	 * @throws UnsupportedOperationException if no root access is available
	 */
	public void startNetworkCapture() throws UnsupportedOperationException {
		if (!mHasRoot) {
			throw new UnsupportedOperationException();
		}
		startWlanCapture();
		// startMobileCapture();

		startForeground(NOTIFICATION_ID, mNotification);
		mCaptureRunning = true;
	}

	/**
	 * Starts the executable with the given parameters.
	 * 
	 * This method start the capture from the executable, builds
	 * the flow table, generates the transactions and builds
	 * the {@link HAPGraphlet}.
	 * The results from the executable are provided using the 
	 * callback handler.
	 * 
	 * @see #setCallbackHandler(Handler)
	 * @param params parameters to be passed to the executable
	 */
	public void startExecutableCapture(String params) {
		startExecutable(params);

		// we cant start the service in foreground because this will
		// break testing with a NPE. See android issue 12122
	}

	private void startExecutable(final String params) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					RootTools.sendShell(mFileDir + EXECUTABLE + " " + params,
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
							mMobileResult, -1);
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
							mWlanResult, -1);
					Log.d(LOG_TAG, "WLAN capture started");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Stopping the netork capture.
	 * 
	 * Will do nothing if the capture is not running.
	 */
	public void stopNetworkCapture() {
		if (!mCaptureRunning)
			return;
		
		stopWlanCapture();
		// stopMobileCapture();
		synchronized (this) {
			try {
				wait(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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

	/**
	 * Clears the capture.
	 * 
	 * This method will clear the flow table as well as create
	 * a new HAPGraphlet.
	 */
	public void clearCapture() {
		mFlowTable.clear();
		mHAPGraphlet = new HAPGraphlet();
	}

	/**
	 * Getter for flow table.
	 * 
	 * @return {@link FlowTable} generated FlowTable
	 */
	public FlowTable getFlowTable() {
		return mFlowTable;
	}

	/**
	 * Checks whether the network capture currently is running.
	 * 
	 * @return true if the network capture is running, false 
	 * 		otherwise
	 */
	public boolean isCaptureRunning() {
		return mCaptureRunning;
	}

	/**
	 * Reads a pcap file and generates the flow table and {@link HAPGraphlet}.
	 * 
	 * This method will clear all previous captures and read in the
	 * given pcap file.
	 * 
	 * For any non pcap file the behaviour is undefined.
	 * 
	 * @param filePath the absolute file path
	 * @param ip dotted string representation of the source ip to 
	 * 		be used
	 */
	public void importPcapFile(String filePath, String ip){
		clearCapture();
		mFlowTable.setSourceIp(ip);
		startExecutableCapture("-p " + filePath);
	}
	
	/**
	 * Reads a cflow file and generates the flow table and {@link HAPGraphlet}.
	 * 
	 * This method will clear all previous captures and read in the
	 * given cflow file. Please note that a gzipped cflow4 file is
	 * expected. For any other file the behaviour is undefined.
	 * 
	 * @param filePath absolute file path to gzipped cflow4 file
	 */
	public void importCflowFile(String filePath){
		clearCapture();
		try {
			File file = new File(filePath.toString());
			FileInputStream fis = new FileInputStream(file);
			GZIPInputStream gis = new GZIPInputStream(fis);
			mFlowTable.importByteArray(getByteArray(gis));
			gis.close();
			fis.close();
			getTransactions();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private byte[] getByteArray(GZIPInputStream gis) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[Flow.SIZE_BYTE];
		int n = 0;
		try {
			while (-1 != (n = gis.read(buffer))) {
				output.write(buffer, 0, n);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output.toByteArray();
	}

	/**
	 * Get the start time of the first flow in the flow table.
	 * 
	 * @return {@link Timeval} start time 
	 */
	public Timeval getStartTime() {
		return mFlowTable.getStartTime();
	}

	/**
	 * Get the end time of the last flow in the flow table.
	 * 
	 * @return {@link Timeval} end time
	 */
	public Timeval getEndTime() {
		return mFlowTable.getEndTime();
	}

	/**
	 * Check whether packets have been captured by the flow table.
	 * 
	 * @return true if at least one packet has been captured, false
	 * 		otherwise.
	 */
	public boolean hasPacketsCaptured() {
		return !mFlowTable.isEmpty();
	}

	/**
	 * Class used as a result callback for the executable.
	 * 
	 * The NetworkResult class is used to process the results
	 * given from the executable as well as tag the packets 
	 * with the capture source.
	 * 
	 * @author "Dominik Spengler"
	 * @see Result
	 * @see CaptureSource
	 */
	private class NetworkResult extends Result {

		private CaptureSource mSource;

		public NetworkResult(CaptureSource source) {
			mSource = source;
		}

		@Override
		public void processError(String line) throws Exception {
			// TODO Auto-generated method stub

		}

		@Override
		public void process(String line) throws Exception {
			mCurrentPacket = Packet.parsePacket(line);
			mCurrentPacket.source = mSource;
			handlePacket(mCurrentPacket);
		}

		@Override
		public void onFailure(Exception ex) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onComplete(int diag) {
			mHandler.sendEmptyMessage(RECIEVE_PACKET_FINISH);
		}
	}
}
