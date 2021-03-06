package jneickhoff.workleisure;

import jneickhoff.workleisure.db.Task;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ClaimConfirmDialogFragment extends DialogFragment {

	public interface ClaimConfirmDialogListener {
		public void onClaimDialogPositiveClick(DialogFragment dialog, String newBounty, String comment, boolean toRemoveDue);
	}
	
	public final static String FLOAT_BOUNTY = "float_bounty";
	private float originalBounty;
	public final static String STRING_TASKTYPE = "string_tasktype";
	private String taskType;
	public final static String BOOLEAN_ISDUE = "boolean_isdue";
	private boolean isDue;
	public final static String BOOLEAN_ISCURRENTGOAL = "boolean_iscurrentgoal";
	private boolean isCurrentGoal;
	public final static String FLOAT_CURRENTGOALPROGRESS = "float_currentgoalprogress";
	private float currentGoalProgress;
	public final static String FLOAT_CURRENTGOALTARGET = "float_currentgoaltarget";
	private float currentGoalTarget;
	
	private final static String STRING_TXTNEWBALANCE = "string_txtNewBalance";
	private final static String STRING_TXTNEWGOALPROGRESS = "string_txtnewgoalprogress";
	
	private ClaimConfirmDialogListener mListener;
	private float balance;
	
	private TextView txtNewBalance;
	private RelativeLayout lytCurGoal;
	private TextView txtNewGoalProgress;
	private EditText editBounty;
	private EditText editComment;
	private LinearLayout lytRemoveDue;
	private CheckBox chkRemoveDue;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (ClaimConfirmDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() 
					+ " must implement ClaimConfirmDialogListener");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_claim, null);
		
		TextView txtOldBalance = (TextView) view.findViewById(R.id.txtOldBalance);
		txtNewBalance = (TextView) view.findViewById(R.id.txtNewBalance);
		lytCurGoal = (RelativeLayout) view.findViewById(R.id.lytCurGoal);
		TextView txtOldGoalProgress = (TextView) view.findViewById(R.id.txtOldGoalProgress);
		txtNewGoalProgress = (TextView) view.findViewById(R.id.txtNewGoalProgress);
		TextView txtGoalTarget = (TextView) view.findViewById(R.id.txtGoalTarget);
		editBounty = (EditText) view.findViewById(R.id.editBounty);
		editComment = (EditText) view.findViewById(R.id.editComment);
		lytRemoveDue = (LinearLayout) view.findViewById(R.id.lytRemoveDue);
		chkRemoveDue = (CheckBox) view.findViewById(R.id.chkRemoveDue);
		
		SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.user_balances), Context.MODE_PRIVATE);
		balance = sharedPref.getFloat(getString(R.string.default_balance), 0f);
		
		Bundle arguments = getArguments();
		originalBounty = arguments.getFloat(FLOAT_BOUNTY);
		taskType = arguments.getString(STRING_TASKTYPE);
		isDue = arguments.getBoolean(BOOLEAN_ISDUE);
		isCurrentGoal = arguments.getBoolean(BOOLEAN_ISCURRENTGOAL);
		currentGoalProgress = arguments.getFloat(FLOAT_CURRENTGOALPROGRESS, 0f);
		currentGoalTarget = arguments.getFloat(FLOAT_CURRENTGOALTARGET, 0f);
		
		editBounty.setHint(getResources().getString(R.string.dif_bounty1) 
				+ String.format("%.1f", originalBounty) 
				+ getResources().getString(R.string.dif_bounty2));
		if(isDue)
			lytRemoveDue.setVisibility(View.VISIBLE);
		txtOldBalance.setText(String.format("%.1f", balance));
		if(isCurrentGoal) {
			txtOldGoalProgress.setText(String.format("%.1f", currentGoalProgress));
			txtGoalTarget.setText(String.format("%.1f", currentGoalTarget));
		}
		else {
			lytCurGoal.setVisibility(View.GONE);
		}
		
		updateBalanceDisplays();
		
		//update balance display upon pressing done or next or changing focus
		editBounty.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE 
						|| actionId == EditorInfo.IME_ACTION_NEXT) {
					updateBalanceDisplays();
				}
				
				return false;
			}
		});
		editBounty.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(!hasFocus)
					updateBalanceDisplays();
			}
		});
		
		builder.setView(view)
		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int id) {
						boolean toRemoveDue = false;
						if(isDue)
							toRemoveDue = chkRemoveDue.isChecked();
							
						mListener.onClaimDialogPositiveClick(ClaimConfirmDialogFragment.this, 
								editBounty.getText().toString(), 
								editComment.getText().toString(),
								toRemoveDue);
					}
				})
				.setNegativeButton(R.string.cancel, null);
		
		if(savedInstanceState != null) {
			txtNewBalance.setText(savedInstanceState.getString(STRING_TXTNEWBALANCE));
			if(isCurrentGoal)
				txtNewGoalProgress.setText(savedInstanceState.getString(STRING_TXTNEWGOALPROGRESS));
		}
		
		return builder.create();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	
		savedInstanceState.putString(STRING_TXTNEWBALANCE, txtNewBalance.getText().toString());
		if(isCurrentGoal)
			savedInstanceState.putString(STRING_TXTNEWGOALPROGRESS, txtNewGoalProgress.getText().toString());
	}
	
	private void updateBalanceDisplays(){
		String bountyText = editBounty.getText().toString();
		if(bountyText.length() == 0)
			Log.w("DisplayUpdate", "Empty bounty text");
		else
			Log.w("DisplayUpdate",bountyText);
		
		float bounty;
		if(bountyText.length() == 0)
			bounty = originalBounty;
		else if(bountyText.equals(".")) {
			bounty = 0;
			editBounty.setText("");
		}
		else {
			bounty = Float.parseFloat(bountyText);
			editBounty.setText(String.format("%.1f", bounty)); //correct format of bounty
		}
		
		if(taskType.equals(Task.TYPE_WORK))
			txtNewBalance.setText(String.format("%.1f", balance + bounty));
		else
			txtNewBalance.setText(String.format("%.1f", balance - bounty));
		
		if(isCurrentGoal)
			txtNewGoalProgress.setText(String.format("%.1f", currentGoalProgress + bounty));
	}
}
