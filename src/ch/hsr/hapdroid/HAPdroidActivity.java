package ch.hsr.hapdroid;

import java.io.IOException;

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
			prepareRootLibrary();
			startActivity(new Intent(this, HAPdroidRootActivity.class));
		} else {
			RootTools.offerSuperUser(this);
		}

		finish();
	}

	private void prepareRootLibrary() {
		try {
			Runtime.getRuntime().exec("su");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}