package jneickhoff.workleisure;

import java.util.List;

import jneickhoff.workleisure.db.Task;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DueTasksArrayAdapter extends ArrayAdapter<Task>{
	
	private Context context;
	private List<Task> values;
	
	public DueTasksArrayAdapter(Context context, List<Task> values) {
		super(context, R.layout.row_task, values);
		this.context = context;
		this.values = values;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row_task, parent, false);
		
		Task task = values.get(position);
		
		TextView txtTaskName = (TextView) rowView.findViewById(R.id.txtTaskName);
		TextView txtTaskImportance = (TextView) rowView.findViewById(R.id.txtTaskImportance);
		TextView txtTaskDateDue = (TextView) rowView.findViewById(R.id.txtTaskDateDue);
		TextView txtTaskBounty = (TextView) rowView.findViewById(R.id.txtTaskBounty);
		
		
		txtTaskName.setText(task.getName());
//		if(task.getImportance().equals(Task.IMPORTANCE_HIGH)) {
//			txtTaskImportance.setText(context.getResources().getString(R.string.high));
//			txtTaskImportance.setBackgroundColor(context.getResources().getColor(R.color.yellow));
//		}
//		else if(task.getImportance().equals(Task.IMPORTANCE_LOW)) {
//			txtTaskImportance.setText(context.getResources().getString(R.string.low));
//			txtTaskImportance.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
//		}
		if(task.getType().equals(Task.TYPE_WORK)) {
			txtTaskImportance.setText(context.getResources().getString(R.string.work));
			txtTaskImportance.setBackgroundColor(context.getResources().getColor(R.color.blue));
		} 
		else {
			txtTaskImportance.setText(context.getResources().getString(R.string.leisure));
			txtTaskImportance.setBackgroundColor(context.getResources().getColor(R.color.red));
		}
			
		txtTaskDateDue.setText(" " + DateFormat.getDateFormat(context).format(task.getDateDue()));
		txtTaskBounty.setText(String.format("%.1f", task.getBounty()));
		
		return rowView;
	}

}
