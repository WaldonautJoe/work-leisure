package jneickhoff.workleisure;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
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
								implements ClaimConfirmDialogFragment.ClaimConfirmDialogListener,
										   TaskDeleteDialogFragment.TaskDeleteDialogListener {

	public final static String EXTRA_TASK_ID = "extra_task_id";
	
	public static final String EXTRA_CHANGE_TYPE = "extra_change_type";
	public static final int CHANGE_EDIT = 10;
	public static final int CHANGE_DELETE = 20;
	
	private final static int REQ_EDIT = 10;
	private final static int REQ_VIEW_CLAIMS = 20;
	
	private Task task;
	private DateFormat dateFormat;
	private DataSource dataSource;
	
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
		listLatestClaims = (LinearLayout) findViewById(R.id.listLatestClaims);
		txtTimesClaimed = (TextView) findViewById(R.id.txtTimesClaimed);
		btnViewAllClaims = (Button) findViewById(R.id.btnViewAllClaims);
		
		updateDisplay();
		
		if(task.getType().equals(Task.TYPE_WORK))
			lytHeader.setBackgroundColor(getResources().getColor(R.color.blue));
		else
			lytHeader.setBackgroundColor(getResources().getColor(R.color.red));
		
		List<ClaimLog> claims = dataSource.getAllClaimLogs(task.getID(), ClaimSimpleArrayAdapter.MAX_LIST_SIZE);
		adapter = new ClaimSimpleArrayAdapter(this, claims);
		for(int i = 0; i < adapter.getCount(); i++) {
			View view = adapter.getView(i, null, null);
			listLatestClaims.addView(view);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_detail, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_delete:
			deleteTask();
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
				DataSource dataSource = new DataSource(this);
				dataSource.open();
				task = dataSource.getTask(task.getID());
				dataSource.close();
				
				updateDisplay();
				
				Intent intent = new Intent();
				intent.putExtra(EXTRA_CHANGE_TYPE, CHANGE_EDIT);
				setResult(RESULT_OK, intent);
			}
			else if(requestCode == REQ_VIEW_CLAIMS) {
				dataSource.open();
				List<ClaimLog> claims = dataSource.getAllClaimLogs(task.getID(), ClaimSimpleArrayAdapter.MAX_LIST_SIZE);
				dataSource.close();
				adapter.clear();
				adapter.addAll(claims);
				listLatestClaims.removeAllViews();
				for(int i = 0; i < adapter.getCount(); i++) {
					View view = adapter.getView(i, null, null);
					listLatestClaims.addView(view);
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
		
		Intent intent = new Intent();
		intent.putExtra(EXTRA_CHANGE_TYPE, CHANGE_EDIT);
		setResult(RESULT_OK, intent);
		
		Toast.makeText(this, task.getName() + " " + getResources().getString(R.string.claimed), Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onClaimDialogNegativeClick(DialogFragment dialog) {
		//nothing
	}
	
	@Override
	public void onTaskDeleteDialogPositiveClick(DialogFragment dialog) {
		dataSource.deleteTask(task);
		dataSource.close();
		
		Toast.makeText(this, task.getName() + " discarded", Toast.LENGTH_SHORT).show();
		
		Intent intent = new Intent();
		intent.putExtra(EXTRA_CHANGE_TYPE, CHANGE_DELETE);
		setResult(RESULT_OK, intent);
		
		super.finish();
	}

	/**
	 * Called when user clicks the delete task action. <br/>
	 * Displays deletion confirmation dialog.
	 */
	public void deleteTask() {		
		
		DialogFragment dialog = new TaskDeleteDialogFragment();
		dialog.show(getFragmentManager(), "ClaimConfirmDialogFragment");
	}
	
	/**
	 * Called when user clicks the edit action.
	 * Alters task data
	 */
	public void editTask() {
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
			
			Intent intent = new Intent();
			intent.putExtra(EXTRA_CHANGE_TYPE, CHANGE_EDIT);
			setResult(RESULT_OK, intent);
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
			
			Intent intent2 = new Intent();
			intent2.putExtra(EXTRA_CHANGE_TYPE, CHANGE_EDIT);
			setResult(RESULT_OK, intent2);
			break;
			
		case R.id.btnClaimTask:
			Bundle bundle = new Bundle();
			bundle.putFloat(ClaimConfirmDialogFragment.FLOAT_BOUNTY, task.getBounty());
			bundle.putString(ClaimConfirmDialogFragment.STRING_TASKTYPE, task.getType());
			bundle.putBoolean(ClaimConfirmDialogFragment.BOOLEAN_ISDUE, task.isDue());
			
			DialogFragment dialog = new ClaimConfirmDialogFragment();
			dialog.setArguments(bundle);
			dialog.show(getFragmentManager(), "ClaimConfirmDialogFragment");
			break;
			
		case R.id.btnViewAllClaims:
			Intent i = new Intent(this, ClaimListActivity.class);
			i.putExtra(ClaimListActivity.EXTRA_TASK_ID, task.getID());
			i.putExtra(ClaimListActivity.EXTRA_TASK_NAME, task.getName());
			i.putExtra(ClaimListActivity.EXTRA_TASK_TYPE, task.getType());
			startActivityForResult(i, REQ_VIEW_CLAIMS);
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
			
	}

}
