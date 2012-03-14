package ch.hsr.hapdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.stericson.RootTools.RootTools;

import ch.hsr.hapdroid.R.id;
import ch.hsr.hapdroid.R.string;
import android.app.Activity;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HAPdroidRootActivity extends Activity {
	private TextView mResultView;
	private Button mCaptureWlanBtn;
	private Button mCaptureMobileBtn;
	private NetworkCaptureTask mNetworkCaptureTask;
	
	public static final StringBuilder mResult = new StringBuilder();
	public static final int UPDATE_RESULT_VIEW = 0;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			mResultView.setText(mResult);
			
		}
	};

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mResultView = (TextView) findViewById(id.resultView);
        mResultView.setMovementMethod(new ScrollingMovementMethod());
        installBinary();
        
        RootTools.debugMode = true;
        
        mCaptureWlanBtn = (Button) findViewById(id.capturewlan_btn);
        mCaptureWlanBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), string.capture_toast, Toast.LENGTH_LONG);
				try {
					RootTools.runBinary(getApplicationContext(), "flowdump", 
							"wlan0 " + NetworkCaptureTask.SERVER_NAME);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        
        mCaptureMobileBtn = (Button) findViewById(id.capturemobile_btn);
        mCaptureMobileBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), string.capture_toast, Toast.LENGTH_LONG);
				try {
					RootTools.runBinary(getApplicationContext(), "flowdump", 
							"vsnet0 " + NetworkCaptureTask.SERVER_NAME);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        
        mNetworkCaptureTask = new NetworkCaptureTask();
        mNetworkCaptureTask.execute();
    }

	private void installBinary() {
		if (!RootTools.installBinary(getApplicationContext(), R.raw.flowdump, "flowdump"))
			Toast.makeText(getApplicationContext(), "Unable to install binary", Toast.LENGTH_LONG);
	}
	
	
	private class NetworkCaptureTask extends AsyncTask<Void, String, Void>{
		public static final String SERVER_NAME = "NetCaptureServ";
		private static final String LOG_TAG = "NetworkCaptureTask";
		private LocalServerSocket mServerSocket;
		private BufferedReader mReader;
		private InputStream mInputStream;
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				mServerSocket = new LocalServerSocket(SERVER_NAME);
				LocalSocket sk = mServerSocket.accept();
				mInputStream = sk.getInputStream();
				mReader = new BufferedReader(new InputStreamReader(mInputStream));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String line;
			Log.d(LOG_TAG, "Start reading lines");
			try {
				while ((line = mReader.readLine()) != null) {
				    publishProgress(line);
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
			for (String s : values){
				mResult.append(s);
				mResult.append('\n');
			}
			HAPdroidRootActivity.this.mHandler.sendEmptyMessage(UPDATE_RESULT_VIEW);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			try {
				mInputStream.close();
				mServerSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
