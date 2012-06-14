package ch.hsr.hapdroid;

import ch.hsr.hapdroid.gui.Graphlet;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * The DialogHelper class is used to create the dialogs used in the 
 * {@link HAPdroidGraphletActivity} used to inform the user or get user
 * input. 
 * 
 * As the name indicates, this class is a helper in order to not bloat\
 * the activity.
 * 
 * @author "Dominik Spengler, Remo Egli"
 *
 */
public class DialogHelper {
	
	/**
	 * Creates an IP input dialog.
	 * 
	 * The dialog created will show a simple text field with OK and Cancel
	 * buttons.
	 * 
	 * @param context the {@link Context} to be used to create the dialogs
	 * @param listener the {@link android.view.View.OnClickListener} to 
	 * 		attach to the OK button
	 * @return {@link AlertDialog}
	 */
	public static AlertDialog createIPInputDialog(Context context, EditText editText, OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		editText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		builder.setMessage(R.string.input_ip).setView(editText).setCancelable(false)
				.setPositiveButton(R.string.input_ip_ok, listener)
				.setNegativeButton(R.string.input_ip_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		return builder.create();
	}

	/**
	 * Creates a simple {@link AlertDialog}.
	 * 
	 * The dialog created will inform the user with the message taken
	 * from the messageId.
	 * 
	 * @param context the {@link Context} used to create the dialog
	 * @param messageId the int identifier for the message to be shown
	 * @return {@link AlertDialog}
	 */
	public static AlertDialog createAlertDialog(Context context, int messageId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(messageId).setCancelable(true);
		return builder.create();
	}

	/**
	 * Create a simple {@link ProgressDialog}
	 * 
	 * The dialog created will inform the user about the ongoing task
	 * of creating the {@link Graphlet} out of the captured data or out
	 * of the selected file.
	 * 
	 * @param context the {@link Context} usd to create the dialog
	 * @return {@link ProgressDialog}
	 */
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
