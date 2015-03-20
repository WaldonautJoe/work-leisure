package jneickhoff.workleisure;

import jneickhoff.workleisure.db.Goal;

public class NamedGoal extends Goal {

	private String taskName;
	private String taskType;
	
	public NamedGoal() {
		super();
	}
	
	public NamedGoal(Goal goal) {
		super(goal.getId(), goal.getTaskID(), goal.getBountyProgress(), 
				goal.getBountyTarget(), goal.getDateStart(), goal.getDateEnd(), 
				goal.getClaimDateList());
	}
	
	public NamedGoal(Goal goal, String taskName, String taskType) {
		this(goal);
		this.taskName = taskName;
		this.taskType = taskType;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((taskName == null) ? 0 : taskName.hashCode());
		result = prime * result
				+ ((taskType == null) ? 0 : taskType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamedGoal other = (NamedGoal) obj;
		if (taskName == null) {
			if (other.taskName != null)
				return false;
		} else if (!taskName.equals(other.taskName))
			return false;
		if (taskType == null) {
			if (other.taskType != null)
				return false;
		} else if (!taskType.equals(other.taskType))
			return false;
		return true;
	}
}
