package jneickhoff.workleisure;

import java.util.List;

import jneickhoff.workleisure.ClaimDeleteDialogFragment.ClaimDeleteDialogListener;
import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ClaimListActivity extends Activity
  							   implements ClaimDeleteDialogListener{

	public static final String EXTRA_TASK_ID = "extra_task_id";
	public static final String EXTRA_TASK_NAME = "extra_task_name";
	public static final String EXTRA_TASK_TYPE = "extra_task_type";
	
	private DataSource ds;
	private ClaimSimpleArrayAdapter adapter;
	private int selectedPosition;
	private boolean isClaimDeleted;
	private static final String KEY_IS_CLAIM_DELETED = "key_is_claim_deleted";
	public static final String EXTRA_IS_CLAIM_DELETED = "extra_is_claim_deleted";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_claim_list);
		
		Bundle extras = getIntent().getExtras();
		long taskID = extras.getLong(EXTRA_TASK_ID);
		String taskName = extras.getString(EXTRA_TASK_NAME);
		String taskType = extras.getString(EXTRA_TASK_TYPE);
		
		TextView txtTaskName = (TextView) findViewById(R.id.txtTaskName);
		txtTaskName.setText(taskName);
		
		if(taskType.equals(Task.TYPE_WORK)) {
			LinearLayout lytHeader = (LinearLayout) findViewById(R.id.lytHeader);
			lytHeader.setBackgroundColor(getResources().getColor(R.color.blue));
		}
		else {
			LinearLayout lytHeader = (LinearLayout) findViewById(R.id.lytHeader);
			lytHeader.setBackgroundColor(getResources().getColor(R.color.red));
		}
		
		ds = new DataSource(this);
		ds.open();
		List<ClaimLog> list = ds.getAllClaimLogs(taskID, null);
		
		adapter = new ClaimSimpleArrayAdapter(this, list);
		ListView claimList = (ListView) findViewById(R.id.claimList);
		claimList.setAdapter(adapter);
		
		claimList.setOnItemClickListener(GetClaimListener());
		
		if(savedInstanceState != null)
			isClaimDeleted = savedInstanceState.getBoolean(KEY_IS_CLAIM_DELETED);
		else
			isClaimDeleted = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.claim_list, menu);
		return true;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		ds.close();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		ds.open();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putBoolean(KEY_IS_CLAIM_DELETED, isClaimDeleted);
	}
	
	@Override
	public void finish() {
		if(isClaimDeleted) {
			Intent data = new Intent();
			data.putExtra(EXTRA_IS_CLAIM_DELETED, isClaimDeleted);
			setResult(RESULT_OK, data);
		}
		
		super.finish();
	}
	
	/**
	 * Listener that queries user for confirmation when a claim is tapped
	 * @return listener
	 */
	private OnItemClickListener GetClaimListener() {
		return new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				DialogFragment dialog = new ClaimDeleteDialogFragment();
				selectedPosition = position;
				dialog.show(getFragmentManager(), "ClaimDeleteDialogFragment");
			}
		};
	}

	//from ClaimDeleteDialogListener
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		ClaimLog cl = adapter.getItem(selectedPosition);
		Task task = ds.getTask(cl.getTaskID());
		
		task.setTimesClaimed(task.getTimesClaimed() - 1);
		ds.updateTask(task);
		
		SharedPreferences sharedPref = 
				this.getSharedPreferences(getString(R.string.user_balances), Context.MODE_PRIVATE);
		float balance = sharedPref.getFloat(getString(R.string.default_balance), 0f);
		float bountyRefund = cl.getBounty();
		if(task.getType().equals(Task.TYPE_LEISURE))
			bountyRefund = -1 * bountyRefund;
		
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putFloat(getString(R.string.default_balance), balance - bountyRefund);
		editor.commit();
		
		ds.deleteClaimLog(cl);
		adapter.remove(cl);
		adapter.notifyDataSetChanged();
		isClaimDeleted = true;
		Toast.makeText(this, "Claim deleted", Toast.LENGTH_SHORT).show();
	}

}
