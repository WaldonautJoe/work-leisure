package jneickhoff.workleisure;

import java.text.DateFormat;
import java.util.List;

import jneickhoff.workleisure.db.Goal;
import jneickhoff.workleisure.db.Task;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BountyGoalHistoryArrayAdapter extends ArrayAdapter<Goal>{
	
	private Context context;
	private List<Goal> values;
	String type;
	private DateFormat dateFormat;
	
	public BountyGoalHistoryArrayAdapter(Context context, List<Goal> goalValues, String type) {
		super(context, R.layout.row_goal, goalValues);
		this.context = context;
		this.values = goalValues;
		this.type = type;
		dateFormat = android.text.format.DateFormat.getDateFormat(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row_goal, parent, false);
		
		TextView txtGoalMet = (TextView) rowView.findViewById(R.id.txtGoalName);
		HorizontalMeter metBounty = (HorizontalMeter) rowView.findViewById(R.id.metBounty);
		NotchedHorizontalMeter metTime = (NotchedHorizontalMeter) rowView.findViewById(R.id.metTime);
		TextView txtCurStartDate = (TextView) rowView.findViewById(R.id.txtCurStartDate);
		TextView txtCurEndDate = (TextView) rowView.findViewById(R.id.txtCurEndDate);
		
		Goal goal = values.get(position);
		
		if(goal.getBountyProgress() >= goal.getBountyTarget())
			txtGoalMet.setText(context.getString(R.string.goal_met));
		else
			txtGoalMet.setText(context.getString(R.string.goal_miss));
		
		if(type.equals(Task.TYPE_WORK)) {
			metBounty.setColors(context.getResources().getColor(R.color.blue), 
					context.getResources().getColor(R.color.blue_light2));
			metTime.setColors(context.getResources().getColor(R.color.blue_light2), 
					context.getResources().getColor(R.color.blue));
		}
		else {
			metBounty.setColors(context.getResources().getColor(R.color.red), 
					context.getResources().getColor(R.color.red_light2));
			metTime.setColors(context.getResources().getColor(R.color.red_light2), 
					context.getResources().getColor(R.color.red));
		}
		
		metBounty.setValue(goal.getBountyProgress(), goal.getBountyTarget());
		metTime.setValue(goal.getDateEnd().getTimeInMillis(), 
				goal.getDateStart().getTimeInMillis(), 
				goal.getDateEnd().getTimeInMillis());
		metTime.setNotchValues(goal.getClaimDateList());
		
		txtCurStartDate.setText(dateFormat.format(goal.getDateStart().getTime()));
		txtCurEndDate.setText(dateFormat.format(goal.getDateEnd().getTime()));
		
		return rowView;
	}
	
}
