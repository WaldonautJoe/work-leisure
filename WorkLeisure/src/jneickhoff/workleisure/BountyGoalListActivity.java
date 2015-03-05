package jneickhoff.workleisure;

import java.util.Calendar;
import java.util.List;
import java.text.DateFormat;

import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Goal;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class BountyGoalListActivity extends Activity
									implements SetGoalDialogFragment.SetGoalDialogListener {

	public static final String EXTRA_TASK_ID = "extra_task_id";
	private long taskID;
	
	private DateFormat dateFormat;
	private List<Goal> goals;
	private Goal currentGoal;
	private BountyGoalHistoryArrayAdapter adapter;
	
	private LinearLayout lytHeader;
	private TextView txtName;
	private LinearLayout lytCurrentGoal;
	private HorizontalMeter metBountyCurrentGoal;
	private NotchedHorizontalMeter metTimeCurrentGoal;
	private TextView txtCurStartDate;
	private TextView txtCurEndDate;
	private Button btnSetBountyGoal;
	private LinearLayout lytEditButtons;
	
	private int selectedPosition;
	
	private final static int REQ_VIEWGOALCLAIMS_FROMLIST = 10;
	private final static int REQ_VIEWGOALCLAIMS_FROMCUR = 20;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bounty_goal_list);
		
		Task task;
		
		lytHeader = (LinearLayout) findViewById(R.id.lytHeader);
		txtName = (TextView) findViewById(R.id.txtName);
		lytCurrentGoal = (LinearLayout) findViewById(R.id.lytCurrentGoal);
		metBountyCurrentGoal = (HorizontalMeter) findViewById(R.id.metCurrentGoal);
		metTimeCurrentGoal = (NotchedHorizontalMeter) findViewById(R.id.metTimeCurrentGoal);
		txtCurStartDate = (TextView) findViewById(R.id.txtCurStartDate);
		txtCurEndDate = (TextView) findViewById(R.id.txtCurEndDate);
		btnSetBountyGoal = (Button) findViewById(R.id.btnSetBountyGoal);
		lytEditButtons = (LinearLayout) findViewById(R.id.lytEditButtons);
		
		dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
		
		taskID = getIntent().getLongExtra(EXTRA_TASK_ID, 0);
		
		DataSource ds = new DataSource(this);
		ds.open();
		task = ds.getTask(taskID);
		goals = ds.getAllGoalsForTask(taskID);
		ds.close();
		
		txtName.setText(task.getName());
		
		if(task.getType().equals(Task.TYPE_WORK)) {
			lytHeader.setBackgroundColor(getResources().getColor(R.color.blue));
			metBountyCurrentGoal.setColors(getResources().getColor(R.color.blue), 
					getResources().getColor(R.color.blue_light2));
			metTimeCurrentGoal.setColors(getResources().getColor(R.color.blue_light2), 
					getResources().getColor(R.color.blue));
		}
		else {
			lytHeader.setBackgroundColor(getResources().getColor(R.color.red));
			metBountyCurrentGoal.setColors(getResources().getColor(R.color.red), 
					getResources().getColor(R.color.red_light2));
			metTimeCurrentGoal.setColors(getResources().getColor(R.color.red_light2), 
					getResources().getColor(R.color.red));
		}
		
		if(goals.size() > 0 && goals.get(0).getDateEnd().after(Calendar.getInstance())) {
			currentGoal = goals.get(0);
			goals.remove(0);
			metBountyCurrentGoal.setValue(currentGoal.getBountyProgress(), currentGoal.getBountyTarget());
			metTimeCurrentGoal.setValue(Calendar.getInstance().getTimeInMillis(), 
					currentGoal.getDateStart().getTimeInMillis(), currentGoal.getDateEnd().getTimeInMillis());
			metTimeCurrentGoal.setNotchValues(currentGoal.getClaimDateList());
			txtCurStartDate.setText(dateFormat.format(currentGoal.getDateStart().getTime()));
			txtCurEndDate.setText(dateFormat.format(currentGoal.getDateEnd().getTime()));
			lytCurrentGoal.setVisibility(View.VISIBLE);
			btnSetBountyGoal.setVisibility(View.GONE);
			lytEditButtons.setVisibility(View.VISIBLE);
		}
		else {
			currentGoal = null;
			lytCurrentGoal.setVisibility(View.GONE);
			btnSetBountyGoal.setVisibility(View.VISIBLE);
			lytEditButtons.setVisibility(View.GONE);
		}
		
		adapter = new BountyGoalHistoryArrayAdapter(this, goals, task.getType());
		
		ListView goalList = (ListView) findViewById(R.id.listBountyGoals);
		goalList.setAdapter(adapter);
		
		goalList.setOnItemClickListener(getViewGoalClaimsClickListener());
		lytCurrentGoal.setOnClickListener(getCurrentGoalClickListener());
