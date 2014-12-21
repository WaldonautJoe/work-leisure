package jneickhoff.workleisure;

import java.util.List;

import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TaskListActivity extends Activity {
	
	public static final String EXTRA_BOOL_ARCHIVE = "extra_bool_archive";
	private boolean displayArchived;
	public static final String EXTRA_TASK_TYPE = "extra_task_type";
	private String strTaskType;
	public static final String EXTRA_BOOL_ITEMUNARCHIVED = "extra_bool_itemunarchived";
	private boolean itemUnarchived;
	
	private static final int REQ_NEW = 100;
	private static final int REQ_DETAIL = 200;
	private static final int REQ_ARCHIVE = 300;
	
	private DataSource dataSource;
	private Spinner spnSortType;
	private ListView taskList;
	private int selectedItemPosition;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);
		
		strTaskType = getIntent().getStringExtra(EXTRA_TASK_TYPE);
		displayArchived = getIntent().getBooleanExtra(EXTRA_BOOL_ARCHIVE, false);
		itemUnarchived = false;
		spnSortType = (Spinner) findViewById(R.id.spnSort);
		
		TextView display = (TextView) findViewById(R.id.task_type_display);
		RelativeLayout lytListHeader = (RelativeLayout) findViewById(R.id.lytListHeader);
		
		if(strTaskType.equals(Task.TYPE_WORK)) {
			display.setText(getString(R.string.work));
			lytListHeader.setBackgroundColor(getResources().getColor(R.color.blue));
		}
		else if(strTaskType.equals(Task.TYPE_LEISURE)) {
			display.setText(getString(R.string.leisure));
			lytListHeader.setBackgroundColor(getResources().getColor(R.color.red));
		}
		
		if(!displayArchived) {
			TextView txtArchive = (TextView) findViewById(R.id.txtArchive);
			txtArchive.setVisibility(View.GONE);
		}
		
		dataSource = new DataSource(this);
		dataSource.open();
		List<Task> values = dataSource.getAllTasksOfType(strTaskType, displayArchived);
		dataSource.close();
		TaskArrayAdapter adapter = new TaskArrayAdapter(this, values);
		taskList = (ListView) findViewById(R.id.taskList);
		taskList.setAdapter(adapter);
		
		taskList.setOnItemClickListener(getTaskClickListener());
		spnSortType.setOnItemSelectedListener(getSortTypeListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_list, menu);
		
		if(displayArchived) {
			MenuItem item = menu.findItem(R.id.action_view_archive);
			item.setVisible(false);
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_add:
			addNewTask();
			return true;
		case R.id.action_view_archive:
			viewArchive();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
		
	/**
	 * Called when user clicks on add new button
	 * Starts activity that creates new task
	 * @param view
	 */
	public void addNewTask() {
		Intent i = new Intent(this, EditTaskActivity.class);
		i.putExtra(EditTaskActivity.EXTRA_EDIT_TYPE, EditTaskActivity.ADD_NEW);
		i.putExtra(EditTaskActivity.EXTRA_TASK_TYPE, strTaskType);
		startActivityForResult(i, REQ_NEW);
	}
	
	public void deleteTask(final Task task) {
		final TaskArrayAdapter adapter = (TaskArrayAdapter) taskList.getAdapter();
		adapter.remove(task);
		
//		TextView txtUndoMessage = (TextView) findViewById(R.id.txtUndoMessage);
//		Button btnUndo = (Button) findViewById(R.id.btnUndo);
//		final Handler handler = new Handler();
//		
//		final Runnable runDelete = new Runnable() {
//			
//			@Override
//			public void run() {
//				dataSource.open();
//				dataSource.deleteTask(task);
//				dataSource.close();
//				lytUndoPrompt.setVisibility(View.GONE);
//			}
//		};
//		
//		adapter.remove(task);
//		txtUndoMessage.setText(task.getName() + " discarded");
//		lytUndoPrompt.setVisibility(View.VISIBLE);
//		handler.postDelayed(runDelete, 4000);
//		btnUndo.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				lytUndoPrompt.setVisibility(View.GONE);
//				handler.removeCallbacks(runDelete);
//				adapter.add(task);
//				sortList();
//				Toast.makeText(TaskListActivity.this, "Deletion undone", Toast.LENGTH_SHORT).show();
//			}
//		});
	}
	
	/**
	 * Called when user clicks on view archive button
	 * Starts activity that displays archived tasks
	 * @param view
	 */
	public void viewArchive() {
		Intent i = new Intent(this, TaskListActivity.class);
		i.putExtra(EXTRA_TASK_TYPE, strTaskType);
		i.putExtra(EXTRA_BOOL_ARCHIVE, true);
		startActivityForResult(i, REQ_ARCHIVE);
	}
	
	@Override
	public void finish() {
		if(displayArchived) {
			Intent i = new Intent();
			i.putExtra(EXTRA_BOOL_ITEMUNARCHIVED, itemUnarchived);
			setResult(RESULT_OK, i);
			
			/*
			if(itemUnarchived)
				setResult(RESULT_OK);
			else
				setResult(RESULT_CANCELED);
			*/
		}
		
		super.finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.w("Info", "onActivityResult called");
		if(resultCode == RESULT_OK) {
			
			TaskArrayAdapter adapter = (TaskArrayAdapter) taskList.getAdapter();
			
			switch(requestCode) {
			case REQ_NEW:
				long newID = data.getExtras().getLong(EditTaskActivity.UPDATE_TASK_ID);
				dataSource.open();
				Task newTask = dataSource.getTask(newID);
				dataSource.close();
				if(newTask.isArchived() == displayArchived) {
					adapter.add(newTask);
					itemUnarchived = true;
				}
				sortList();
				
				selectedItemPosition = adapter.getPosition(newTask);
				Intent intent = new Intent(getApplicationContext(), TaskDetailActivity.class);
				intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, newTask.getID());
				startActivityForResult(intent, REQ_DETAIL);
				break;
			
			case REQ_DETAIL: 
				int changeType = data.getExtras().getInt(TaskDetailActivity.EXTRA_CHANGE_TYPE);
				Task oldTask = adapter.getItem(selectedItemPosition);
				
				if(changeType == TaskDetailActivity.CHANGE_EDIT) {
					dataSource.open();
					newTask = dataSource.getTask(oldTask.getID());
					dataSource.close();
					
					if(displayArchived && !newTask.isArchived()) {
						adapter.remove(oldTask);
						itemUnarchived = true;
					}
					else if (!displayArchived && newTask.isArchived()) {
						adapter.remove(oldTask);
					}
					else {
						adapter.setTask(selectedItemPosition, newTask);
						sortList();
					}
				}
				else if(changeType == TaskDetailActivity.CHANGE_DELETE) {
					deleteTask(oldTask);
				}
			
				break;
			case REQ_ARCHIVE:
					boolean itemUnarchived = data.getBooleanExtra(EXTRA_BOOL_ITEMUNARCHIVED, false);
					if(itemUnarchived) {
						dataSource.open();
						List<Task> values = dataSource.getAllTasksOfType(strTaskType, displayArchived);
						dataSource.close();
						adapter.clear();
						adapter.addAll(values);
						sortList();
					}
				
				break;
			}
			
			adapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * Returns listener for when user selects a task<br/>
	 * Starts activity for viewing details of task
	 * @return
	 */
	private OnItemClickListener getTaskClickListener() {
		return new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Task task = (Task) taskList.getItemAtPosition(position);
				selectedItemPosition = position;
				
				Intent intent = new Intent(getApplicationContext(), TaskDetailActivity.class);
				intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getID());
				startActivityForResult(intent, REQ_DETAIL);
			}
			
		};
	}
	
	/**
	 * Returns listener for stock type spinner that changes visibility of stock amount field (editTaskStock).
	 * @return OnItemSelectedListener 
	 */
	private OnItemSelectedListener getSortTypeListener() {
		return new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
					int position, long id) {
				sortList();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// nothing
			}
			
		};
	}
	
	private void sortList(){
		TaskArrayAdapter adapter = (TaskArrayAdapter) taskList.getAdapter();
		
		int position = spnSortType.getSelectedItemPosition();
		
		switch(position){
			case 0:
				adapter.sort(Task.getComparator(Task.COMPARE_IMPORTANCE));
				break;
			case 1:
				adapter.sort(Task.getComparator(Task.COMPARE_ALPHA));
				break;
			case 2:
				adapter.sort(Task.getComparator(Task.COMPARE_ALPHA_DESC));
				break;
			case 3:
				adapter.sort(Task.getComparator(Task.COMPARE_UPDATED));
				break;
			case 4:
				adapter.sort(Task.getComparator(Task.COMPARE_UPDATED_DESC));
				break;
			case 5:
				adapter.sort(Task.getComparator(Task.COMPARE_TIMES_CLAIMED));
				break;
		}
	}

}
