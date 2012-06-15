package ch.hsr.hapdroid;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ipaulpro.afilechooser.FileChooserActivity;

/**
 * Custom file import class, which can be used to import from 
 * the SD card directory or any program that support file picking.
 * 
 * This class uses FileChooserActivity from the aFileChooser 
 * library project. The only thing it does is start a file 
 * picker which lets you choose from all the available programs
 * installed on the device and provide the choosen file back to
 * the Activity.
 * 
 * @see http://code.google.com/p/afilechooser/
 * @author Dominik Spengler
 */
public class FileImportActivity extends FileChooserActivity {

	/**
	 * The key used to store the chosen file path in the Intent
	 * used as result for the starting Activity.
	 */
	public static final String FILE_KEY = "FILENAME";
	private static final String LOG_TAG = "FileChooserActivity";

	/**
	 * @see com.ipaulpro.afilechooser.FileChooserActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            showFileChooser();
        }
	}

	/**
	 * @see com.ipaulpro.afilechooser.FileChooserActivity#onFileSelect(java.io.File)
	 */
	@Override
	protected void onFileSelect(File file) {
		setResult(RESULT_OK, getIntent().putExtra(FILE_KEY, file.getAbsolutePath()));
		finish();
	}

	/**
	 * @see com.ipaulpro.afilechooser.FileChooserActivity#onFileError(java.lang.Exception)
	 */
	@Override
    protected void onFileError(Exception e) {
            Log.e(LOG_TAG, "File select error", e);
            finish();
    }

	/**
	 * @see com.ipaulpro.afilechooser.FileChooserActivity#onFileSelectCancel()
	 */
    @Override
    protected void onFileSelectCancel() {
            Log.d(LOG_TAG, "File selections canceled");
            finish();
    }

    /**
     * @see com.ipaulpro.afilechooser.FileChooserActivity#onFileDisconnect()
     */
    @Override
    protected void onFileDisconnect() {
            Log.d(LOG_TAG, "External storage disconneted");
            finish();
    }
	
}
