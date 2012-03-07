package ch.hsr.hapdroid;

import ch.hsr.hapdroid.R.id;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HAPdroidRootActivity extends Activity {
	private TextView resultView;
	private Button captureButton;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        resultView = (TextView) findViewById(id.resultView);
        captureButton = (Button) findViewById(id.captureButton);
        
        captureButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				resultView.setText(NetworkCapture.getResultString());
			}
		});
    }
}
