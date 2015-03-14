package jneickhoff.workleisure;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Goal;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TaskDetailActivity extends Activity 
								implements ClaimConfirmDialogFragment.ClaimConfirmDialogListener {

	public static final String EXTRA_TASK_ID = "extra_task_id";
	
	public static final String EXTRA_CHANGE_TYPE = "extra_change_type";
	public static final int CHANGE_EDIT = 10;
	public static final int CHANGE_DELETE = 20;
	
	private static final int REQ_EDIT = 10;
	private static final int REQ_VIEW_CLAIMS = 20;
	private static final int REQ_CUR_GOAL = 25;
	private static final int REQ_VIEW_GOALS = 30;
	
	private Task task;
	private DateFormat dateFormat;
	private DataSource dataSource;
	private boolean isTaskEdited;
	private static final String KEY_IS_TASK_EDITED = "is_task_edited";
	
	private TextView txtName;
	private TextView txtDateUpdated;
	private LinearLayout lytDue;
	private TextView txtDateDue;
	private TextView txtArchived;
	private TextView txtImportance;
	private TextView txtDesc;
	private TextView txtBounty;
	private TextView txtStock;
	private LinearLayout lytDueButtons;
	private Button btnClaimTask;
	private LinearLayout lytCurrentGoal;
	private HorizontalMeter metBountyCurrentGoal;
	private NotchedHorizontalMeter metTimeCurrentGoal;
	private TextView txtCurStartDate;
	private TextView txtCurEndDate;
	private LinearLayout listLatestClaims;
	private TextView txtTimesClaimed;
	private Button btnViewAllClaims;
	private ClaimSimpleArrayAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_detail);
		
		long id = getIntent().getExtras().getLong(EXTRA_TASK_ID);
		
		dataSource = new DataSource(this);
		dataSource.open();
		task = dataSource.getTask(id);
		
		dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
		
		LinearLayout lytHeader = (LinearLayout) findViewById(R.id.lytHeader);
		txtName = (TextView) findViewById(R.id.txtName);
		txtDateUpdated = (TextView) findViewById(R.id.txtDateUpdated);
		lytDue = (LinearLayout) findViewById(R.id.lytDue);
		txtDateDue = (TextView) findViewById(R.id.txtDateDue);
		txtArchived = (TextView) findViewById(R.id.txtArchived);
		txtImportance = (TextView) findViewById(R.id.txtImportance);
		txtDesc = (TextView) findViewById(R.id.txtDesc);
		txtBounty = (TextView) findViewById(R.id.txtBounty);
		txtStock = (TextView) findViewById(R.id.txtStock);
		lytDueButtons = (LinearLayout) findViewById(R.id.lytDueButtons);
		btnClaimTask = (Button) findViewById(R.id.btnClaimTask);
		lytCurrentGoal = (LinearLayout) findViewById(R.id.lytCurrentGoal);
		metBountyCurrentGoal = (HorizontalMeter) findViewById(R.id.metCurrentGoal);
		metTimeCurrentGoal = (NotchedHorizontalMeter) findViewById(R.id.metTimeCurrentGoal);
		txtCurStartDate = (TextView) findViewById(R.id.txtCurStartDate);
		txtCurEndDate = (TextView) findViewById(R.id.txtCurEndDate);
		listLatestClaims = (LinearLayout) findViewById(R.id.listLatestClaims);
		txtTimesClaimed = (TextView) findViewById(R.id.txtTimesClaimed);
		btnViewAllClaims = (Button) findViewById(R.id.btnViewAllClaims);
		
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
		
		updateDisplay();
		
		List<ClaimLog> claims = dataSource.getAllClaimLogs(task.getID(), ClaimSimpleArrayAdapter.MAX_LIST_SIZE);
		adapter = new ClaimSimpleArrayAdapter(this, claims);
		for(int i = 0; i < adapter.getCount(); i++) {
			View view = adapter.getView(i, null, null);
			listLatestClaims.addView(view);
		}
		
		if(savedInstanceState != null) {
			isTaskEdited = savedInstanceState.getBoolean(KEY_IS_TASK_EDITED);
		}
		else
		{
			isTaskEdited = false;
		}
	}
	
	@Override
	public void onResume() {
		dataSource.open();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		dataSource.close();
		super.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putBoolean(KEY_IS_TASK_EDITED, isTaskEdited);
	}
	
	@Override
	public void finish(){
		if(isTaskEdited) {
			Intent intent = new Intent();
			intent.putExtra(EXTRA_CHANGE_TYPE, CHANGE_EDIT);
			setResult(RESULT_OK, intent);
		}
		
		super.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_detail, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_delete:
			confirmDeleteTask();
			return true;
		case R.id.action_edit:
			editTask();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			if(requestCode == REQ_EDIT) {
				dataSource.open();
				task = dataSource.getTask(task.getID());
				
				updateDisplay();
				dataSource.close();
				
				isTaskEdited = true;
			}
			else if(requestCode == REQ_VIEW_CLAIMS) {
				boolean isClaimDeleted = data.getBooleanExtra(ClaimListActivity.EXTRA_IS_CLAIM_DELETED, false);
				
				if(isClaimDeleted) {
					dataSource.open();
					List<ClaimLog> claims = dataSource.getAllClaimLogs(task.getID(), ClaimSimpleArrayAdapter.MAX_LIST_SIZE);
					task = dataSource.getTask(task.getID()); //update times claimed
					updateCurrentGoal();
					dataSource.close();
					
					adapter.clear();
					adapter.addAll(claims);
					listLatestClaims.removeAllViews();
					for(int i = 0; i < adapter.getCount(); i++) {
						View view = adapter.getView(i, null, null);
						listLatestClaims.addView(view);
					}
					
					txtTimesClaimed.setText(task.getTimesClaimed() + " " + getResources().getString(R.string.total_claims));
					if(task.getTimesClaimed() == 0) {
						btnViewAllClaims.setVisibility(View.GONE);
					}
				}
			}
			else if(requestCode == REQ_CUR_GOAL) {
				boolean isCurrentGoalDeleted = data.getBooleanExtra(GoalClaimListActivity.BOOLEAN_DELETE_OK_EXTRA, false);
				if(isCurrentGoalDeleted) {
					dataSource.open();
					updateCurrentGoal();
					dataSource.close();
				}
			}
			else if(requestCode == REQ_VIEW_GOALS) {
				boolean isCurrentGoalUpdated = data.getBooleanExtra(BountyGoalListActivity.IS_CURRENT_GOAL_UPDATED_EXTRA, false);
				if(isCurrentGoalUpdated) {
					dataSource.open();
					updateCurrentGoal();
					dataSource.close();
					isTaskEdited = true;
				}
			}
		}
	}
	
	@Override
	public void onClaimDialogPositiveClick(DialogFragment dialog, String newBounty, String comment, boolean toRemoveDue) {
		if(task.getStockType().equals(Task.STOCK_TYPE_LIMITED)){
			long taskStock = task.getStockNumber();
			taskStock--;
			task.setStockNumber(taskStock);
			
			if(taskStock <= 0)
				btnClaimTask.setEnabled(false);
			
			txtStock.setText(String.valueOf(taskStock));
		}
		Date currentTime = Calendar.getInstance().getTime();
		
		float bounty;
		if(newBounty.length() == 0)
			bounty = task.getBounty();
		else if(newBounty.equals("."))
			bounty = 0;
		else
			bounty = Float.parseFloat(newBounty);
		
		SharedPreferences sharedPref = 
				this.getSharedPreferences(getString(R.string.user_balances), Context.MODE_PRIVATE);
		float balance = sharedPref.getFloat(getString(R.string.default_balance), 0f);
		if(task.getType().equals(Task.TYPE_LEISURE))
			balance -= bounty;
		else
			balance += bounty;
		
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putFloat(getString(R.string.default_balance), balance);
		editor.commit();
		
		Long dueDifference;
		if(task.isDue()) {
			Calendar currentDate = Calendar.getInstance();
			currentDate.set(Calendar.HOUR_OF_DAY, 0);
			currentDate.set(Calendar.MINUTE, 0);
			currentDate.set(Calendar.SECOND, 0);
			currentDate.set(Calendar.MILLISECOND, 0);
			dueDifference = task.getDateDue().getTime() - currentDate.getTimeInMillis();
			dueDifference = dueDifference / (1000*60*60*24);
		}
		else
			dueDifference = null;
				
		ClaimLog cl = dataSource.createClaimLog(currentTime, task.getID(), task.getType(), comment, 
				bounty, balance, task.getBounty() - bounty, dueDifference);
		
		task.setTimesClaimed(1 + task.getTimesClaimed());
		task.setDateUpdated(currentTime);
		if(toRemoveDue) {
			task.setDue(false);
			lytDue.setVisibility(View.GONE);
			lytDueButtons.setVisibility(View.VISIBLE);
		}
		dataSource.updateTask(task);
		
		txtTimesClaimed.setText(String.valueOf(task.getTimesClaimed() + " " + getResources().getString(R.string.total_claims)));
		if(task.getTimesClaimed() == 1)
			btnViewAllClaims.setVisibility(View.VISIBLE);
		
		adapter.addTop(cl);
		adapter.notifyDataSetChanged();
		listLatestClaims.addView(adapter.getView(0, null, null), 0);
		if(listLatestClaims.getChildCount() > ClaimSimpleArrayAdapter.MAX_LIST_SIZE)
			listLatestClaims.removeViewAt(ClaimSimpleArrayAdapter.MAX_LIST_SIZE);
		
		updateCurrentGoal();
		
		isTaskEdited = true;
		
		Toast.makeText(this, task.getName() + " " + getResources().getString(R.string.claimed), Toast.LENGTH_SHORT).show();
	}

	/**
	 * Called when user clicks the delete task action. <br/>
	 * Displays deletion confirmation dialog.
	 */
	private void confirmDeleteTask() {		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.task_delete_confirm)
			   .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteTask();
					}
				})
			   .setNegativeButton(R.string.cancel, null)
			   .create()
			   .show();
	}
	
	private void deleteTask() {
		dataSource.deleteTask(task);
		dataSource.close();
		
		Toast.makeText(this, task.getName() + " " + getString(R.string.discarded), Toast.LENGTH_SHORT).show();
		
		Intent intent = new Intent();
		intent.putExtra(EXTRA_CHANGE_TYPE, CHANGE_DELETE);
		setResult(RESULT_OK, intent);
		
		super.finish();
	}
	
	/**
	 * Called when user clicks the edit action.
	 * Alters task data
	 */
	private void editTask() {
		Intent intent = new Intent(this, EditTaskActivity.class);
		intent.putExtra(EditTaskActivity.EXTRA_EDIT_TYPE, EditTaskActivity.EDIT_OLD);
		intent.putExtra(EditTaskActivity.EXTRA_TASK_TYPE, task.getType());
		intent.putExtra(EditTaskActivity.EXTRA_TASK_ID, task.getID());
		startActivityForResult(intent, REQ_EDIT);
	}
	
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.btnDueToday:
			Log.w("Info", "btnDueToday clicked");
			Calendar dateToday = Calendar.getInstance();
			dateToday.set(Calendar.HOUR_OF_DAY, 0);
			dateToday.set(Calendar.MINUTE, 0);
			dateToday.set(Calendar.SECOND, 0);
			dateToday.set(Calendar.MILLISECOND, 0);
			task.setDue(true);;
			task.setDateDue(dateToday.getTime());
			dataSource.updateTask(task);
			updateDisplay();
			
			isTaskEdited = true;
			break;
			
		case R.id.btnDueTomorrow:
			Calendar dateTomorrow = Calendar.getInstance();
			dateTomorrow.set(Calendar.HOUR_OF_DAY, 0);
			dateTomorrow.set(Calendar.MINUTE, 0);
			dateTomorrow.set(Calendar.SECOND, 0);
			dateTomorrow.set(Calendar.MILLISECOND, 0);
			dateTomorrow.add(Calendar.DATE, 1);
			task.setDue(true);
			task.setDateDue(dateTomorrow.getTime());
			dataSource.updateTask(task);
			updateDisplay();
			
			isTaskEdited = true;
			break;
			
		case R.id.btnClaimTask:
			Bundle bundle = new Bundle();
			bundle.putFloat(ClaimConfirmDialogFragment.FLOAT_BOUNTY, task.getBounty());
			bundle.putString(ClaimConfirmDialogFragment.STRING_TASKTYPE, task.getType());
			bundle.putBoolean(ClaimConfirmDialogFragment.BOOLEAN_ISDUE, task.isDue());
			if(task.getCurrentGoal() == null)
				bundle.putBoolean(ClaimConfirmDialogFragment.BOOLEAN_ISCURRENTGOAL, false);
			else
			{
				bundle.putBoolean(ClaimConfirmDialogFragment.BOOLEAN_ISCURRENTGOAL, true);
				bundle.putFloat(ClaimConfirmDialogFragment.FLOAT_CURRENTGOALPROGRESS, task.getCurrentGoal().getBountyProgress());
				bundle.putFloat(ClaimConfirmDialogFragment.FLOAT_CURRENTGOALTARGET, task.getCurrentGoal().getBountyTarget());
			}
			
			DialogFragment dialog = new ClaimConfirmDialogFragment();
			dialog.setArguments(bundle);
			dialog.show(getFragmentManager(), "ClaimConfirmDialogFragment");
			break;
			
		case R.id.lytCurrentGoal:
			Intent intentCurrentGoal = new Intent(this, GoalClaimListActivity.class);
			intentCurrentGoal.putExtra(GoalClaimListActivity.LONG_GOAL_ID_EXTRA, task.getCurrentGoal().getId());
			startActivityForResult(intentCurrentGoal, REQ_CUR_GOAL);
			break;
			
		case R.id.btnViewBountyGoals:
			Intent intentGoals = new Intent(this, BountyGoalListActivity.class);
			intentGoals.putExtra(BountyGoalListActivity.LONG_TASK_ID_EXTRA, task.getID());
			startActivityForResult(intentGoals, REQ_VIEW_GOALS);
			break;
			
		case R.id.btnViewAllClaims:
			Intent intentViewClaims = new Intent(this, ClaimListActivity.class);
			intentViewClaims.putExtra(ClaimListActivity.EXTRA_TASK_ID, task.getID());
			intentViewClaims.putExtra(ClaimListActivity.EXTRA_TASK_NAME, task.getName());
			intentViewClaims.putExtra(ClaimListActivity.EXTRA_TASK_TYPE, task.getType());
			startActivityForResult(intentViewClaims, REQ_VIEW_CLAIMS);
			break;
		}
	}
	
	private void updateDisplay() {
		txtName.setText(task.getName());
		txtDateUpdated.setText(dateFormat.format(task.getDateUpdated()));
		if(task.isDue()) {
			txtDateDue.setText(dateFormat.format(task.getDateDue()));
			Calendar dateDue = Calendar.getInstance();
			dateDue.setTime(task.getDateDue());
			dateDue.add(Calendar.DATE, 1);
			Calendar dateBoundary = Calendar.getInstance();
			dateBoundary.setTime(task.getDateDue());
			dateBoundary.add(Calendar.DAY_OF_MONTH, -1);
			if(Calendar.getInstance().after(dateDue)) {
				txtDateDue.setTextColor(getResources().getColor(R.color.red));
			}
			else if(Calendar.getInstance().after(dateBoundary)) {
				txtDateDue.setTextColor(getResources().getColor(R.color.yellow));
			}
			else {
				txtDateDue.setTextColor(getResources().getColor(R.color.blue));
			}
			lytDue.setVisibility(View.VISIBLE);
			lytDueButtons.setVisibility(View.GONE);
		}
		else {
			lytDue.setVisibility(View.GONE);
			lytDueButtons.setVisibility(View.VISIBLE);
		}
		if(task.isArchived())
			txtArchived.setVisibility(View.VISIBLE);
		else
			txtArchived.setVisibility(View.GONE);
		if(task.getImportance().equals(Task.IMPORTANCE_HIGH))
			txtImportance.setText(getResources().getText(R.string.high_importance));
		else if(task.getImportance().equals(Task.IMPORTANCE_LOW))
			txtImportance.setText(getResources().getText(R.string.low_importance));
		if(task.getDesc().length() == 0)
			txtDesc.setVisibility(View.GONE);
		else {
			txtDesc.setVisibility(View.VISIBLE);
			txtDesc.setText(task.getDesc());
		}
		txtBounty.setText(String.format("%.1f", task.getBounty()));
		if(task.getStockType().equals(Task.STOCK_TYPE_UNLIMITED)){
			txtStock.setText(getResources().getString(R.string.unlimited));
			btnClaimTask.setEnabled(true);
		}
		else {
			txtStock.setText(String.valueOf(task.getStockNumber()));
			if(task.getStockNumber() <= 0)
				btnClaimTask.setEnabled(false);
			else
				btnClaimTask.setEnabled(true);
		}
		txtTimesClaimed.setText(String.valueOf(task.getTimesClaimed() + " " + getResources().getString(R.string.total_claims)));
		if(task.getTimesClaimed() == 0) {
			btnViewAllClaims.setVisibility(View.GONE);
		}
		else {
			btnViewAllClaims.setVisibility(View.VISIBLE);
		}
		
		updateCurrentGoal();
	}
	
	/**
	 * updates the current goal and its display <br/>
	 * dataSource must be open
	 */
	private void updateCurrentGoal() {
		Goal currentGoal = dataSource.getCurrentGoalForTask(task.getID());
		task.setCurrentGoal(currentGoal);
		
		if(task.getCurrentGoal() == null) {
			lytCurrentGoal.setVisibility(View.GONE);
		}
		else {
			metBountyCurrentGoal.setValue(currentGoal.getBountyProgress(), currentGoal.getBountyTarget());
			metTimeCurrentGoal.setValue(Calendar.getInstance().getTimeInMillis(), 
					currentGoal.getDateStart().getTimeInMillis(), 
					currentGoal.getDateEnd().getTimeInMillis());
			metTimeCurrentGoal.setNotchValues(currentGoal.getClaimDateList());
			txtCurStartDate.setText(dateFormat.format(currentGoal.getDateStart().getTime()));
			txtCurEndDate.setText(dateFormat.format(currentGoal.getDateEnd().getTime()));
			lytCurrentGoal.setVisibility(View.VISIBLE);
		}
	}

}
