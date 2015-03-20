package jneickhoff.workleisure;

import java.text.DateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class SetGoalDialogFragment extends DialogFragment {

	public interface SetGoalDialogListener {
		public void onGoalDialogPositiveClick(DialogFragment dialog, 
				boolean isNewGoal, float bountyTarget, Calendar startDate, Calendar endDate);
	}
	
	public final static String IS_NEW_GOAL_KEY = "key_is_new_goal";
	private boolean isNewGoal;
	public final static String FLOAT_BOUNTY_TARGET_KEY = "key_bounty_target";
	private float bountyTarget;
	public final static String LONG_START_DATE_KEY = "key_start_date";
	private Calendar startDate;
	public final static String LONG_END_DATE_KEY = "key_end_date";
	private Calendar endDate;
	
	private final static String LONG_EDITED_START_DATE_KEY = "long_edited_start_date";
	private final static String LONG_EDITED_END_DATE_KEY = "long_edited_end_date";
	
	private SetGoalDialogListener mListener;
	private DateFormat dateFormat;
	private EditText editBountyTarget;
	private TextView txtStartDate;
	private TextView txtEndDate;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (SetGoalDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() 
					+ " must implement SetGoalDialogListener");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_set_goal, null);
		
		dateFormat = android.text.format.DateFormat.getDateFormat(builder.getContext());
		
		editBountyTarget = (EditText) view.findViewById(R.id.editBountyTarget);
		txtStartDate = (TextView) view.findViewById(R.id.txtStartDate);
		txtEndDate = (TextView) view.findViewById(R.id.txtEndDate);
		
		Bundle arguments = getArguments();
		
		isNewGoal = arguments.getBoolean(IS_NEW_GOAL_KEY);
		
		if(isNewGoal) {
			bountyTarget = 0;
			startDate = Calendar.getInstance();
			endDate = Calendar.getInstance();
			endDate.set(Calendar.HOUR_OF_DAY, 0);
			endDate.set(Calendar.MINUTE, 0);
			endDate.set(Calendar.SECOND, 0);
			endDate.set(Calendar.MILLISECOND, 0);
			endDate.add(Calendar.DATE, 7);
			endDate.add(Calendar.MILLISECOND, -1);
		}
		else {
			bountyTarget = arguments.getFloat(FLOAT_BOUNTY_TARGET_KEY);
			startDate = Calendar.getInstance();
			startDate.setTimeInMillis(arguments.getLong(LONG_START_DATE_KEY));
			endDate = Calendar.getInstance();
			endDate.setTimeInMillis(arguments.getLong(LONG_END_DATE_KEY));
			
			editBountyTarget.setText(Float.toString(bountyTarget));
		}
		
		if(savedInstanceState != null) {
			startDate.setTimeInMillis(savedInstanceState.getLong(LONG_EDITED_START_DATE_KEY));
			endDate.setTimeInMillis(savedInstanceState.getLong(LONG_EDITED_END_DATE_KEY));
		}
		
		txtStartDate.setText(dateFormat.format(startDate.getTime()));
		txtEndDate.setText(dateFormat.format(endDate.getTime()));
		
		txtStartDate.setOnClickListener(getOnStartDateClickListener());
		txtEndDate.setOnClickListener(getOnEndDateClickListener());
		
		builder.setView(view)
		       .setPositiveButton(R.string.okay, getOnPositiveClickListener())
				.setNegativeButton(R.string.cancel, null);
		
		return builder.create();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	
		savedInstanceState.putLong(LONG_EDITED_START_DATE_KEY, startDate.getTimeInMillis());
		savedInstanceState.putLong(LONG_EDITED_END_DATE_KEY, endDate.getTimeInMillis());
	}
	
	/**
	 * Returns click listener that starts a date picker dialog for start date
	 */
	private View.OnClickListener getOnStartDateClickListener(){
		return new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DatePickerDialog dialog =  new DatePickerDialog(getActivity(), getStartDateSetListener(), 
						startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH));
				dialog.show();
				
			}
		};
	}
	
	/**
	 * Returns listener that updates start date when edited from dialog by user
	 */
	private DatePickerDialog.OnDateSetListener getStartDateSetListener() { 
		return new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				startDate.clear();
				startDate.set(year, monthOfYear, dayOfMonth);
				
				txtStartDate.setText(dateFormat.format(startDate.getTime()));
			}
		};
	}
	
	/**
	 * Returns a click listener that starts a date picker dialog for end date
	 */
	private View.OnClickListener getOnEndDateClickListener() {
		return new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DatePickerDialog dialog =  new DatePickerDialog(getActivity(), getEndDateSetListener(), 
						endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.get(Calendar.DAY_OF_MONTH));
				dialog.show();
			}
		};
	}
	
	/**
	 * Returns a listener that updates the end date when edited from dialog by user
	 */
	private DatePickerDialog.OnDateSetListener getEndDateSetListener() {
		return new DatePickerDialog.OnDateSetListener() {
	
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				endDate.clear();
				endDate.set(year, monthOfYear, dayOfMonth + 1);
				endDate.add(Calendar.MILLISECOND, -1);
				
				txtEndDate.setText(dateFormat.format(endDate.getTime()));
			}
		};
	}
	
	/**
	 * Returns a listener that formats and returns entered information to calling activity
	 */
	private DialogInterface.OnClickListener getOnPositiveClickListener(){
		return new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				
				String strBounty = editBountyTarget.getText().toString();
				
				if(strBounty.equals("") || strBounty.equals("."))
					bountyTarget = 0;
				else
					bountyTarget = Float.valueOf(editBountyTarget.getText().toString());
				
				mListener.onGoalDialogPositiveClick(SetGoalDialogFragment.this, 
						isNewGoal, bountyTarget, startDate, endDate);
			}
		};
	}
}
