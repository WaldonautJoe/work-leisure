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

	//input extras
	public static final String INT_EDIT_TYPE_EXTRA = "edit_type";
	public static final int ADD_NEW = 10;
	public static final int EDIT_OLD = 20;
	private int editType;
	public static final String STRING_TASK_TYPE_EXTRA = "task_type";
	private String taskType;
	public static final String BOOLEAN_TASK_ISARCHIVED_EXTRA = "task_isarchived";
	private boolean taskIsArchived; //only used for a new task
	public static final String LONG_TASK_ID_EXTRA = "update_task_id";
	
	//output extra
	public static final String LONG_UPDATE_TASK_ID_EXTRA = "update_task_id";
	
	private LinearLayout lytHeader;
	private EditText editTaskName;
	private CheckBox chkTaskArchived;
	private Spinner spnTaskImportance;
	private CheckBox chkTaskDue;
	private TextView txtTaskDueDate;
	private EditText editTaskDesc;
	private EditText editTaskBounty;
	
	private Task oldTask;
	private Calendar taskDueDate;
	private static final String LONG_DUE_DATE_KEY = "long_due_date";
	
	private static final String DEFAULT_NAME = "Unnamed task";
	private static final String DEFAULT_IMPORTANCE = Task.IMPORTANCE_LOW;
	private static final boolean DEFAULT_IS_DUE = false;
	private static final float DEFAULT_BOUNTY = 0.0f;
	private static final String DEFAULT_STOCK_TYPE = Task.STOCK_TYPE_UNLIMITED;
	
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
		
		chkTaskDue.setOnCheckedChangeListener(getDueDateOnCheckedChangeListener());
		txtTaskDueDate.setOnClickListener(getDueDateOnClickListener());
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
		taskType = extras.getString(STRING_TASK_TYPE_EXTRA);
		editType = extras.getInt(INT_EDIT_TYPE_EXTRA);
		
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
			chkTaskArchived.setChecked(DEFAULT_IS_DUE);
			txtTaskDueDate.setText(getString(R.string.never));
			spnTaskImportance.setSelection(1); //set selection to low importance
			
			taskIsArchived = extras.getBoolean(BOOLEAN_TASK_ISARCHIVED_EXTRA);
			chkTaskArchived.setChecked(taskIsArchived);
			chkTaskArchived.setEnabled(false);
			TextView txtTaskArchived = (TextView) findViewById(R.id.txtArchived);
			txtTaskArchived.setEnabled(false);
		}
		else if(editType == EDIT_OLD){
			long id = extras.getLong(LONG_TASK_ID_EXTRA);
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
			taskDueDate.setTimeInMillis(savedInstanceState.getLong(LONG_DUE_DATE_KEY));
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
	
		savedInstanceState.putLong(LONG_DUE_DATE_KEY, taskDueDate.getTimeInMillis());
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
		Intent data = new Intent();
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
			taskName = DEFAULT_NAME;
		taskIsArchived = this.taskIsArchived;
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
		taskStockType = DEFAULT_STOCK_TYPE;
		taskStockNum = 1;
		
		
		switch(editType) {
		case ADD_NEW:
			//test if task is empty
			if(taskName.equals(DEFAULT_NAME)
					&& taskImportance.equals(DEFAULT_IMPORTANCE)
					&& taskIsDue == DEFAULT_IS_DUE
					&& taskDesc.length() == 0 
					&& taskBounty == DEFAULT_BOUNTY
					&& taskStockType.equals(DEFAULT_STOCK_TYPE)) {
				Toast.makeText(this, getString(R.string.empty_task_discarded), Toast.LENGTH_SHORT).show();
				setResult(RESULT_CANCELED, data);
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
				
				data.putExtra(LONG_UPDATE_TASK_ID_EXTRA, newTask.getID());
				setResult(RESULT_OK, data);
			}
			break;
		case EDIT_OLD:
			Task newTask = new Task(oldTask.getID(), taskName, taskType, taskDesc, taskBounty, 
					taskStockType, taskStockNum, oldTask.getTimesClaimed(), taskIsArchived, 
					taskImportance, oldTask.getDateUpdated(), taskIsDue, taskDueDate, oldTask.getCurrentGoal());
			
			if(newTask.equals(oldTask)) {
				Toast.makeText(this, getString(R.string.no_changes_made), Toast.LENGTH_SHORT).show();
				setResult(RESULT_CANCELED, data);
			}
			else {
				newTask.setDateUpdated(Calendar.getInstance());
				
				DataSource dataSource = new DataSource(this);
				dataSource.open();
				dataSource.updateTask(newTask);
				dataSource.close();
				
				Toast.makeText(this, getString(R.string.edited) + " " + newTask.getName(), 
						Toast.LENGTH_SHORT).show();
				
				setResult(RESULT_OK, data);
			}
			break;
		default:
			setResult(RESULT_CANCELED, data);
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
			Toast.makeText(this, getString(R.string.new_task_discarded), Toast.LENGTH_SHORT).show();
		else if(editType == EDIT_OLD)
			Toast.makeText(this, getString(R.string.changes_abandoned), Toast.LENGTH_SHORT).show();
		setResult(RESULT_CANCELED);
		super.finish();
	}
	
	public CompoundButton.OnCheckedChangeListener getDueDateOnCheckedChangeListener(){
		return new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					txtTaskDueDate.setText(DateFormat.getDateFormat(TaskEditActivity.this).format(taskDueDate.getTime()));
				}
				else {
					txtTaskDueDate.setText(getString(R.string.never));
				}
				
			}
		};
	}
	
	private View.OnClickListener getDueDateOnClickListener() {
		return new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DatePickerDialog dialog = new DatePickerDialog(TaskEditActivity.this, getDueDateSetListener(), 
						taskDueDate.get(Calendar.YEAR),
						taskDueDate.get(Calendar.MONTH),
						taskDueDate.get(Calendar.DAY_OF_MONTH));
				dialog.show();
			}
		};
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