//		goalList.setOnItemClickListener(getDeleteGoalClickListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bounty_goal_list, menu);
		return true;
	}
	
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.btnAbandon:
			abandonCurrentGoal();
			break;
		case R.id.btnEdit:
			displayEditGoalDialog();
			break;
		case R.id.btnEndNow:
			endCurrentGoal();
			break;
		case R.id.btnSetBountyGoal:
			displaySetNewGoalDialog();
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			switch(requestCode){
			case REQ_VIEWGOALCLAIMS_FROMLIST:
				if(data.getBooleanExtra(GoalClaimListActivity.BOOLEAN_DELETE_OK_EXTRA, false)) {
					Goal goal = adapter.getItem(selectedPosition);
					adapter.remove(goal);
					adapter.notifyDataSetChanged();
				}
				break;
			case REQ_VIEWGOALCLAIMS_FROMCUR:
				if(data.getBooleanExtra(GoalClaimListActivity.BOOLEAN_DELETE_OK_EXTRA, false)) {
					currentGoal = null;
					lytCurrentGoal.setVisibility(View.GONE);
					btnSetBountyGoal.setVisibility(View.VISIBLE);
					lytEditButtons.setVisibility(View.GONE);
					setResult(RESULT_OK);
				}
				break;
			}
		}
	}

	@Override
	public void onGoalDialogPositiveClick(DialogFragment dialog,
			boolean isNewGoal, float bountyTarget, Calendar startDate, Calendar endDate) {
		Goal goal;
		DataSource ds = new DataSource(this);
		ds.open();
		if(isNewGoal) {
			goal = ds.createGoal(taskID, bountyTarget, startDate, endDate);
		}
		else {
			goal = new Goal(currentGoal.getId(), currentGoal.getTaskID(), currentGoal.getBountyProgress(), 
					bountyTarget, startDate, endDate, currentGoal.getClaimDateList()); 
			ds.updateGoal(goal);
			goal = ds.getGoal(goal.getId()); //updates claim date list
		}
		ds.close();
		
		if(goal.getDateEnd().after(Calendar.getInstance())) {
			currentGoal = goal;
			
			metBountyCurrentGoal.setValue(currentGoal.getBountyProgress(), currentGoal.getBountyTarget());
			metTimeCurrentGoal.setValue(Calendar.getInstance().getTimeInMillis(), 
					currentGoal.getDateStart().getTimeInMillis(), currentGoal.getDateEnd().getTimeInMillis());
			metTimeCurrentGoal.setNotchValues(currentGoal.getClaimDateList());
			txtCurStartDate.setText(dateFormat.format(currentGoal.getDateStart().getTime()));
			txtCurEndDate.setText(dateFormat.format(currentGoal.getDateEnd().getTime()));
			lytCurrentGoal.setVisibility(View.VISIBLE);
			btnSetBountyGoal.setVisibility(View.GONE);
			lytEditButtons.setVisibility(View.VISIBLE);
		}
		else {
			currentGoal = null;
			lytCurrentGoal.setVisibility(View.GONE);
			btnSetBountyGoal.setVisibility(View.VISIBLE);
			lytEditButtons.setVisibility(View.GONE);
			
			goals.add(goal);
			adapter.notifyDataSetChanged();
		}
		
		setResult(RESULT_OK);
	}
	
	/**
	 * Creates dialog confirming deletion, then deletes if positive
	 */
	private void abandonCurrentGoal() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.abandon_goal_q)
			   .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DataSource ds = new DataSource(BountyGoalListActivity.this);
						ds.open();
						ds.deleteGoal(currentGoal);
						ds.close();
						currentGoal = null;
						lytCurrentGoal.setVisibility(View.GONE);
						btnSetBountyGoal.setVisibility(View.VISIBLE);
						lytEditButtons.setVisibility(View.GONE);
						setResult(RESULT_OK);
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.create()
				.show();
	}
	
	private void displayEditGoalDialog() {
		Bundle bundle = new Bundle();
		bundle.putBoolean(SetGoalDialogFragment.IS_NEW_GOAL, false);
		bundle.putFloat(SetGoalDialogFragment.FLOAT_BOUNTY_TARGET, currentGoal.getBountyTarget());
		bundle.putLong(SetGoalDialogFragment.LONG_START_DATE, currentGoal.getDateStart().getTimeInMillis());
		bundle.putLong(SetGoalDialogFragment.LONG_END_DATE, currentGoal.getDateEnd().getTimeInMillis());
		
		DialogFragment dialog = new SetGoalDialogFragment();
		dialog.setArguments(bundle);
		dialog.show(getFragmentManager(), "SetGoalDialogFragment");
	}
	
	private void endCurrentGoal() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.end_now_q)
			   .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DataSource ds = new DataSource(BountyGoalListActivity.this);
						ds.open();
						currentGoal.setDateEnd(Calendar.getInstance());
						ds.updateGoal(currentGoal);
						ds.close();
						
						goals.add(currentGoal);
						adapter.notifyDataSetChanged();
						
						currentGoal = null;
						lytCurrentGoal.setVisibility(View.GONE);
						btnSetBountyGoal.setVisibility(View.VISIBLE);
						lytEditButtons.setVisibility(View.GONE);
						setResult(RESULT_OK);
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.create()
				.show();
	}
	
	private void displaySetNewGoalDialog() {
		Bundle bundle = new Bundle();
		bundle.putBoolean(SetGoalDialogFragment.IS_NEW_GOAL, true);
		
		DialogFragment dialog = new SetGoalDialogFragment();
		dialog.setArguments(bundle);
		dialog.show(getFragmentManager(), "SetGoalDialogFragment");
	}
	
	private OnItemClickListener getViewGoalClaimsClickListener() {
		return new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Goal goal = adapter.getItem(position);
				selectedPosition = position;
				
				Intent i = new Intent(BountyGoalListActivity.this, GoalClaimListActivity.class);
				i.putExtra(GoalClaimListActivity.LONG_GOAL_ID_EXTRA, goal.getId());
				startActivityForResult(i, REQ_VIEWGOALCLAIMS_FROMLIST);
			}
		};
	}
	
	private View.OnClickListener getCurrentGoalClickListener() {
		return new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(BountyGoalListActivity.this, GoalClaimListActivity.class);
				i.putExtra(GoalClaimListActivity.LONG_GOAL_ID_EXTRA, currentGoal.getId());
				startActivityForResult(i, REQ_VIEWGOALCLAIMS_FROMCUR);
			}
		};
	}
	
	/**
	 * Return click listener that displays dialog confirming deletion of selected goal and 
	 * deletes goal upon confirmation
	 * @return click listener
	 */
	private OnItemClickListener getDeleteGoalClickListener(){
		return new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectedPosition = position;
				
				getDeleteGoalConfirmDialog().show();
			}
		};
	}
	
	/**
	 * Create dialog when selecting a bounty to delete
	 * @return dialog
	 */
	private Dialog getDeleteGoalConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(BountyGoalListActivity.this);
		builder.setMessage(R.string.delete_goal_q)
			   .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteSelectedGoal();
					}
				})
				.setNegativeButton(R.string.cancel, null);
		return builder.create();
	}
	
	/**
	 * Delete goal in adapter indicated by positionToDelete
	 */
	private void deleteSelectedGoal() {
		Goal goal = adapter.getItem(selectedPosition);
		DataSource ds = new DataSource(this);
		ds.open();
		ds.deleteGoal(goal);
		ds.close();
		adapter.remove(goal);
		adapter.notifyDataSetChanged();
	}

}
