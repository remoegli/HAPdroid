package ch.hsr.hapdroid;

import com.stericson.RootTools.RootTools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class HAPdroidActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (RootTools.isRootAvailable()) {
			startActivity(new Intent(this, HAPdroidRootActivity.class));
		} else {
			startActivity(new Intent(this, HAPdroidNonRootActivity.class));
		}

		finish();
	}
}