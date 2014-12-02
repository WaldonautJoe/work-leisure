package jneickhoff.workleisure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class ClaimDeleteDialogFragment extends DialogFragment {

	public interface ClaimDeleteDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);
	}
	
	public final static String EXTRA_POSITION = "extra_position";
	
	ClaimDeleteDialogListener mListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (ClaimDeleteDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() 
					+ " must implement ClaimDeleteDialogListener");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		String message = "Delete claim and refund bounty?";
		
		builder.setMessage(message)
		       .setPositiveButton(R.string.okay, new OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mListener.onDialogPositiveClick(ClaimDeleteDialogFragment.this);
					}
				})
			   .setNegativeButton(R.string.cancel, null);
		
		return builder.create();
	}
}
