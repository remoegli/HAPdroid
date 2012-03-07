package ch.hsr.hapdroid;

import com.stericson.RootTools.RootTools;

import ch.hsr.hapdroid.R.id;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HAPdroidNonRootActivity extends Activity {
	private Button exitButton;
	private Button getrootButton;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nonroot);
        
        exitButton = (Button) findViewById(id.exit_btn);
        getrootButton = (Button) findViewById(id.getroot_btn);
        
        getrootButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RootTools.offerSuperUser(getParent());
			}
		});
        
        exitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    }
}
