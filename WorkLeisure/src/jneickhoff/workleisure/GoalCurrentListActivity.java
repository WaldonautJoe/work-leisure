package jneickhoff.workleisure;

import java.util.List;

import jneickhoff.workleisure.GoalCurrentArrayAdapter;
import jneickhoff.workleisure.db.DataSource;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class GoalCurrentListActivity extends Activity {

	GoalCurrentArrayAdapter adapter;
	TextView txtNoGoals;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goal_current_list);
		
		txtNoGoals = (TextView) findViewById(R.id.txtNoGoals);
		ListView listCurrentGoals = (ListView) findViewById(R.id.listCurrentGoals);
		
		adapter = new GoalCurrentArrayAdapter(this, getUpdatedNamedGoalList());
		
		updateNoGoalDisplay();
		
		listCurrentGoals.setAdapter(adapter);
		listCurrentGoals.setOnItemClickListener(getGoalClickListener());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		adapter.clear();
		adapter.addAll(getUpdatedNamedGoalList());
		updateNoGoalDisplay();
	}
	
	private List<NamedGoal> getUpdatedNamedGoalList(){
		DataSource dataSource = new DataSource(this);
		dataSource.open();
		List<NamedGoal> namedGoals = dataSource.getAllCurrentGoalsNamed();
		dataSource.close();
		
		return namedGoals;
	}
	
	private void updateNoGoalDisplay(){
		if(adapter.isEmpty())
			txtNoGoals.setVisibility(View.VISIBLE);
		else
			txtNoGoals.setVisibility(View.GONE);
	}
	
	/**
	 * @return click listener that starts task detail for task associated with goal clicked
	 */
	private OnItemClickListener getGoalClickListener() {
		return new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				NamedGoal namedGoal = adapter.getItem(position);
				
				Intent i = new Intent(GoalCurrentListActivity.this, TaskDetailActivity.class);
				i.putExtra(TaskDetailActivity.EXTRA_TASK_ID, namedGoal.getTaskID());
				startActivity(i);
			}
		};
	}

}
