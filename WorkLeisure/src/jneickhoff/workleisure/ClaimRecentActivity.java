package jneickhoff.workleisure;

import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ClaimRecentActivity extends Activity {

	private final static int MAX_METER_VALUE = 8;
	private final static int DISPLAYED_DAYS = 7;
	
	private DataSource ds;
	private LinearLayout lytRecentClaims;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_claim_recent);
		
		ds = new DataSource(this);
		lytRecentClaims = (LinearLayout) findViewById(R.id.lytRecentClaims);
		
		updateDisplay();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.claim_recent, menu);
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateDisplay();
	}
	
	/**
	 * update the displayed elements of this activity
	 */
	public void updateDisplay() {
		lytRecentClaims.removeAllViews();
		
		int groupsPos = DISPLAYED_DAYS - 1;
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		Calendar targetDateBeg = Calendar.getInstance();
		targetDateBeg.set(Calendar.HOUR_OF_DAY, 0);
		targetDateBeg.set(Calendar.MINUTE, 0);
		targetDateBeg.set(Calendar.SECOND, 0);
		targetDateBeg.set(Calendar.MILLISECOND, 0);
		
		Calendar targetDateEnd = Calendar.getInstance();
		targetDateEnd.set(Calendar.HOUR_OF_DAY, 0);
		targetDateEnd.set(Calendar.MINUTE, 0);
		targetDateEnd.set(Calendar.SECOND, 0);
		targetDateEnd.set(Calendar.MILLISECOND, 0);
		targetDateEnd.add(Calendar.DAY_OF_MONTH, 1);
		targetDateEnd.add(Calendar.MILLISECOND, -1);
		
		ds.open();
		while (groupsPos >= 0) {
			//set up claim displays and count bounties totals
			View dayView = inflater.inflate(R.layout.days_claims, lytRecentClaims, false);
			List<ClaimLog> daysClaims = ds.getAllClaimLogs(targetDateBeg, targetDateEnd, null, false);
			
			TextView txtDateHeader = (TextView) dayView.findViewById(R.id.txtDateHeader);
			HorizontalMeter meterWork = (HorizontalMeter) dayView.findViewById(R.id.meterWork);
			HorizontalMeter meterLeisure = (HorizontalMeter) dayView.findViewById(R.id.meterLeisure);
			LinearLayout lytClaims = (LinearLayout) dayView.findViewById(R.id.lytClaims);
			
			float workClaimedTime = 0.0f;
			float leisClaimedTime = 0.0f;
			
			if(daysClaims.size() == 0) {
				TextView txtEmptyMsg = (TextView) dayView.findViewById(R.id.txtNoClaims);
				txtEmptyMsg.setVisibility(View.VISIBLE);
			}
			else {
				ClaimRecentArrayAdapter claimAdapter = new ClaimRecentArrayAdapter(this, daysClaims);
				for(int i = 0; i < daysClaims.size(); i++) {
					ClaimLog claim = daysClaims.get(i);
					View view = claimAdapter.getView(i, null, null);
					
					view.setOnClickListener(getClaimClickListener(claim.getTaskID()));
					lytClaims.addView(view);
					
					if(claim.getTaskType().equals(Task.TYPE_WORK))
						workClaimedTime += claim.getBounty();
					else if(claim.getTaskType().equals(Task.TYPE_LEISURE))
						leisClaimedTime += claim.getBounty();
				}
			}
			
			txtDateHeader.setText(DateFormat.getDateFormat(this).format(targetDateBeg.getTime()));
			
			meterWork.setValue(workClaimedTime, MAX_METER_VALUE);
			meterLeisure.setValue(leisClaimedTime, MAX_METER_VALUE);
			
			lytRecentClaims.addView(dayView);
			
			//go to next day
			targetDateBeg.add(Calendar.DAY_OF_MONTH, -1);
			targetDateEnd.add(Calendar.DAY_OF_MONTH, -1);
			groupsPos--;
		}
		ds.close();
	}
	
	/**
	 * Displays task detail upon click
	 * @param taskId id of task on which to display details
	 * @return click listener
	 */
	private View.OnClickListener getClaimClickListener(final long taskID) {
		return new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ClaimRecentActivity.this, TaskDetailActivity.class);
				intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskID);
				startActivity(intent);
			}
		};
	}

}
