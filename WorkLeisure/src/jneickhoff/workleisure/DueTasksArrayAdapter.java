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
		super(context, R.layout.row_claim_simple, values);
		this.context = context;
		this.values = values;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.row_task_due, parent, false);
		
		TextView txtTaskName = (TextView) rowView.findViewById(R.id.txtTaskName);
		TextView txtTaskDateDue = (TextView) rowView.findViewById(R.id.txtTaskDateDue);
		
		txtTaskName.setText(values.get(position).getName());
		txtTaskDateDue.setText(DateFormat.getDateFormat(context).format(values.get(position).getDateDue()));
		
		return rowView;
	}

}
