package ch.hsr.hapdroid;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ipaulpro.afilechooser.FileChooserActivity;

public class FileImportActivity extends FileChooserActivity {

	public static final String FILE_KEY = "FILENAME";
	private static final String LOG_TAG = "FileChooserActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            showFileChooser();
        }
	}

	@Override
	protected void onFileSelect(File file) {
		setResult(RESULT_OK, getIntent().putExtra(FILE_KEY, file.getAbsolutePath()));
		finish();
	}
	
	@Override
    protected void onFileError(Exception e) {
            Log.e(LOG_TAG, "File select error", e);
            finish();
    }

    @Override
    protected void onFileSelectCancel() {
            Log.d(LOG_TAG, "File selections canceled");
            finish();
    }

    @Override
    protected void onFileDisconnect() {
            Log.d(LOG_TAG, "External storage disconneted");
            finish();
    }
	
}
