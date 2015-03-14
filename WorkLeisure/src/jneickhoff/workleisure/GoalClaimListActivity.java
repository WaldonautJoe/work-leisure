package jneickhoff.workleisure;

import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Goal;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GoalClaimListActivity extends Activity {

	public final static String LONG_GOAL_ID_EXTRA = "long_goal_id_extra";
	public final static String BOOLEAN_DELETE_OK_EXTRA = "boolean_delete_ok_extra";
	private Goal goal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goal_claim_list);
		
		long goalId = getIntent().getExtras().getLong(LONG_GOAL_ID_EXTRA);
		
		DataSource ds = new DataSource(this);
		ds.open();
		goal = ds.getGoal(goalId);
		Task goalTask = ds.getTask(goal.getTaskID());
		List<ClaimLog> goalClaims = ds.getAllClaimLogs(goal.getTaskID(), 
				goal.getDateStart(), goal.getDateEnd(), null, false);
		ds.close();
		
		LinearLayout lytHeader = (LinearLayout) findViewById(R.id.lytHeader);
		TextView txtName = (TextView) findViewById(R.id.txtName);
		TextView txtGoalSuccess = (TextView) findViewById(R.id.txtGoalSuccess);
		HorizontalMeter metBountyProgress = (HorizontalMeter) findViewById(R.id.metBountyProgress);
		NotchedHorizontalMeter metTime = (NotchedHorizontalMeter) findViewById(R.id.metTime);
		TextView txtStartDate = (TextView) findViewById(R.id.txtStartDate);
		TextView txtEndDate = (TextView) findViewById(R.id.txtEndDate);
		ListView listClaims = (ListView) findViewById(R.id.listClaims);
		
		if(goalTask.getType().equals(Task.TYPE_WORK)) {
			lytHeader.setBackgroundColor(getResources().getColor(R.color.blue));
			metBountyProgress.setColors(getResources().getColor(R.color.blue), 
					getResources().getColor(R.color.blue_light2));
			metTime.setColors(getResources().getColor(R.color.blue_light2), 
					getResources().getColor(R.color.blue));
		}
		else {
			lytHeader.setBackgroundColor(getResources().getColor(R.color.red));
			metBountyProgress.setColors(getResources().getColor(R.color.red), 
					getResources().getColor(R.color.red_light2));
			metTime.setColors(getResources().getColor(R.color.red_light2), 
					getResources().getColor(R.color.red));
		}
		
		metBountyProgress.setValue(goal.getBountyProgress(), goal.getBountyTarget());
		metTime.setValue(Calendar.getInstance().getTimeInMillis(), 
				goal.getDateStart().getTimeInMillis(), goal.getDateEnd().getTimeInMillis());
		metTime.setNotchValues(goal.getClaimDateList());
		
		txtName.setText(goalTask.getName());
		if(goal.getBountyProgress() >= goal.getBountyTarget())
			txtGoalSuccess.setText(getResources().getString(R.string.goal_met));
		else
			txtGoalSuccess.setText(getResources().getString(R.string.goal_miss));
		txtStartDate.setText(DateFormat.getDateFormat(this).format(goal.getDateStart().getTime()));
		txtEndDate.setText(DateFormat.getDateFormat(this).format(goal.getDateEnd().getTime()));
		
		ClaimSimpleArrayAdapter adapter = new ClaimSimpleArrayAdapter(this, goalClaims);
		listClaims.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.goal_claim_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_delete:
			showDeleteGoalDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void showDeleteGoalDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.delete_goal_q)
			   .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteGoal();
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.create()
				.show();
	}
	
	/**
	 * Set flag for parent activity to remove references to goal
	 */
	private void deleteGoal() {
		DataSource ds = new DataSource(this);
		ds.open();
		ds.deleteGoal(goal);
		ds.close();
		
		Toast.makeText(this, getResources().getString(R.string.goal_discarded), Toast.LENGTH_SHORT).show();
		
		Intent i = new Intent();
		i.putExtra(BOOLEAN_DELETE_OK_EXTRA, true);
		setResult(RESULT_OK, i);
		finish();
	}

}
