package jneickhoff.workleisure.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Goal {

	private long id;
	private long taskID;
	private float bountyProgress;
	private float bountyTarget;
	private Calendar dateStart;
	private Calendar dateEnd;
	private List<Calendar> claimDateList;
	
	public Goal() {
		id = 0;
		taskID = 0;
		bountyProgress = 0;
		bountyTarget = 0;
		dateStart = Calendar.getInstance();
		dateEnd = Calendar.getInstance();
		claimDateList = new ArrayList<Calendar>();
	}
	
	public Goal(long id, long taskID, float bountyProgress, float bountyTarget, 
			Calendar dateStart, Calendar dateEnd, List<Calendar> claimDateList) {
		this.id = id;
		this.taskID = taskID;
		this.bountyProgress = bountyProgress;
		this.bountyTarget = bountyTarget;
		this.dateStart = dateStart;
		this.dateEnd = dateEnd;
		this.claimDateList = new ArrayList<Calendar>(claimDateList);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(bountyProgress);
		result = prime * result + Float.floatToIntBits(bountyTarget);
		result = prime * result
				+ ((claimDateList == null) ? 0 : claimDateList.hashCode());
		result = prime * result + ((dateEnd == null) ? 0 : dateEnd.hashCode());
		result = prime * result
				+ ((dateStart == null) ? 0 : dateStart.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (int) (taskID ^ (taskID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Goal other = (Goal) obj;
		if (Float.floatToIntBits(bountyProgress) != Float
				.floatToIntBits(other.bountyProgress))
			return false;
		if (Float.floatToIntBits(bountyTarget) != Float
				.floatToIntBits(other.bountyTarget))
			return false;
		if (claimDateList == null) {
			if (other.claimDateList != null)
				return false;
		} else if (!claimDateList.equals(other.claimDateList))
			return false;
		if (dateEnd == null) {
			if (other.dateEnd != null)
				return false;
		} else if (!dateEnd.equals(other.dateEnd))
			return false;
		if (dateStart == null) {
			if (other.dateStart != null)
				return false;
		} else if (!dateStart.equals(other.dateStart))
			return false;
		if (id != other.id)
			return false;
		if (taskID != other.taskID)
			return false;
		return true;
	}

	public void addBountyProgress(float bounty) {
		bountyProgress += bounty;
	}
	
	public void addClaimDate(Calendar claimDate) {
		claimDateList.add(claimDate);
	}
	
	public void addClaimDate(Date claimDate) {
		Calendar calClaimDate = Calendar.getInstance();
		calClaimDate.setTime(claimDate);
		claimDateList.add(calClaimDate);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTaskID() {
		return taskID;
	}

	public void setTaskID(long taskID) {
		this.taskID = taskID;
	}

	public float getBountyProgress() {
		return bountyProgress;
	}

	public void setBountyProgress(float bountyProgress) {
		this.bountyProgress = bountyProgress;
	}

	public float getBountyTarget() {
		return bountyTarget;
	}

	public void setBountyTarget(float bountyTarget) {
		this.bountyTarget = bountyTarget;
	}

	public Calendar getDateStart() {
		return dateStart;
	}

	public void setDateStart(Calendar dateStart) {
		this.dateStart = dateStart;
	}
	
	public void setDateStart(long dateStart) {
		this.dateStart.setTimeInMillis(dateStart);
	}

	public Calendar getDateEnd() {
		return dateEnd;
	}

	public void setDateEnd(Calendar dateEnd) {
		this.dateEnd = dateEnd;
	}
	
	public void setDateEnd(long dateEnd) {
		this.dateEnd.setTimeInMillis(dateEnd);;
	}

	public List<Calendar> getClaimDateList() {
		return claimDateList;
	}

	public void setClaimDateList(List<Calendar> claimDateList) {
		this.claimDateList.clear();
		this.claimDateList.addAll(claimDateList);
	}
	
}
