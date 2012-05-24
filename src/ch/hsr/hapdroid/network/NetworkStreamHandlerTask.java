package ch.hsr.hapdroid.network;

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

public class NetworkStreamHandlerTask extends AsyncTask<Void, String, Void> {
	private LocalServerSocket mServerSocket;
	private BufferedReader mReader;
	private InputStream mInputStream;
	private Message mMessage;
	private String mServerName;
	private Handler mHandler;
	private int mProgressMessage;
	private int mShutdownMessage;

	private static final String LOG_TAG = "NetworkStreamHandlerTask";

	public NetworkStreamHandlerTask(String servername, Handler handler,
			int progressMsg, int shutdownMsg) {
		mServerName = servername;
		mHandler = handler;
		mProgressMessage = progressMsg;
		mShutdownMessage = shutdownMsg;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(LOG_TAG, mServerName + ": Server started");
	}

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
				Log.d(LOG_TAG, "line recieved: " + line);
				publishProgress(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(LOG_TAG, mServerName + ": Finished reading lines");
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		for (String s : values) {
			Log.d(LOG_TAG, "publishing message: " + s);
			mMessage = new Message();
			mMessage.what = mProgressMessage;
			mMessage.obj = s;
			if (mHandler != null) {
				mHandler.sendMessage(mMessage);
				Log.d(LOG_TAG, "message sent");
			}
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		mHandler.sendEmptyMessage(mShutdownMessage);
	}
}
