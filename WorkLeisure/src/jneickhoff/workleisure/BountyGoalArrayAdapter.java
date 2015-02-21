package jneickhoff.workleisure;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.BountyGoalArrayAdapter.NamedGoal;
import jneickhoff.workleisure.db.Goal;
import jneickhoff.workleisure.db.Task;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BountyGoalArrayAdapter extends ArrayAdapter<NamedGoal> {
	
	public static class NamedGoal {
		public String name;
		public String type;
		public Goal goal;
		
		public NamedGoal(String name, String type, Goal goal) {
			this.name = name;
			this.type = type;
			this.goal = goal;
		}
	}
	
	private Context context;
	private List<NamedGoal> values;
	private DateFormat dateFormat;
	
	public BountyGoalArrayAdapter(Context context, List<NamedGoal> goalValues) {
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
		
		NamedGoal cGoal = values.get(position);
		
		
		txtGoalName.setText(cGoal.name);
		
		if(cGoal.type.equals(Task.TYPE_WORK)) {
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
		
		metBounty.setValue(cGoal.goal.getBountyProgress(), cGoal.goal.getBountyTarget());
		metTime.setValue(Calendar.getInstance().getTimeInMillis(), 
				cGoal.goal.getDateStart().getTimeInMillis(), 
				cGoal.goal.getDateEnd().getTimeInMillis());
		metTime.setNotchValues(cGoal.goal.getClaimDateList());
		
		txtCurStartDate.setText(dateFormat.format(cGoal.goal.getDateStart().getTime()));
		txtCurEndDate.setText(dateFormat.format(cGoal.goal.getDateEnd().getTime()));
		
		return rowView;
	}
	
}
