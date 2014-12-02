package jneickhoff.workleisure;

import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ClaimRecentActivity extends Activity {

	private DataSource ds;
	private LinearLayout lytRecentClaims;
	
	private final static int DISPLAYED_DAYS = 7;
	
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
		//TODO add condition to updating display
		updateDisplay();
	}
	
	public void updateDisplay() {
		
		lytRecentClaims.removeAllViews();
		
		int groupsPos = DISPLAYED_DAYS - 1;
		Resources res = this.getResources();
		
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
			List<ClaimLog> daysClaims = ds.getAllClaimLogs(targetDateBeg, targetDateEnd, null, true);
			
			LinearLayout lytClaims = new LinearLayout(this);
			lytClaims.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			lytClaims.setOrientation(LinearLayout.VERTICAL);
			lytClaims.setPadding((int) res.getDimension(R.dimen.activity_horizontal_margin), 
								 0, 
								 (int) res.getDimension(R.dimen.activity_horizontal_margin), 
								 10);
			
			float workClaimedTime = 0.0f;
			float leisClaimedTime = 0.0f;
			
			if(daysClaims.size() == 0) {
				TextView txtEmptyMsg = new TextView(this);
				txtEmptyMsg.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				txtEmptyMsg.setText("No claims");
				lytClaims.addView(txtEmptyMsg);
			}
			else {
				ClaimRecentArrayAdapter claimAdapter = new ClaimRecentArrayAdapter(this, daysClaims);
				for(int i = 0; i < daysClaims.size(); i++) {
					ClaimLog claim = daysClaims.get(i);
					View view = claimAdapter.getView(i, null, null);
					
					view.setTag(claim.getTaskID());
					view.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							long taskID = (Long) v.getTag();
							
							Intent intent = new Intent(ClaimRecentActivity.this, TaskDetailActivity.class);
							intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskID);
							startActivity(intent);
						}
					});
					
					lytClaims.addView(view);
					
					if(claim.getTaskType().equals(Task.TYPE_WORK))
						workClaimedTime += claim.getBounty();
					else if(claim.getTaskType().equals(Task.TYPE_LEISURE))
						leisClaimedTime += claim.getBounty();
				}
			}
			
			TextView txtDateHeader = new TextView(this);
			txtDateHeader.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			txtDateHeader.setPadding((int) res.getDimension(R.dimen.activity_horizontal_margin), 
									 0, 
									 (int) res.getDimension(R.dimen.activity_horizontal_margin), 
									 0);
			txtDateHeader.setBackgroundColor(this.getResources().getColor(R.color.yellow));
			txtDateHeader.setTextColor(this.getResources().getColor(R.color.white));
			txtDateHeader.setTextSize(22);
			txtDateHeader.setText(DateFormat.getDateFormat(this).format(targetDateBeg.getTime()));
			
			//TODO create graph
			HorizontalMeter workMeter = new HorizontalMeter(this, 
					workClaimedTime, 12, getResources().getColor(R.color.blue));
			workMeter.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			HorizontalMeter leisMeter = new HorizontalMeter(this, 
					leisClaimedTime, 12, getResources().getColor(R.color.red));
			leisMeter.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
			lytClaims.addView(workMeter, 0);
			lytClaims.addView(leisMeter, 1);
			
			lytRecentClaims.addView(txtDateHeader);
			lytRecentClaims.addView(lytClaims);
			
			targetDateBeg.add(Calendar.DAY_OF_MONTH, -1);
			targetDateEnd.add(Calendar.DAY_OF_MONTH, -1);
			groupsPos--;
		}
		ds.close();
	}

}