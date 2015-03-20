package jneickhoff.workleisure;

import java.util.List;

import jneickhoff.workleisure.db.ClaimLogExt;
import jneickhoff.workleisure.db.Task;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ClaimRecentArrayAdapter extends ArrayAdapter<ClaimLogExt>{
	
	private Context context;
	private List<ClaimLogExt> values;
	
	public ClaimRecentArrayAdapter(Context context, List<ClaimLogExt> claimValues) {
		super(context, R.layout.row_claim_named, claimValues);
		this.context = context;
		this.values = claimValues;
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
		ClaimLogExt claimExt = values.get(position);
		
		txtTaskName.setText(claimExt.getTaskName());
		String strClaimDate = "";
		if(claimExt.getDueDifference() == null) {
			//nothing
		}
		else if(claimExt.getDueDifference() > 0) {
			strClaimDate += "Early " + claimExt.getDueDifference() + " day";
			if(claimExt.getDueDifference() != 1)
				strClaimDate += "s";
		}
		else if(claimExt.getDueDifference() == 0){
			strClaimDate += "On time";
		}
		else if(claimExt.getDueDifference() < 0) {
			strClaimDate += "Late " + Math.abs(claimExt.getDueDifference()) + " day";
			if(claimExt.getDueDifference() != -1)
				strClaimDate += "s";
		}
		txtClaimDate.setText(strClaimDate);
		txtClaimBounty.setText(String.format("%.1f", claimExt.getBounty()));
		Resources r = context.getResources();
		if(claimExt.getTaskType().equals(Task.TYPE_WORK)) {
			txtTaskType.setText(r.getString(R.string.work));
			txtTaskType.setBackgroundColor(r.getColor(R.color.blue));
		}
		else if(claimExt.getTaskType().equals(Task.TYPE_LEISURE)) {
			txtTaskType.setText(r.getString(R.string.leisure));
			txtTaskType.setBackgroundColor(r.getColor(R.color.red));
		}
		
		txtClaimComment.setText(claimExt.getComment());
		
		return rowView;
	}
	
	public void addTop(ClaimLogExt claim) {
		values.add(0, claim);
	}
	
}
