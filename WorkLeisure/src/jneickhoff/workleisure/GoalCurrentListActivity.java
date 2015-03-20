package jneickhoff.workleisure;

import java.util.ArrayList;
import java.util.List;

import jneickhoff.workleisure.GoalArrayAdapter;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Goal;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class GoalCurrentListActivity extends Activity {

	GoalArrayAdapter adapter;
	TextView txtNoGoals;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_current_goal_list);
		
		txtNoGoals = (TextView) findViewById(R.id.txtNoGoals);
		ListView listCurrentGoals = (ListView) findViewById(R.id.listCurrentGoals);
		
		adapter = new GoalArrayAdapter(this, getUpdatedNamedGoalList());
		
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
	
	private List<GoalArrayAdapter.NamedGoal> getUpdatedNamedGoalList(){
		DataSource dataSource = new DataSource(this);
		dataSource.open();
		
		List<Goal> goals = dataSource.getAllCurrentGoals();
		List<GoalArrayAdapter.NamedGoal> namedGoalList = new ArrayList<GoalArrayAdapter.NamedGoal>();
				
		for(Goal goal : goals) {
			Task task = dataSource.getTask(goal.getTaskID());
			GoalArrayAdapter.NamedGoal namedGoal = 
					new GoalArrayAdapter.NamedGoal(task.getName(), task.getType(), goal);
			namedGoalList.add(namedGoal);
		}
		
		dataSource.close();
		
		return namedGoalList;
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
				GoalArrayAdapter.NamedGoal namedGoal = adapter.getItem(position);
				
				Intent i = new Intent(GoalCurrentListActivity.this, TaskDetailActivity.class);
				i.putExtra(TaskDetailActivity.EXTRA_TASK_ID, namedGoal.goal.getTaskID());
				startActivity(i);
			}
		};
	}

}
