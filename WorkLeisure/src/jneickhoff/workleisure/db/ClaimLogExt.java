package jneickhoff.workleisure.db;

public class ClaimLogExt extends ClaimLog {

	private String taskName;
	
	public ClaimLogExt() {
		super();
	}
	
	public ClaimLogExt(ClaimLog claimLog) {
		super(claimLog.getId(), claimLog.getClaimDate(), claimLog.getTaskID(), 
				claimLog.getTaskType(), claimLog.getComment(), claimLog.getBounty(), 
				claimLog.getUpdatedBalance(), claimLog.getBountyDeviation(), claimLog.getDueDifference());
	}
	
	public ClaimLogExt(ClaimLog claimLog, String taskName) {
		this(claimLog);
		this.taskName = taskName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((taskName == null) ? 0 : taskName.hashCode());
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
		ClaimLogExt other = (ClaimLogExt) obj;
		if (taskName == null) {
			if (other.taskName != null)
				return false;
		} else if (!taskName.equals(other.taskName))
			return false;
		return true;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
