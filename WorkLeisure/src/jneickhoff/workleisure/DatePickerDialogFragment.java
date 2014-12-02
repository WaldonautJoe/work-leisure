package jneickhoff.workleisure;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;

public class DatePickerDialogFragment extends DialogFragment {

	private OnDateSetListener mDateSetListener;
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		
		mDateSetListener = (OnDateSetListener) activity;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar cal = Calendar.getInstance();
		
		return new DatePickerDialog(getActivity(), mDateSetListener, cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
	}
}
