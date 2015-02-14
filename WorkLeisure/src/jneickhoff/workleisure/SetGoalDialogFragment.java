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
	
	public final static String IS_NEW_GOAL = "key_is_new_goal";
	private boolean isNewGoal;
	public final static String FLOAT_BOUNTY_TARGET = "key_bounty_target";
	private float bountyTarget;
	public final static String LONG_START_DATE = "key_start_date";
	private Calendar startDate;
	public final static String LONG_END_DATE = "key_end_date";
	private Calendar endDate;
	
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
		
		isNewGoal = arguments.getBoolean(IS_NEW_GOAL);
		
		if(isNewGoal) {
			bountyTarget = 0;
			startDate = Calendar.getInstance();
			endDate = Calendar.getInstance();
			endDate.add(Calendar.DATE, 7);
		}
		else {
			bountyTarget = arguments.getFloat(FLOAT_BOUNTY_TARGET);
			startDate = Calendar.getInstance();
			startDate.setTimeInMillis(arguments.getLong(LONG_START_DATE));
			endDate = Calendar.getInstance();
			endDate.setTimeInMillis(arguments.getLong(LONG_END_DATE));
			
			editBountyTarget.setText(Float.toString(bountyTarget));
		}
		
		txtStartDate.setText(dateFormat.format(startDate.getTime()));
		txtEndDate.setText(dateFormat.format(endDate.getTime()));
		
		txtStartDate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DatePickerDialog dialog =  new DatePickerDialog(getActivity(), mStartDateListener, 
						startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH));
				dialog.show();
				
			}
		});
		txtEndDate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DatePickerDialog dialog =  new DatePickerDialog(getActivity(), mEndDateListener, 
						endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.get(Calendar.DAY_OF_MONTH));
				dialog.show();
			}
		});
		
		builder.setView(view)
		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int id) {
						
						if(editBountyTarget.getText().length() == 0)
							bountyTarget = 0;
						else
							bountyTarget = Float.valueOf(editBountyTarget.getText().toString());
						
						mListener.onGoalDialogPositiveClick(SetGoalDialogFragment.this, 
								isNewGoal, bountyTarget, startDate, endDate);
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//nothing
					}
				});
		
		return builder.create();
	}
	
	private DatePickerDialog.OnDateSetListener mStartDateListener = new DatePickerDialog.OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			startDate.clear();
			startDate.set(year, monthOfYear, dayOfMonth);
			
			txtStartDate.setText(dateFormat.format(startDate.getTime()));
		}
	};
	
	private DatePickerDialog.OnDateSetListener mEndDateListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			endDate.clear();
			endDate.set(year, monthOfYear, dayOfMonth);
			
			txtEndDate.setText(dateFormat.format(endDate.getTime()));
		}
	};
	
}
