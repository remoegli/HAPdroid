package ch.hsr.hapdroid.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import ch.hsr.hapdroid.graph.Transaction;

/**
 * {@link AsyncTask} implementation that creates a {@link LocalServerSocket}
 * for reading and passes callback messages to a {@link Handler}.
 * 
 * This class reads from the {@link LocalServerSocket} specified by 
 * servername until the connection is closed by the client. Also it 
 * sends a Message progressMsg containing the parsed transaction
 * received from the local server socket to the handler. The shutdownMsg 
 * is sent to the handler once the client closes the connection and the 
 * task will be stopped.
 * 
 * @author "Dominik Spengler"
 * @see AsyncTask
 *
 */
public class LocalServerTransactionHandlerTask extends AsyncTask<Void, String, Void> {
	private LocalServerSocket mServerSocket;
	private BufferedReader mReader;
	private InputStream mInputStream;
	private Message mMessage;
	private String mServerName;
	private Handler mHandler;
	private int mProgressMessage;
	private int mShutdownMessage;
	private String[] mTransactionString;
	private int mTransactionStringPos;

	private static final String LOG_TAG = "LocalServerTransactionHandlerTask";

	/**
	 * Constructs a new LocalServerTransactionHandlerTask.
	 * 
	 * @param servername name of the {@link LocalServerSocket}
	 * @param handler callback handler
	 * @param progressMsg message identifier for line read messages
	 * @param shutdownMsg message identifier for shutdown message
	 */
	public LocalServerTransactionHandlerTask(String servername, Handler handler,
			int progressMsg, int shutdownMsg) {
		mServerName = servername;
		mHandler = handler;
		mProgressMessage = progressMsg;
		mShutdownMessage = shutdownMsg;	
		resetTransactionString();

	}

	/**
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(LOG_TAG, mServerName + ": Server started");
	}

	/**
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		try {
			mServerSocket = new LocalServerSocket(mServerName);
		} catch (IOException e) {
			Log.e(LOG_TAG,
					mServerName + ": Creation of server failed"
							+ e.getMessage());
			e.printStackTrace();
		}

		String line;
		Log.d(LOG_TAG, mServerName + ": Start reading lines");
		try {
			LocalSocket sk = mServerSocket.accept();
			mInputStream = sk.getInputStream();
			mReader = new BufferedReader(new InputStreamReader(mInputStream));
			while ((line = mReader.readLine()) != null) {
				handleTransaction(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(LOG_TAG, mServerName + ": Finished reading lines");
		return null;
	}

	/**
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(String... values) {
		// do nothing
	}
	
	private void handleTransaction(String s) {
		if (s.length() > 0 && s.charAt(0) == 't') {
			parseTransaction();
		}
		if (s.length() > 0 && s.charAt(0) == '-') {
			parseTransaction();
			return;
		}

		mTransactionString[mTransactionStringPos++] = s;
	}

	private void parseTransaction() {
		Transaction t = Transaction.parse(mTransactionString);
		if (t != null){
			mMessage = new Message();
			mMessage.what = mProgressMessage;
			mMessage.obj = t;
			if (mHandler != null) {
				mHandler.sendMessage(mMessage);
			}
		}
		resetTransactionString();
	}

	private void resetTransactionString() {
		mTransactionString = new String[7];
		mTransactionStringPos = 0;
	}

	/**
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Void result) {
		mHandler.sendEmptyMessage(mShutdownMessage);
		try {
			mReader.close();
			mInputStream.close();
			mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
