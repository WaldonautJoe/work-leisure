package jneickhoff.workleisure;

import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.db.Task;
import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TaskArrayAdapter extends ArrayAdapter<Task>{
	private final Context context;
	private final List<Task> values;
	
	public TaskArrayAdapter(Context context, List<Task> values) {
		super(context, R.layout.row_task, values);
		this.context = context;
		this.values = values;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row_task, parent, false);
		TextView txtTaskName = (TextView) rowView.findViewById(R.id.txtTaskName);
		TextView txtTaskImportance = (TextView) rowView.findViewById(R.id.txtTaskImportance);
		TextView txtTaskDateDue = (TextView) rowView.findViewById(R.id.txtTaskDateDue);
		TextView txtTaskBounty = (TextView) rowView.findViewById(R.id.txtTaskBounty);
		Task task = values.get(position);
		Resources res = context.getResources();
		
		txtTaskName.setText(task.getName());
		if(task.getImportance().equals(Task.IMPORTANCE_HIGH)) {
			txtTaskImportance.setText(res.getString(R.string.high));
			txtTaskImportance.setBackgroundColor(res.getColor(R.color.yellow));
		}
		else if(task.getImportance().equals(Task.IMPORTANCE_LOW)) {
			txtTaskImportance.setText(res.getString(R.string.low));
			txtTaskImportance.setBackgroundColor(res.getColor(R.color.light_grey));
		}
		if(task.isDue()) {
			txtTaskDateDue.setText(" " + DateFormat.getDateFormat(context).format(task.getDateDue()));
			
			Calendar dateDue = Calendar.getInstance();
			dateDue.setTime(task.getDateDue());
			dateDue.add(Calendar.DATE, 1);
			Calendar dateBoundary = Calendar.getInstance();
			dateBoundary.setTime(task.getDateDue());
			dateBoundary.add(Calendar.DAY_OF_MONTH, -1);
			if(Calendar.getInstance().after(dateDue))
				txtTaskDateDue.setTextColor(res.getColor(R.color.red));
			else if(Calendar.getInstance().after(dateBoundary))
				txtTaskDateDue.setTextColor(res.getColor(R.color.yellow));
			else
				txtTaskDateDue.setTextColor(res.getColor(R.color.blue));
		}
		txtTaskBounty.setText(String.format("%.1f", task.getBounty()));
		
		return rowView;
	}
	
	/**
	 * Replaces the task at the specified position with the specified task.
	 * @param position the position to replace with the specified task
	 * @param task the task to insert
	 */
	public void setTask(int position, Task task) {
		values.set(position, task);
	}
}
