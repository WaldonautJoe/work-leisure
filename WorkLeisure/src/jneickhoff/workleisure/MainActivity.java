package jneickhoff.workleisure;

import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView textBalance;
	private RelativeLayout lytBalanceWrapper;
	private MirrorBarGraph workLeisureBarGraph;
	private DataSource ds;
	
	private final static int GRAPH_DISPLAYED_DAYS = 7;
	public final static String LAST_DISLPAY_LATE_UPCOMING_TASKS = "last_display_late_upcoming_tasks";
	private final static long ACCEPTABLE_DISPLAY_TIME_LAPSE = 600000; //10 minutes
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textBalance = (TextView) findViewById(R.id.textBalance);
		lytBalanceWrapper = (RelativeLayout) findViewById(R.id.lytBalanceWrapper);
		workLeisureBarGraph = (MirrorBarGraph) findViewById(R.id.workLeisureBarGraph);
		ds = new DataSource(this);
		updateDisplay();
		
		SharedPreferences sharedPref = 
				this.getSharedPreferences(getString(R.string.user_balances), Context.MODE_PRIVATE);
		
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long lastDisplayLateUpcomingTasks = sharedPref.getLong(LAST_DISLPAY_LATE_UPCOMING_TASKS, currentTime - ACCEPTABLE_DISPLAY_TIME_LAPSE);
		
		if((currentTime - lastDisplayLateUpcomingTasks) >= ACCEPTABLE_DISPLAY_TIME_LAPSE) {
			displayLateUpcomingTasksDialog();
			sharedPref.edit()
					  .putLong(LAST_DISLPAY_LATE_UPCOMING_TASKS, currentTime)
					  .commit();
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		updateDisplay();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_reset_balance:
			confirmResetBalance();
			return true;
		case R.id.action_settings:
			
			return true;
		default:
			return super.onOptionsItemSelected(item);	
		}
	}
	
	/**
	 * Called when a button is clicked
	 * @param view button that was clicked
	 */
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.btnWorkList:
			displayWorkTasks();
			break;
		case R.id.btnLeisureList:
			displayLeisureTasks();
			break;
		case R.id.btnViewRecentClaims:
			displayRecentClaims();
			break;
		case R.id.btnViewLateUpcomingTasks:
			displayLateUpcomingTasks();
			break;
		case R.id.btnViewCurrentGoals:
			displayCurrentGoals();
			break;
		}
	}

	/**
	 * Updates the display of the user's balance, balance graph, and list of recent claims
	 */
	private void updateDisplay(){
		updateBalanceDisplay();
		ds.open();
		updateBalanceGraph();
		ds.close();
	}
	
	/**
	 * Updates display of user's balance
	 */
	private void updateBalanceDisplay() {
		SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.user_balances), Context.MODE_PRIVATE);
		float balance = sharedPref.getFloat(getString(R.string.default_balance), 0f);
		if(balance >= 0) {
			textBalance.setText("+ " + String.format("%.1f", balance));
			lytBalanceWrapper.setBackgroundColor(getResources().getColor(R.color.blue));
		}
		else {
			textBalance.setText("- " + String.format("%.1f", Math.abs(balance)));
			lytBalanceWrapper.setBackgroundColor(getResources().getColor(R.color.red));
		}
	}
	
	/**
	 * Updates the balance graph. <br/>
	 * ds must be open prior to calling this method.
	 */
	private void updateBalanceGraph() {
		Calendar dateGraphBoundary = Calendar.getInstance();
		dateGraphBoundary.set(Calendar.HOUR_OF_DAY, 0);
		dateGraphBoundary.set(Calendar.MINUTE, 0);
		dateGraphBoundary.set(Calendar.SECOND, 0);
		dateGraphBoundary.set(Calendar.MILLISECOND, 0);
		dateGraphBoundary.add(Calendar.DAY_OF_MONTH, -1 * GRAPH_DISPLAYED_DAYS + 1);
		List<ClaimLog> graphClaims = ds.getAllClaimLogs(dateGraphBoundary, null, null, true);
		float[] workVals = new float[GRAPH_DISPLAYED_DAYS];
		float[] leisVals = new float[GRAPH_DISPLAYED_DAYS];
		for(int i = 0; i < GRAPH_DISPLAYED_DAYS; i++){
			workVals[i] = 0;
			leisVals[i] = 0;
		}
		int claimListPos = 0;
		int groupsPos = GRAPH_DISPLAYED_DAYS - 1;
		Calendar dateLimit = Calendar.getInstance();
		dateLimit.set(Calendar.HOUR_OF_DAY, 0);
		dateLimit.set(Calendar.MINUTE, 0);
		dateLimit.set(Calendar.SECOND, 0);
		dateLimit.set(Calendar.MILLISECOND, 0);
		
		//while within boundaries
		while(claimListPos < graphClaims.size()
				&& groupsPos >= 0) {
			ClaimLog claim = graphClaims.get(claimListPos);
			
			//if within group boundary
			if(claim.getClaimDate().after(dateLimit)){
				//add bounty to appropriate counter
				if(claim.getTaskType().equals(Task.TYPE_WORK)){
					workVals[groupsPos] += claim.getBounty();
				}
				else
					leisVals[groupsPos] += claim.getBounty();
				
				//move to next item
				claimListPos++;
			}
			else {
				dateLimit.add(Calendar.DAY_OF_MONTH, -1);
				groupsPos--;
			}
		}
		
		workLeisureBarGraph.clearColumnPairs();
		Calendar dateLabel = (Calendar) dateGraphBoundary.clone();
		for(int i = 0; i < GRAPH_DISPLAYED_DAYS; i++){
			workLeisureBarGraph.addColumnPair((dateLabel.get(Calendar.MONTH) + 1) + "/" 
					+ dateLabel.get(Calendar.DAY_OF_MONTH), 
					workVals[i], leisVals[i]);
			dateLabel.add(Calendar.DAY_OF_MONTH, 1);
		}
		workLeisureBarGraph.postInvalidate();
	}
	
	/**
	 * Starts activity that displays work tasks
	 */
	public void displayWorkTasks() {
		Intent i = new Intent(this, TaskListActivity.class);
		i.putExtra(TaskListActivity.EXTRA_TASK_TYPE, Task.TYPE_WORK);
		startActivity(i);
	}

	/**
	 * Starts activity that displays leisure tasks
	 */
	public void displayLeisureTasks() {
		Intent i = new Intent(this, TaskListActivity.class);
		i.putExtra(TaskListActivity.EXTRA_TASK_TYPE, Task.TYPE_LEISURE);
		startActivity(i);
	}
	
	/**
	 * Starts activity that displays recent tasks
	 */
	public void displayRecentClaims() {
		Intent i = new Intent(this, ClaimRecentActivity.class);
		startActivity(i);
	}
	
	/**
	 * Starts activity that displays late and upcoming due tasks
	 */
	public void displayLateUpcomingTasks() {
		Intent i = new Intent(this, TasksDueActivity.class);
		startActivity(i);
	}
	
	/**
	 * Starts activity that displays currently started bounty goals
	 */
	public void displayCurrentGoals(){
		Intent i = new Intent(this, GoalCurrentListActivity.class);
		startActivity(i);
	}
	
	public void displayLateUpcomingTasksDialog() {
		DialogFragment dialog = new TasksDueDialogFragment();
		dialog.show(getFragmentManager(), "LateUpcomingDialogFragment");
	}
	
	private void confirmResetBalance() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.reset_balance_q)
			   .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						resetBalance();
					}
				})
			   .setNegativeButton(R.string.cancel, null)
			   .create()
			   .show();
	}
	
	private void resetBalance() {
		SharedPreferences sharedPref = 
				this.getSharedPreferences(getString(R.string.user_balances), Context.MODE_PRIVATE);
		
		sharedPref.edit()
				  .putFloat(getString(R.string.default_balance), 0.0f)
				  .commit();
		
		updateBalanceDisplay();
	}

}
