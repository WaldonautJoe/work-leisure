package jneickhoff.workleisure;

import java.util.List;

import jneickhoff.workleisure.db.ClaimLog;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ClaimSimpleArrayAdapter extends ArrayAdapter<ClaimLog>{

	public final static int MAX_LIST_SIZE = 3;
	
	private Context context;
	private List<ClaimLog> values;
	
	public ClaimSimpleArrayAdapter(Context context, List<ClaimLog> values) {
		super(context, R.layout.row_claim_simple, values);
		this.context = context;
		this.values = values;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row_claim_simple, parent, false);
		
		TextView txtClaimDate = (TextView) rowView.findViewById(R.id.txtClaimDate);
		TextView txtClaimBounty = (TextView) rowView.findViewById(R.id.txtClaimBounty);
		TextView txtClaimComment = (TextView) rowView.findViewById(R.id.txtClaimComment);
		ClaimLog cl = values.get(position);
		
		String strClaimDate = DateFormat.getDateFormat(context).format(cl.getClaimDate().getTime());
		if(cl.getDueDifference() == null) {
			//nothing
		}
		else if(cl.getDueDifference() > 0) {
			strClaimDate += ", early " + cl.getDueDifference() + " day";
			if(cl.getDueDifference() != 1)
				strClaimDate += "s";
		}
		else if(cl.getDueDifference() == 0){
			strClaimDate += ", on time";
		}
		else if(cl.getDueDifference() < 0) {
			strClaimDate += ", late " + Math.abs(cl.getDueDifference()) + " day";
			if(cl.getDueDifference() != -1)
				strClaimDate += "s";
		}
		txtClaimDate.setText(strClaimDate);
		txtClaimBounty.setText(String.format("%.1f", cl.getBounty()));
		txtClaimComment.setText(cl.getComment());
		
		return rowView;
	}
	
	/**
	 * Adds claim to top of list, removing the last item if list is larger than MAX_LIST_SIZE
	 * @param claim claim to add
	 */
	public void addTop(ClaimLog claim) {
		values.add(0, claim);
		if(values.size() > MAX_LIST_SIZE)
			values.remove(values.size() - 1);
	}
}
