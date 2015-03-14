package jneickhoff.workleisure;

import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class ClaimListActivity extends Activity {

	public static final String EXTRA_TASK_ID = "extra_task_id";
	public static final String EXTRA_TASK_NAME = "extra_task_name";
	public static final String EXTRA_TASK_TYPE = "extra_task_type";
	
	private DataSource ds;
	private ClaimSimpleArrayAdapter adapter;
	private boolean isClaimDeleted;
	public static final String IS_CLAIM_DELETED_EXTRA = "extra_is_claim_deleted";
	
	private static final String IS_CLAIM_DELETED_KEY = "is_claim_deleted_key";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_claim_list);
		
		Bundle extras = getIntent().getExtras();
		long taskID = extras.getLong(EXTRA_TASK_ID);
		String taskName = extras.getString(EXTRA_TASK_NAME);
		String taskType = extras.getString(EXTRA_TASK_TYPE);
		
		TextView txtTaskName = (TextView) findViewById(R.id.txtTaskName);
		LinearLayout lytHeader = (LinearLayout) findViewById(R.id.lytHeader);
		ListView claimList = (ListView) findViewById(R.id.claimList);
		
		txtTaskName.setText(taskName);
		if(taskType.equals(Task.TYPE_WORK))
			lytHeader.setBackgroundColor(getResources().getColor(R.color.blue));
		else
			lytHeader.setBackgroundColor(getResources().getColor(R.color.red));
		
		ds = new DataSource(this);
		ds.open();
		
		List<ClaimLog> list = ds.getAllClaimLogs(taskID, null);
		adapter = new ClaimSimpleArrayAdapter(this, list);
		claimList.setAdapter(adapter);
		claimList.setOnItemClickListener(getClaimListener());
		
		if(savedInstanceState != null)
			isClaimDeleted = savedInstanceState.getBoolean(IS_CLAIM_DELETED_KEY);
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
		
		savedInstanceState.putBoolean(IS_CLAIM_DELETED_KEY, isClaimDeleted);
	}
	
	@Override
	public void finish() {
		if(isClaimDeleted) {
			Intent data = new Intent();
			data.putExtra(IS_CLAIM_DELETED_EXTRA, isClaimDeleted);
			setResult(RESULT_OK, data);
		}
		
		super.finish();
	}
	
	/**
	 * Listener that queries user for confirmation when a claim is tapped
	 * @return listener
	 */
	private OnItemClickListener getClaimListener() {
		return new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				confirmClaimDelete(position);
			}
		};
	}
	
	/**
	 * Display dialog to confirm deletion
	 * @param selectedPosition position of claim to delete within list
	 */
	private void confirmClaimDelete(final int selectedPosition) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.claim_delete_confirm)
			   .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteClaim(selectedPosition);
					}
				})
			   .setNegativeButton(R.string.cancel, null)
			   .create()
			   .show();
	}

	/**
	 * Delete claim from list and database
	 * @param selectedPosition position of claim to delete within list
	 */
	private void deleteClaim(int selectedPosition) {
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
