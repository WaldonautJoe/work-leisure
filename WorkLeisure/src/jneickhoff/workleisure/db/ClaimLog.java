package jneickhoff.workleisure.db;

import java.util.Date;

public class ClaimLog {

	private long id;
	private Date claimDate;
	private Long taskID;
	private String taskType;
	private String comment;
	private float bounty;
	private float updatedBalance;
	private float bountyDeviation;
	private Long dueDifference;

	public ClaimLog() {
		
	}
	
	public ClaimLog(long id, Date claimDate, Long taskID, String taskType, String comment,
			float bounty, float updatedBalance, float bountyDeviation, Long dueDifference) {
		this.id = id;
		this.claimDate = claimDate;
		this.taskID = taskID;
		this.taskType = taskType;
		this.comment = comment;
		this.bounty = bounty;
		this.updatedBalance = updatedBalance;
		this.bountyDeviation = bountyDeviation;
		this.dueDifference = dueDifference;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(bounty);
		result = prime * result + Float.floatToIntBits(bountyDeviation);
		result = prime * result
				+ ((claimDate == null) ? 0 : claimDate.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result
				+ ((dueDifference == null) ? 0 : dueDifference.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((taskID == null) ? 0 : taskID.hashCode());
		result = prime * result
				+ ((taskType == null) ? 0 : taskType.hashCode());
		result = prime * result + Float.floatToIntBits(updatedBalance);
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
		ClaimLog other = (ClaimLog) obj;
		if (Float.floatToIntBits(bounty) != Float.floatToIntBits(other.bounty))
			return false;
		if (Float.floatToIntBits(bountyDeviation) != Float
				.floatToIntBits(other.bountyDeviation))
			return false;
		if (claimDate == null) {
			if (other.claimDate != null)
				return false;
		} else if (!claimDate.equals(other.claimDate))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (dueDifference == null) {
			if (other.dueDifference != null)
				return false;
		} else if (!dueDifference.equals(other.dueDifference))
			return false;
		if (id != other.id)
			return false;
		if (taskID == null) {
			if (other.taskID != null)
				return false;
		} else if (!taskID.equals(other.taskID))
			return false;
		if (taskType == null) {
			if (other.taskType != null)
				return false;
		} else if (!taskType.equals(other.taskType))
			return false;
		if (Float.floatToIntBits(updatedBalance) != Float
				.floatToIntBits(other.updatedBalance))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.claimDate + " ... " + this.bounty;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the claim date
	 */
	public Date getClaimDate() {
		return claimDate;
	}

	/**
	 * @param claimDate the claim date to set
	 */
	public void setClaimDate(Date claimDate) {
		this.claimDate = claimDate;
	}

	/**
	 * @return the task ID
	 */
	public Long getTaskID() {
		return taskID;
	}

	/**
	 * @param taskID the task ID to set
	 */
	public void setTaskID(Long taskID) {
		this.taskID = taskID;
	}
	
	/**
	 * @return the taskType
	 */
	public String getTaskType() {
		return taskType;
	}

	/**
	 * @param taskType the taskType to set
	 */
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the bounty
	 */
	public float getBounty() {
		return bounty;
	}

	/**
	 * @param bounty the bounty to set
	 */
	public void setBounty(float bounty) {
		this.bounty = bounty;
	}
	
	/**
	 * @return the updatedBalance
	 */
	public float getUpdatedBalance() {
		return updatedBalance;
	}

	/**
	 * @param updatedBalance the updatedBalance to set
	 */
	public void setUpdatedBalance(float updatedBalance) {
		this.updatedBalance = updatedBalance;
	}

	/**
	 * @return the bounty deviation
	 */
	public float getBountyDeviation() {
		return bountyDeviation;
	}

	/**
	 * @param bountyDeviation the bounty deviation to set
	 */
	public void setBountyDeviation(float bountyDeviation) {
		this.bountyDeviation = bountyDeviation;
	}

	/**
	 * @return the due difference
	 */
	public Long getDueDifference() {
		return dueDifference;
	}

	/**
	 * @param dueDifference the due difference to set
	 */
	public void setDueDifference(Long dueDifference) {
		this.dueDifference = dueDifference;
	}
	
	
}
