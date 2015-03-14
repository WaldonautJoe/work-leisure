package jneickhoff.workleisure.db;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class Task {

	private long id;
	private String name;
	private String type;
		public final static String TYPE_WORK = "work";
		public final static String TYPE_LEISURE = "leisure";
	private String desc;
	private float bounty;
	private String stockType;
		public final static String STOCK_TYPE_UNLIMITED = "U";
		public final static String STOCK_TYPE_LIMITED = "L";
	private long stockNumber;
	private long timesClaimed;
	private boolean isArchived;
	private String importance;
		public final static String IMPORTANCE_LOW = "L";
		public final static String IMPORTANCE_HIGH = "H";
	private Calendar dateUpdated;
	private boolean isDue;
	private Calendar dateDue;
	private Goal currentGoal;
	
	public final static int COMPARE_IMPORTANCE = 0;
	public final static int COMPARE_ALPHA = 1;
	public final static int COMPARE_ALPHA_DESC = 2;
	public final static int COMPARE_UPDATED = 3;
	public final static int COMPARE_UPDATED_DESC = 4;
	public final static int COMPARE_TIMES_CLAIMED = 5;
	
	public Task() {}
	
	public Task(long id, String name, String type, String desc, float bounty, 
			String stockType, long stockNumber, long timesClaimed, boolean isArchived, 
			String importance, Calendar dateUpdated, boolean isDue, Calendar dateDue, Goal currentGoal) {
		this.id = id;
		this.name = name;
		if(type.equals(TYPE_WORK) || type.equals(TYPE_LEISURE))
			this.type = type;
		else
			throw new IllegalArgumentException();
		this.desc = desc;
		this.bounty = bounty;
		if(stockType.equals(STOCK_TYPE_UNLIMITED) || stockType.equals(STOCK_TYPE_LIMITED))
			this.stockType = stockType;
		else
			throw new IllegalArgumentException();
		this.stockNumber = stockNumber;
		this.timesClaimed = timesClaimed;
		this.isArchived = isArchived;
		if(importance.equals(IMPORTANCE_LOW) || importance.equals(IMPORTANCE_HIGH))
			this.importance = importance;
		else
			throw new IllegalArgumentException();
		this.dateUpdated = dateUpdated;
		this.isDue = isDue;
		this.dateDue = dateDue;
		this.currentGoal = currentGoal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(bounty);
		result = prime * result
				+ ((currentGoal == null) ? 0 : currentGoal.hashCode());
		result = prime * result + ((dateDue == null) ? 0 : dateDue.hashCode());
		result = prime * result
				+ ((dateUpdated == null) ? 0 : dateUpdated.hashCode());
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result
				+ ((importance == null) ? 0 : importance.hashCode());
		result = prime * result + (isArchived ? 1231 : 1237);
		result = prime * result + (isDue ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (stockNumber ^ (stockNumber >>> 32));
		result = prime * result
				+ ((stockType == null) ? 0 : stockType.hashCode());
		result = prime * result + (int) (timesClaimed ^ (timesClaimed >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Task other = (Task) obj;
		if (Float.floatToIntBits(bounty) != Float.floatToIntBits(other.bounty))
			return false;
		if (currentGoal == null) {
			if (other.currentGoal != null)
				return false;
		} else if (!currentGoal.equals(other.currentGoal))
			return false;
		if (dateDue == null) {
			if (other.dateDue != null)
				return false;
		} else if (!dateDue.equals(other.dateDue))
			return false;
		if (dateUpdated == null) {
			if (other.dateUpdated != null)
				return false;
		} else if (!dateUpdated.equals(other.dateUpdated))
			return false;
		if (desc == null) {
			if (other.desc != null)
				return false;
		} else if (!desc.equals(other.desc))
			return false;
		if (id != other.id)
			return false;
		if (importance == null) {
			if (other.importance != null)
				return false;
		} else if (!importance.equals(other.importance))
			return false;
		if (isArchived != other.isArchived)
			return false;
		if (isDue != other.isDue)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (stockNumber != other.stockNumber)
			return false;
		if (stockType == null) {
			if (other.stockType != null)
				return false;
		} else if (!stockType.equals(other.stockType))
			return false;
		if (timesClaimed != other.timesClaimed)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name + "... for " + bounty + " hours!";
	}
	
	public int compareTo(Task cmpTask, Integer compareType) {
		if(compareType == null)
			compareType = COMPARE_IMPORTANCE;
		
		return getComparator(compareType).compare(this, cmpTask);
		
		/*
		switch(compareType) {
		default:
		case COMPARE_IMPORTANCE:
			int lhsImp, rhsImp;
			if(importance.equals(Task.IMPORTANCE_HIGH))
				lhsImp = 0;
			else
				lhsImp = 1;
			if(cmpTask.getImportance().equals(Task.IMPORTANCE_HIGH))
				rhsImp = 0;
			else
				rhsImp = 1;

			if(lhsImp < rhsImp)
				return -1;
			else if(lhsImp > rhsImp)
				return 1;
			else if(isDue && !cmpTask.isDue())
				return -1;
			else if(!isDue && cmpTask.isDue())
				return 1;
			else if(isDue && cmpTask.isDue()) {
				int intCmp = dateDue.compareTo(cmpTask.getDateDue());
				if(intCmp != 0)
					return intCmp;
			}
			int intCmpUpdate = dateUpdated.compareTo(cmpTask.getDateUpdated()); 
			if(intCmpUpdate != 0)
				return intCmpUpdate;
			else
				return name.compareToIgnoreCase(cmpTask.getName());
			
		case COMPARE_ALPHA:
			return name.compareToIgnoreCase(cmpTask.getName());
			
		case COMPARE_ALPHA_DESC:
			return cmpTask.getName().compareToIgnoreCase(name);
			
		case COMPARE_UPDATED:
			int intCmpUpdated = dateUpdated.compareTo(cmpTask.getDateUpdated()); 
			if(intCmpUpdated != 0)
				return intCmpUpdated;
			else
				return name.compareToIgnoreCase(cmpTask.getName());
			
		case COMPARE_UPDATED_DESC:
			int intCmpUpdatedDesc = cmpTask.getDateUpdated().compareTo(dateUpdated); 
			if(intCmpUpdatedDesc != 0)
				return intCmpUpdatedDesc;
			else
				return name.compareToIgnoreCase(cmpTask.getName());
		case COMPARE_TIMES_CLAIMED:
			if(timesClaimed > cmpTask.getTimesClaimed())
				return -1;
			else if(timesClaimed < cmpTask.getTimesClaimed())
				return 1;
			else
				return name.compareToIgnoreCase(cmpTask.getName());
		}
		*/
	}
	
	public static Comparator<Task> getComparator(final int compareType) {
		/*
		return new Comparator<Task>() {
			
			@Override
			public int compare(Task lhs, Task rhs) {
				return lhs.compareTo(rhs, compareType);
			}
		};
		*/
		
		switch(compareType) {
		default:
		case COMPARE_IMPORTANCE:
			return new Comparator<Task>() {

				@Override
				public int compare(Task lhs, Task rhs) {
					//Importance, is due, due date, has goal, goal end date, updated, name
					int lhsImp, rhsImp;
					if(lhs.getImportance().equals(Task.IMPORTANCE_HIGH))
						lhsImp = 0;
					else
						lhsImp = 1;
					if(rhs.getImportance().equals(Task.IMPORTANCE_HIGH))
						rhsImp = 0;
					else
						rhsImp = 1;

					if(lhsImp < rhsImp)
						return -1;
					else if(lhsImp > rhsImp)
						return 1;
					else if(lhs.isDue() && !rhs.isDue())
						return -1;
					else if(!lhs.isDue() && rhs.isDue())
						return 1;
					else if(lhs.isDue() && rhs.isDue()) {
						int intCmp = lhs.getDateDue().compareTo(rhs.getDateDue());
						if(intCmp != 0)
							return intCmp;
					}
					else if(lhs.getCurrentGoal() != null && rhs.getCurrentGoal() == null)
						return -1;
					else if(lhs.getCurrentGoal() == null && rhs.getCurrentGoal() != null)
						return 1;
					else if(lhs.getCurrentGoal() != null && rhs.getCurrentGoal() != null) {
						int intCmpDateEnd = lhs.getCurrentGoal().getDateEnd().compareTo(rhs.getCurrentGoal().getDateEnd());
						if(intCmpDateEnd != 0)
							return intCmpDateEnd;
					}
					int intCmp = lhs.getDateUpdated().compareTo(rhs.getDateUpdated()); 
					if(intCmp != 0)
						return intCmp;
					else
						return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
				
			};
		case COMPARE_ALPHA:
			return new Comparator<Task>() {

				@Override
				public int compare(Task lhs, Task rhs) {
					return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
				
			};
		case COMPARE_ALPHA_DESC:
			return new Comparator<Task>() {

				@Override
				public int compare(Task lhs, Task rhs) {
					return rhs.getName().compareToIgnoreCase(lhs.getName());
				}
				
			};
		case COMPARE_UPDATED:
			return new Comparator<Task>() {

				@Override
				public int compare(Task lhs, Task rhs) {
					int intCmp = lhs.getDateUpdated().compareTo(rhs.getDateUpdated()); 
					if(intCmp > 0 || intCmp < 0)
						return intCmp;
					else
						return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
				
			};
		case COMPARE_UPDATED_DESC:
			return new Comparator<Task>() {

				@Override
				public int compare(Task lhs, Task rhs) {
					int intCmp = rhs.getDateUpdated().compareTo(lhs.getDateUpdated()); 
					if(intCmp > 0 || intCmp < 0)
						return intCmp;
					else
						return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
				
			};
		case COMPARE_TIMES_CLAIMED:
			return new Comparator<Task>() {
				
				@Override
				public int compare(Task lhs, Task rhs) {
					if(lhs.getTimesClaimed() > rhs.getTimesClaimed())
						return -1;
					else if(lhs.getTimesClaimed() < rhs.getTimesClaimed())
						return 1;
					else
						return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
			};
		}
		
	}
	
	public long getID() {
		return id;
	}
	
	public void setID(long taskID) {
		this.id = taskID;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String taskName) {
		this.name = taskName;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String taskType) {
		if(taskType.equals(TYPE_WORK) || taskType.equals(TYPE_LEISURE))
			this.type = taskType;
		else
			throw new IllegalArgumentException();
	}
	
	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String taskDesc) {
		this.desc = taskDesc;
	}
	
	public float getBounty() {
		return bounty;
	}
	
	public void setBounty(float taskBounty) {
		this.bounty = taskBounty;
	}
	
	public String getStockType() {
		return stockType;
	}
	
	public void setStockType(String taskStockType) {
		if(taskStockType.equals(STOCK_TYPE_UNLIMITED) || taskStockType.equals(STOCK_TYPE_LIMITED))
			this.stockType = taskStockType;
		else
			throw new IllegalArgumentException();
	}
	
	public long getStockNumber() {
		return stockNumber;
	}
	
	public void setStockNumber(long taskStockNumber) {
		this.stockNumber = taskStockNumber;
	}
	
	public long getTimesClaimed() {
		return timesClaimed;
	}
	
	public void setTimesClaimed(long taskTimesClaimed) {
		this.timesClaimed = taskTimesClaimed;
	}
	
	/**
	 * @return whether task is archived
	 */
	public boolean isArchived() {
		return isArchived;
	}

	/**
	 * @param isArchived whether task is archived
	 */
	public void setArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}

	/**
	 * @return the importance
	 */
	public String getImportance() {
		return importance;
	}

	/**
	 * @param importance the importance to set
	 */
	public void setImportance(String importance) {
		if(importance.equals(IMPORTANCE_LOW) || importance.equals(IMPORTANCE_HIGH))
			this.importance = importance;
		else
			throw new IllegalArgumentException();
	}

	/**
	 * @return the date updated
	 */
	public Calendar getDateUpdated() {
		return dateUpdated;
	}

	/**
	 * @param dateUpdated the update date to set
	 */
	public void setDateUpdated(Calendar dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	/**
	 * @return the date due
	 */
	public Calendar getDateDue() {
		return dateDue;
	}
	
	/**
	 * @return whether task is due
	 */
	public boolean isDue() {
		return isDue;
	}

	/**
	 * @param isDue set whether task is due
	 */
	public void setDue(boolean isDue) {
		this.isDue = isDue;
	}

	/**
	 * @param dateDue the due date to set
	 */
	public void setDateDue(Calendar dateDue) {
		this.dateDue = dateDue;
	}
	
	public Goal getCurrentGoal() {
		return currentGoal;
	}
	
	public void setCurrentGoal(Goal currentGoal) {
		this.currentGoal = currentGoal;
	}
	
}
