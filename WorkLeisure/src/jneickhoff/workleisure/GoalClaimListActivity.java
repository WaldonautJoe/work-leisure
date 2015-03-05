package jneickhoff.workleisure;

import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Goal;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GoalClaimListActivity extends Activity {

	public final static String EXTRA_GOAL_ID = "extra_goal_id";
	private Goal goal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goal_claim_list);
		
		long goalId = getIntent().getExtras().getLong(EXTRA_GOAL_ID);
		
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

}
