package jneickhoff.workleisure;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.db.Task;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GoalCurrentArrayAdapter extends ArrayAdapter<NamedGoal> {
	
	private Context context;
	private List<NamedGoal> values;
	private DateFormat dateFormat;
	
	public GoalCurrentArrayAdapter(Context context, List<NamedGoal> goalValues) {
		super(context, R.layout.row_goal, goalValues);
		this.context = context;
		this.values = goalValues;
		dateFormat = android.text.format.DateFormat.getDateFormat(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row_goal, parent, false);
		
		TextView txtGoalName = (TextView) rowView.findViewById(R.id.txtGoalName);
		HorizontalMeter metBounty = (HorizontalMeter) rowView.findViewById(R.id.metBounty);
		NotchedHorizontalMeter metTime = (NotchedHorizontalMeter) rowView.findViewById(R.id.metTime);
		TextView txtCurStartDate = (TextView) rowView.findViewById(R.id.txtCurStartDate);
		TextView txtCurEndDate = (TextView) rowView.findViewById(R.id.txtCurEndDate);
		
		NamedGoal namedGoal = values.get(position);
		
		
		txtGoalName.setText(namedGoal.getTaskName());
		
		if(namedGoal.getTaskType().equals(Task.TYPE_WORK)) {
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
		
		metBounty.setValue(namedGoal.getBountyProgress(), namedGoal.getBountyTarget());
		metTime.setValue(Calendar.getInstance().getTimeInMillis(), 
				namedGoal.getDateStart().getTimeInMillis(), 
				namedGoal.getDateEnd().getTimeInMillis());
		metTime.setNotchValues(namedGoal.getClaimDateList());
		
		txtCurStartDate.setText(dateFormat.format(namedGoal.getDateStart().getTime()));
		txtCurEndDate.setText(dateFormat.format(namedGoal.getDateEnd().getTime()));
		
		return rowView;
	}
	
}
