package jneickhoff.workleisure;

import java.util.ArrayList;
import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Task;
import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ClaimAllArrayAdapter extends ArrayAdapter<ClaimLog>{

	private class ClaimLogExt {
		public String taskName;
		public String taskType;
		public ClaimLog claimLog;
		
		public ClaimLogExt(String taskName, String taskType, ClaimLog claimLog) {
			this.taskName = taskName;
			this.taskType = taskType;
			this.claimLog = claimLog;
		}
	}
	
	public final static int MAX_LIST_SIZE = 5;
	
	private Context context;
	private List<ClaimLogExt> values;
	
	public ClaimAllArrayAdapter(Context context, List<ClaimLog> claimValues) {
		super(context, R.layout.row_claim_named, claimValues);
		this.context = context;
		DataSource ds = new DataSource(context);
		ds.open();
		List<ClaimLogExt> values = new ArrayList<ClaimLogExt>();
		for(ClaimLog claim : claimValues) {
			Task task = ds.getTask(claim.getTaskID());
			ClaimLogExt ext = new ClaimLogExt(task.getName(), task.getType(), claim);
			values.add(ext);
		}
		this.values = values;
		ds.close();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row_claim_named, parent, false);
		
		TextView txtTaskName = (TextView) rowView.findViewById(R.id.txtTaskName);
		TextView txtClaimDate = (TextView) rowView.findViewById(R.id.txtClaimDate);
		TextView txtTaskType = (TextView) rowView.findViewById(R.id.txtTaskType);
		TextView txtClaimBounty = (TextView) rowView.findViewById(R.id.txtClaimBounty);
		TextView txtClaimComment = (TextView) rowView.findViewById(R.id.txtClaimComment);
		ClaimLogExt clExt = values.get(position);
		
		txtTaskName.setText(clExt.taskName);
		String strClaimDate = DateFormat.getDateFormat(context).format(clExt.claimLog.getClaimDate());
		if(clExt.claimLog.getDueDifference() == null) {
			//nothing
		}
		else if(clExt.claimLog.getDueDifference() > 0) {
			strClaimDate += ", early " + clExt.claimLog.getDueDifference() + " day";
			if(clExt.claimLog.getDueDifference() != 1)
				strClaimDate += "s";
		}
		else if(clExt.claimLog.getDueDifference() == 0){
			strClaimDate += ", on time";
		}
		else if(clExt.claimLog.getDueDifference() < 0) {
			strClaimDate += ", late " + Math.abs(clExt.claimLog.getDueDifference()) + " day";
			if(clExt.claimLog.getDueDifference() != -1)
				strClaimDate += "s";
		}
		txtClaimDate.setText(strClaimDate);
		txtClaimBounty.setText(String.format("%.1f", clExt.claimLog.getBounty()));
		Resources r = context.getResources();
		if(clExt.taskType.equals(Task.TYPE_WORK)) {
			txtTaskType.setText(r.getString(R.string.work));
			txtTaskType.setBackgroundColor(r.getColor(R.color.blue));
			txtClaimBounty.setBackgroundColor(r.getColor(R.color.blue));
		}
		else if(clExt.taskType.equals(Task.TYPE_LEISURE)) {
			txtTaskType.setText(r.getString(R.string.leisure));
			txtTaskType.setBackgroundColor(r.getColor(R.color.red));
			txtClaimBounty.setBackgroundColor(r.getColor(R.color.red));
		}
		
		txtClaimComment.setText(clExt.claimLog.getComment());
		
		return rowView;
	}
	
	public void addTop(ClaimLog claim) {
		DataSource ds = new DataSource(context);
		ds.open();
		Task task = ds.getTask(claim.getTaskID());
		ds.close();
		ClaimLogExt ext = new ClaimLogExt(task.getName(), task.getType(), claim);
		values.add(0, ext);
		if(values.size() > MAX_LIST_SIZE)
			values.remove(values.size() - 1);
	}
	
}
