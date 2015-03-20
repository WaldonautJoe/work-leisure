package jneickhoff.workleisure;

import java.util.Calendar;
import java.util.GregorianCalendar;

import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TaskEditActivity extends Activity {

	public static final String EXTRA_EDIT_TYPE = "edit_type";
	public static final int ADD_NEW = 10;
	public static final int EDIT_OLD = 20;
	private int editType;
	
	public static final String EXTRA_TASK_TYPE = "task_type";
	private String taskType;
	
	public static final String EXTRA_TASK_ISARCHIVED = "task_isarchived";
	private boolean taskIsArchived; //only used for a new task
	
	public static final String EXTRA_TASK_ID = "update_task_id";
	private Task oldTask;
	
	public static final String UPDATE_TASK_ID = "update_task_id";
	
	private static final String UNNAMED_TASK = "Unnamed Task";
	
	private static final String LONG_DUE_DATE = "long_due_date";
	
	private LinearLayout lytHeader;
	private EditText editTaskName;
	private CheckBox chkTaskArchived;
	private Spinner spnTaskImportance;
	private CheckBox chkTaskDue;
	private TextView txtTaskDueDate;
	private EditText editTaskDesc;
	private EditText editTaskBounty;
	
	private Calendar taskDueDate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_edit);
		
		lytHeader = (LinearLayout) findViewById(R.id.lytHeader);
		editTaskName = (EditText) findViewById(R.id.editTaskName);
		chkTaskArchived = (CheckBox) findViewById(R.id.chkTaskArchived);
		spnTaskImportance = (Spinner) findViewById(R.id.spnTaskImportance);
		chkTaskDue = (CheckBox) findViewById(R.id.chkTaskDue);
		txtTaskDueDate = (TextView) findViewById(R.id.txtTaskDueDate);
		editTaskDesc = (EditText) findViewById(R.id.editTaskDesc);
		editTaskBounty = (EditText) findViewById(R.id.editTaskBounty);
		
		chkTaskDue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					txtTaskDueDate.setText(DateFormat.getDateFormat(TaskEditActivity.this).format(taskDueDate.getTime()));
				}
				else {
					txtTaskDueDate.setText(getString(R.string.never));
				}
				
			}
		});
		
		txtTaskDueDate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pickDueDate();
				
			}
		});
		editTaskBounty.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					editTaskBounty.setText("");
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(editTaskBounty, InputMethodManager.SHOW_IMPLICIT);
				}
				else {
					if(editTaskBounty.getText().length() == 0 
							|| editTaskBounty.getText().toString().equals(".")){
						if(editType == ADD_NEW)
							editTaskBounty.setText(getResources().getText(R.string.default_bounty));
						else if(editType == EDIT_OLD)
							editTaskBounty.setText(String.format("%.1f", oldTask.getBounty()));
					}
					else {
						//correct formatting of decimal input
						editTaskBounty.setText(String.format("%.1f", Float.parseFloat(editTaskBounty.getText().toString())));
					}
				}
			}
		});
		
		Bundle extras = getIntent().getExtras();
		taskType = extras.getString(EXTRA_TASK_TYPE);
		editType = extras.getInt(EXTRA_EDIT_TYPE);
		
		if(taskType.equals(Task.TYPE_WORK)) {
			lytHeader.setBackgroundColor(getResources().getColor(R.color.blue));
			editTaskName.setHintTextColor(getResources().getColor(R.color.blue_light3));
		}
		else {
			lytHeader.setBackgroundColor(getResources().getColor(R.color.red));
			editTaskName.setHintTextColor(getResources().getColor(R.color.red_light3));
		}
		
		if(editType == ADD_NEW) {
			taskDueDate = Calendar.getInstance();
			txtTaskDueDate.setText(getString(R.string.never));
			spnTaskImportance.setSelection(1); //set selection to low importance
			
			taskIsArchived = extras.getBoolean(EXTRA_TASK_ISARCHIVED);
			chkTaskArchived.setChecked(taskIsArchived);
			chkTaskArchived.setEnabled(false);
			TextView txtTaskArchived = (TextView) findViewById(R.id.txtArchived);
			txtTaskArchived.setEnabled(false);
		}
		else if(editType == EDIT_OLD){
			long id = extras.getLong(EXTRA_TASK_ID);
			DataSource dataSource = new DataSource(this);
			dataSource.open();
			oldTask = dataSource.getTask(id);
			dataSource.close();
			
			editTaskName.setText(oldTask.getName());
			if(oldTask.isArchived())
				chkTaskArchived.setChecked(true);
			if(oldTask.getImportance().equals(Task.IMPORTANCE_LOW))
				spnTaskImportance.setSelection(1);
			else if(oldTask.getImportance().equals(Task.IMPORTANCE_HIGH))
				spnTaskImportance.setSelection(0);
			
			if(oldTask.isDue()) {
				taskDueDate = Calendar.getInstance();
				taskDueDate.setTimeInMillis(oldTask.getDateDue().getTimeInMillis());
				chkTaskDue.setChecked(true); //updates due date display
			}
			else {
				taskDueDate = Calendar.getInstance();
				chkTaskDue.setChecked(false);
				txtTaskDueDate.setText(getString(R.string.never));
			}
			editTaskDesc.setText(oldTask.getDesc());
			editTaskBounty.setText(String.valueOf(oldTask.getBounty()));
		}
		
		if(savedInstanceState != null) {
			taskDueDate.setTimeInMillis(savedInstanceState.getLong(LONG_DUE_DATE));
			txtTaskDueDate.setText(DateFormat.getDateFormat(this).format(taskDueDate.getTime()));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_edit, menu);
		return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	
		savedInstanceState.putLong(LONG_DUE_DATE, taskDueDate.getTimeInMillis());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_accept:
			finish();
			return true;
		case R.id.action_cancel:
			cancel();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void finish() {
		Intent intent = new Intent();
		String taskName,
			   taskImportance,
			   taskDesc, 
			   taskStockType;
		boolean taskIsArchived,
			    taskIsDue;
		float taskBounty;
		long taskStockNum;
		
		//get info from fields
		taskName = editTaskName.getText().toString();
		if(editTaskName.length() == 0)
			taskName = UNNAMED_TASK;
		taskIsArchived = chkTaskArchived.isChecked();
		if(spnTaskImportance.getSelectedItemPosition() == 1)
			taskImportance = Task.IMPORTANCE_LOW;
		else
			taskImportance = Task.IMPORTANCE_HIGH;
		taskIsDue = chkTaskDue.isChecked();
		if(!taskIsDue)
			taskDueDate = null;
		taskDesc = editTaskDesc.getText().toString();
		if(editTaskBounty.length() == 0 || editTaskBounty.getText().toString().equals("."))
			taskBounty = 0;
		else
			taskBounty = Math.round(10 * Float.parseFloat(editTaskBounty.getText().toString())) / 10.0f; //removes digits past 10ths place
		taskStockType = Task.STOCK_TYPE_UNLIMITED;
		taskStockNum = 1;
		
		
		switch(editType) {
		case ADD_NEW:
			//test if task is empty
			if(taskName.equals(UNNAMED_TASK)
					&& taskImportance.equals(Task.IMPORTANCE_LOW)
					&& !taskIsDue
					&& taskDesc.length() == 0 
					&& taskBounty == 0
					&& taskStockType.equals(Task.STOCK_TYPE_UNLIMITED)
					&& !taskIsArchived) {
				Toast.makeText(this, "Empty task discarded", Toast.LENGTH_SHORT).show();
				setResult(RESULT_CANCELED, intent);
			}
			else {
				//add task to database
				DataSource dataSource = new DataSource(this);
				dataSource.open();
				Task newTask = dataSource.createTask(taskName, taskType, taskDesc, taskBounty, 
						taskStockType, taskStockNum, 0, taskIsArchived, taskImportance, taskIsDue, taskDueDate);
				dataSource.close();
				
				Toast.makeText(this, "Added " + newTask.getName() + " to " + taskType + " tasks", 
						Toast.LENGTH_SHORT).show();
				
				intent.putExtra(UPDATE_TASK_ID, newTask.getID());
				setResult(RESULT_OK, intent);
			}
			break;
		case EDIT_OLD:
			Task newTask = new Task(oldTask.getID(), taskName, taskType, taskDesc, taskBounty, 
					taskStockType, taskStockNum, oldTask.getTimesClaimed(), taskIsArchived, 
					taskImportance, oldTask.getDateUpdated(), taskIsDue, taskDueDate, oldTask.getCurrentGoal());
			
			if(newTask.equals(oldTask)) {
				Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show();
				setResult(RESULT_CANCELED, intent);
			}
			else {
				newTask.setDateUpdated(Calendar.getInstance());
				
				DataSource dataSource = new DataSource(this);
				dataSource.open();
				dataSource.updateTask(newTask);
				dataSource.close();
				
				Toast.makeText(this, "Edited " + newTask.getName(), 
						Toast.LENGTH_SHORT).show();
				
				setResult(RESULT_OK);
			}
			break;
		default:
			setResult(RESULT_CANCELED, intent);
			Toast.makeText(this, "Unknown task type", Toast.LENGTH_SHORT).show();
			break;
		}
		
		super.finish();
	}
	
	/**
	 * Called when user clicks the cancel button. <br/>
	 * Returns user to previous activity.
	 * @param view
	 */
	public void cancel() {
		if(editType == ADD_NEW)
			Toast.makeText(this, "New task discarded", Toast.LENGTH_SHORT).show();
		else if(editType == EDIT_OLD)
			Toast.makeText(this, "Changes abandoned", Toast.LENGTH_SHORT).show();
		setResult(RESULT_CANCELED);
		super.finish();
	}
	
	private void pickDueDate() {
		DatePickerDialog dialog = new DatePickerDialog(this, getDueDateSetListener(), 
				taskDueDate.get(Calendar.YEAR),
				taskDueDate.get(Calendar.MONTH),
				taskDueDate.get(Calendar.DAY_OF_MONTH));
		dialog.show();
	}
	
	private DatePickerDialog.OnDateSetListener getDueDateSetListener() {
		
		return new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				taskDueDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
				chkTaskDue.setChecked(true);
				txtTaskDueDate.setText(DateFormat.getDateFormat(TaskEditActivity.this).format(taskDueDate.getTime()));
			}
		};
	}

}
