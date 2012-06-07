package ch.hsr.hapdroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class DialogHelper {
	
	public static AlertDialog createIPInputDialog(Context context, OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		EditText mIPEditText = new EditText(context.getApplicationContext());
		mIPEditText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		builder.setMessage(R.string.input_ip).setView(mIPEditText).setCancelable(false)
				.setPositiveButton(R.string.input_ip_ok, listener)
				.setNegativeButton(R.string.input_ip_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		return builder.create();
	}

	public static AlertDialog createWrongFileDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.wrong_file_message).setCancelable(true);
		return builder.create();
	}

	public static ProgressDialog createProgressDialog(Context context) {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setTitle(R.string.load_graphlet_message_title);
		dialog.setMessage(context.getResources()
				.getText(R.string.load_graphlet_message));
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);

		return dialog;
	}

	
}
