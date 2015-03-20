package jneickhoff.workleisure;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.db.DataSource;
import jneickhoff.workleisure.db.Goal;
import jneickhoff.workleisure.db.Task;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TasksDueDialogFragment extends DialogFragment {
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_due, null);
		
		TextView txtDueLateHeader = (TextView) view.findViewById(R.id.txtDueLateHeader);
		LinearLayout lytDueLateTasks = (LinearLayout) view.findViewById(R.id.lytDueLateTasks);
		TextView txtDueSoonHeader = (TextView) view.findViewById(R.id.txtDueSoonHeader);
		LinearLayout lytDueSoonTasks = (LinearLayout) view.findViewById(R.id.lytDueSoonTasks);
		TextView txtGoalsDueSoonHeader = (TextView) view.findViewById(R.id.txtGoalsDueSoonHeader);
		LinearLayout lytDueSoonGoals = (LinearLayout) view.findViewById(R.id.lytDueSoonGoals);
		
		Calendar dateToday = Calendar.getInstance();
		dateToday.set(Calendar.HOUR_OF_DAY, 0);
		dateToday.set(Calendar.MINUTE, 0);
		dateToday.set(Calendar.SECOND, 0);
		dateToday.set(Calendar.MILLISECOND, 0);
		dateToday.add(Calendar.MILLISECOND, -1);
		
		Calendar dateSoon = Calendar.getInstance();
		dateSoon.set(Calendar.HOUR_OF_DAY, 0);
		dateSoon.set(Calendar.MINUTE, 0);
		dateSoon.set(Calendar.SECOND, 0);
		dateSoon.set(Calendar.MILLISECOND, 0);
		dateSoon.add(Calendar.DATE, 2);
		
		DataSource ds = new DataSource(getActivity());
		ds.open();
		List<Task> lateTasksDue = ds.getRecentDueTasks(null, dateToday);
		List<Task> soonTasksDue = ds.getRecentDueTasks(dateToday, dateSoon);
		List<Goal> soonGoalsDue = ds.getAllGoalsEndingBetween(Calendar.getInstance(), dateSoon);
		List<String> soonGoalsDueNames = new ArrayList<String>();
		for(Goal goal : soonGoalsDue) {
			Task task = ds.getTask(goal.getTaskID());
			soonGoalsDueNames.add(task.getName());
		}
		ds.close();
		
		if(lateTasksDue.size() == 0 && soonTasksDue.size() == 0 && soonGoalsDueNames.size() == 0)
			this.dismiss();
		
		if(lateTasksDue.size() > 0) {
			for(int i = 0; i < lateTasksDue.size(); i++) {
				TextView txtTask = new TextView(getActivity());
				txtTask.setText("\u2022 " + lateTasksDue.get(i).getName());
				
				lytDueLateTasks.addView(txtTask);
			}
		}
		else {
			txtDueLateHeader.setVisibility(View.GONE);
			lytDueLateTasks.setVisibility(View.GONE);
		}
		
		if(soonTasksDue.size() > 0) {
			for(int i = 0; i < soonTasksDue.size(); i++) {
				TextView txtTask = new TextView(getActivity());
				txtTask.setText("\u2022 " + soonTasksDue.get(i).getName());
				
				lytDueSoonTasks.addView(txtTask);
			}
		}
		else {
			txtDueSoonHeader.setVisibility(View.GONE);
			lytDueSoonTasks.setVisibility(View.GONE);
		}
		
		if(soonGoalsDueNames.size() > 0) {
			for(int i = 0; i < soonGoalsDueNames.size(); i++) {
				TextView txtTask = new TextView(getActivity());
				txtTask.setText("\u2022 " + soonGoalsDueNames.get(i));
				
				lytDueSoonGoals.addView(txtTask);
			}
		}
		else {
			txtGoalsDueSoonHeader.setVisibility(View.GONE);
			lytDueSoonGoals.setVisibility(View.GONE);
		}
		
		builder.setView(view)
			   .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   dialog.dismiss();
				   }
			   });
		
		return builder.create();
	}
}
