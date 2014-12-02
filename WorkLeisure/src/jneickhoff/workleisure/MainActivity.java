package jneickhoff.workleisure;

import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView textBalance;
	private RelativeLayout lytBalanceWrapper;
	private MirrorBarGraph workLeisureBarGraph;
	private TextView txtDueLateHeader;
	private LinearLayout lytDueLateTasks;
	private TextView txtDueSoonHeader;
	private LinearLayout lytDueSoonTasks;
	private DataSource ds;
	
	private final static int GRAPH_DISPLAYED_DAYS = 7;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textBalance = (TextView) findViewById(R.id.textBalance);
		lytBalanceWrapper = (RelativeLayout) findViewById(R.id.lytBalanceWrapper);
		workLeisureBarGraph = (MirrorBarGraph) findViewById(R.id.workLeisureBarGraph);
		txtDueLateHeader = (TextView) findViewById(R.id.txtDueLateHeader);
		lytDueLateTasks = (LinearLayout) findViewById(R.id.lytDueLateTasks);
		txtDueSoonHeader = (TextView) findViewById(R.id.txtDueSoonHeader);
		lytDueSoonTasks = (LinearLayout) findViewById(R.id.lytDueSoonTasks);
		ds = new DataSource(this);
		updateDisplay();
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
		}
	}

	/**
	 * Updates the display of the user's balance, balance graph, and list of recent claims
	 */
	private void updateDisplay(){
		updateBalanceDisplay();
		ds.open();
		updateBalanceGraph();
		updateRecentDueTasks();
		updateRecentClaims();		
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
			if(claim.getClaimDate().after(dateLimit.getTime())){
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
	 * Updates the lists of recently due tasks <br/>
	 * ds must be open prior to calling this method.
	 */
	private void updateRecentDueTasks() {
		Calendar dateToday = Calendar.getInstance();
		dateToday.set(Calendar.HOUR_OF_DAY, 0);
		dateToday.set(Calendar.MINUTE, 0);
		dateToday.set(Calendar.SECOND, 0);
		dateToday.set(Calendar.MILLISECOND, 0);
		dateToday.add(Calendar.MILLISECOND, -1);
		
		Calendar dateSoon = Calendar.getInstance();
		dateSoon.set(Calendar.HOUR_OF_DAY, 0);
		dateSoon.set(Calendar.MINUTE, 0);
		dateSoon.set(Calendar.SECOND, 0);
		dateSoon.set(Calendar.MILLISECOND, 0);
		dateSoon.add(Calendar.DATE, 2);
		
		List<Task> lateTasksDue = ds.getRecentDueTasks(null, dateToday);
		List<Task> soonTasksDue = ds.getRecentDueTasks(dateToday, dateSoon);
		
		View.OnClickListener dueTasksClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				long taskID = (Long) v.getTag();
				
				Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
				intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskID);
				startActivity(intent);
			}
		};
		
		if(lateTasksDue.size() > 0) {
			lytDueLateTasks.removeAllViews();
			DueTasksArrayAdapter adapter = new DueTasksArrayAdapter(this, lateTasksDue);
			for(int i = 0; i < adapter.getCount(); i++) {
				View view = adapter.getView(i, null, null);
				Task task = adapter.getItem(i);
				view.setTag(task.getID());
				view.setOnClickListener(dueTasksClickListener);
				
				lytDueLateTasks.addView(view);
			}
			
			txtDueLateHeader.setVisibility(View.VISIBLE);
			lytDueLateTasks.setVisibility(View.VISIBLE);
		}
		else {
			txtDueLateHeader.setVisibility(View.GONE);
			lytDueLateTasks.setVisibility(View.GONE);
		}
		
		if(soonTasksDue.size() > 0) {
			lytDueSoonTasks.removeAllViews();
			DueTasksArrayAdapter adapter = new DueTasksArrayAdapter(this, soonTasksDue);
			for(int i = 0; i < adapter.getCount(); i++) {
				View view = adapter.getView(i, null, null);
				Task task = adapter.getItem(i);
				view.setTag(task.getID());
				view.setOnClickListener(dueTasksClickListener);
				
				lytDueSoonTasks.addView(view);
			}
				
			txtDueSoonHeader.setVisibility(View.VISIBLE);
			lytDueSoonTasks.setVisibility(View.VISIBLE);
		}
		else {
			txtDueSoonHeader.setVisibility(View.GONE);
			lytDueSoonTasks.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Updates the lists of recently claimed tasks <br/>
	 * ds must be open prior to calling this method.
	 */
	private void updateRecentClaims() {
		LinearLayout listClaims = (LinearLayout) findViewById(R.id.listClaims);
		List<ClaimLog> recentClaims = ds.getAllClaimLogs(ClaimAllArrayAdapter.MAX_LIST_SIZE, false);
		ClaimAllArrayAdapter adapter = new ClaimAllArrayAdapter(this, recentClaims);
		listClaims.removeAllViews();
		for(int i = 0; i < adapter.getCount(); i++) {
			View view = adapter.getView(i, null, null);
			long taskID = adapter.getItem(i).getTaskID();
			view.setTag(taskID);
			view.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					long taskID = (Long) v.getTag();
					
					Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
					intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskID);
					startActivity(intent);
				}
			});
			
			listClaims.addView(view);
		}
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

}
