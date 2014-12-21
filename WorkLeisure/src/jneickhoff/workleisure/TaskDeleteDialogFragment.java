package jneickhoff.workleisure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class TaskDeleteDialogFragment extends DialogFragment {

	public interface TaskDeleteDialogListener {
		public void onTaskDeleteDialogPositiveClick(DialogFragment dialog);
	}
	
	TaskDeleteDialogListener mListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (TaskDeleteDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() 
					+ " must implement TaskDeleteDialogListener");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setMessage(getResources().getString(R.string.task_delete_confirm))
		       .setPositiveButton(R.string.okay, new OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mListener.onTaskDeleteDialogPositiveClick(TaskDeleteDialogFragment.this);
					}
				})
			   .setNegativeButton(R.string.cancel, null);
		
		return builder.create();
	}
}
