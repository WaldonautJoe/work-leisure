package jneickhoff.workleisure;

import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LateUpcomingTasksActivity extends Activity {

	private TextView txtNoDueTasks;
	private LinearLayout lytDueLateHeader;
	private LinearLayout lytDueLateTasks;
	private LinearLayout lytDueSoonHeader;
	private LinearLayout lytDueSoonTasks;
	private LinearLayout lytDueSomedayHeader;
	private LinearLayout lytDueSomedayTasks;
	private DataSource ds;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_late_upcoming_tasks);
		
		txtNoDueTasks = (TextView) findViewById(R.id.txtNoDueTasks);
		lytDueLateHeader = (LinearLayout) findViewById(R.id.lytDueLateHeader);
		lytDueLateTasks = (LinearLayout) findViewById(R.id.lytDueLateTasks);
		lytDueSoonHeader = (LinearLayout) findViewById(R.id.lytDueSoonHeader);
		lytDueSoonTasks = (LinearLayout) findViewById(R.id.lytDueSoonTasks);
		lytDueSomedayHeader = (LinearLayout) findViewById(R.id.lytDueSomedayHeader);
		lytDueSomedayTasks = (LinearLayout) findViewById(R.id.lytDueSomedayTasks);
		ds = new DataSource(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.late_upcoming_tasks, menu);
		return true;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		ds.open();
		updateRecentDueTasks();
		ds.close();
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
		List<Task> somedayTasksDue = ds.getRecentDueTasks(dateSoon, null);
		
		View.OnClickListener dueTasksClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				long taskID = (Long) v.getTag();
				
				Intent intent = new Intent(LateUpcomingTasksActivity.this, TaskDetailActivity.class);
				intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskID);
				startActivity(intent);
			}
		};
		
		if(lateTasksDue.size() == 0 && soonTasksDue.size() == 0 && somedayTasksDue.size() == 0) {
			txtNoDueTasks.setVisibility(View.VISIBLE);
			
			lytDueLateHeader.setVisibility(View.GONE);
			lytDueLateTasks.setVisibility(View.GONE);
			
			lytDueSoonHeader.setVisibility(View.GONE);
			lytDueSoonTasks.setVisibility(View.GONE);
			
			lytDueSomedayHeader.setVisibility(View.GONE);
			lytDueSomedayTasks.setVisibility(View.GONE);
		}
		else {
			txtNoDueTasks.setVisibility(View.GONE);
			
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
				
				lytDueLateHeader.setVisibility(View.VISIBLE);
				lytDueLateTasks.setVisibility(View.VISIBLE);
			}
			else {
				lytDueLateHeader.setVisibility(View.GONE);
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
					
				lytDueSoonHeader.setVisibility(View.VISIBLE);
				lytDueSoonTasks.setVisibility(View.VISIBLE);
			}
			else {
				lytDueSoonHeader.setVisibility(View.GONE);
				lytDueSoonTasks.setVisibility(View.GONE);
			}
			
			if(somedayTasksDue.size() > 0) {
				lytDueSomedayTasks.removeAllViews();
				DueTasksArrayAdapter adapter = new DueTasksArrayAdapter(this, somedayTasksDue);
				for(int i = 0; i < adapter.getCount(); i++) {
					View view = adapter.getView(i, null, null);
					Task task = adapter.getItem(i);
					view.setTag(task.getID());
					view.setOnClickListener(dueTasksClickListener);
					
					lytDueSomedayTasks.addView(view);
				}
				
				lytDueSomedayHeader.setVisibility(View.VISIBLE);
				lytDueSomedayTasks.setVisibility(View.VISIBLE);
			}
			else {
				lytDueSomedayHeader.setVisibility(View.GONE);
				lytDueSomedayTasks.setVisibility(View.GONE);
			}
		}
	}
}
