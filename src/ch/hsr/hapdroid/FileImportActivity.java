package ch.hsr.hapdroid;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;

import com.ipaulpro.afilechooser.FileChooserActivity;

public class FileImportActivity extends FileChooserActivity {
	
	public static final String FILE_KEY = "FILENAME";

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
}
