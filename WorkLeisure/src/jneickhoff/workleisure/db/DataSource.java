package jneickhoff.workleisure.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jneickhoff.workleisure.NamedGoal;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataSource {

		private SQLiteDatabase database;
		private MySQLiteHelper dbHelper;
		private String[] taskColumns = {
				MySQLiteHelper.COL_TASK_ID,
				MySQLiteHelper.COL_TASK_NAME,
				MySQLiteHelper.COL_TASK_TYPE,
				MySQLiteHelper.COL_TASK_DESC,
				MySQLiteHelper.COL_TASK_BOUNTY,
				MySQLiteHelper.COL_TASK_STOCK_TYPE,
				MySQLiteHelper.COL_TASK_STOCK_NUMBER,
				MySQLiteHelper.COL_TASK_TIMES_CLAIMED,
				MySQLiteHelper.COL_TASK_IS_ARCHIVED,
				MySQLiteHelper.COL_TASK_IMPORTANCE,
				MySQLiteHelper.COL_TASK_DATE_UPDATED,
				MySQLiteHelper.COL_TASK_IS_DUE,
				MySQLiteHelper.COL_TASK_DATE_DUE
		};
		
		private String[] claimLogColumns = {
				MySQLiteHelper.COL_CLAIM_ID,
				MySQLiteHelper.COL_CLAIM_DATE,
				MySQLiteHelper.COL_TASK_ID,
				MySQLiteHelper.COL_TASK_TYPE,
				MySQLiteHelper.COL_CLAIM_COMMENT,
				MySQLiteHelper.COL_CLAIM_BOUNTY,
				MySQLiteHelper.COL_CLAIM_UPDATED_BALANCE,
				MySQLiteHelper.COL_CLAIM_BOUNTY_DEVIATION,
				MySQLiteHelper.COL_CLAIM_DUE_DIFFERENCE 
		};
		
		private String[] goalColumns = {
				MySQLiteHelper.COL_GOAL_ID,
				MySQLiteHelper.COL_TASK_ID,
				MySQLiteHelper.COL_GOAL_BOUNTY_TARGET,
				MySQLiteHelper.COL_GOAL_DATE_START,
				MySQLiteHelper.COL_GOAL_DATE_END
		};
		
		
		public DataSource(Context context) {
			dbHelper = new MySQLiteHelper(context);
		}
		
		public void open() throws SQLException {
			database = dbHelper.getWritableDatabase();
		}
		
		public void close() {
			dbHelper.close();
		}
		
		/**
		 * Add task record to database
		 * 
		 * @param name 			the name of the task
		 * @param type 			the type of the task, should be "work" or "leisure"
		 * @param description	description of the task
		 * @param bounty		amount balance should change upon claiming the task
		 * @param stockType		"U" for unlimited or "L" for limited
		 * @param stockNumber	Number of claims allowed on task if limited
		 * @param timesClaimed	Number of times the task has been claimed
		 * @param isArchived	Whether task is archived
		 * @param importance	Importance level of task
		 * @param isDue			Whether task is due
		 * @param dateDue		Date the task is due
		 * 
		 * @return task containing information entered into database
		 */
		public Task createTask(String name, String type, String description, float bounty, 
				String stockType, long stockNumber, long timesClaimed, boolean isArchived,
				String importance, boolean isDue, Calendar dateDue) {
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COL_TASK_NAME, name);
			values.put(MySQLiteHelper.COL_TASK_TYPE, type);
			values.put(MySQLiteHelper.COL_TASK_DESC, description);
			values.put(MySQLiteHelper.COL_TASK_BOUNTY, bounty);
			values.put(MySQLiteHelper.COL_TASK_STOCK_TYPE, stockType);
			values.put(MySQLiteHelper.COL_TASK_STOCK_NUMBER, stockNumber);
			values.put(MySQLiteHelper.COL_TASK_TIMES_CLAIMED, timesClaimed);
			if(isArchived)
				values.put(MySQLiteHelper.COL_TASK_IS_ARCHIVED, MySQLiteHelper.SQLITE_TRUE);
			else
				values.put(MySQLiteHelper.COL_TASK_IS_ARCHIVED, MySQLiteHelper.SQLITE_FALSE);
			values.put(MySQLiteHelper.COL_TASK_IMPORTANCE, importance);
			Calendar c = Calendar.getInstance();
			values.put(MySQLiteHelper.COL_TASK_DATE_UPDATED, c.getTimeInMillis());
			if(isDue) {
				values.put(MySQLiteHelper.COL_TASK_IS_DUE, MySQLiteHelper.SQLITE_TRUE);
				values.put(MySQLiteHelper.COL_TASK_DATE_DUE, dateDue.getTimeInMillis());
			}
			else {
				values.put(MySQLiteHelper.COL_TASK_IS_DUE, MySQLiteHelper.SQLITE_FALSE);
				values.putNull(MySQLiteHelper.COL_TASK_DATE_DUE);
			}
			
			long insertId = database.insert(MySQLiteHelper.TAB_TASK, null, values);
			Cursor cursor = database.query(MySQLiteHelper.TAB_TASK, taskColumns, 
					MySQLiteHelper.COL_TASK_ID + " = " + insertId, null, null, null, null);
			cursor.moveToFirst();
			Task newTask = cursorToTask(cursor);
			newTask.setCurrentGoal(null);
			cursor.close();
			
			return newTask;
		}

		
		/**
		 * Update the task record in database
		 * @param task new field values
		 */
		public void updateTask(Task task) {
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COL_TASK_NAME, task.getName());
			values.put(MySQLiteHelper.COL_TASK_TYPE, task.getType());
			values.put(MySQLiteHelper.COL_TASK_DESC, task.getDesc());
			values.put(MySQLiteHelper.COL_TASK_BOUNTY, task.getBounty());
			values.put(MySQLiteHelper.COL_TASK_STOCK_TYPE, task.getStockType());
			values.put(MySQLiteHelper.COL_TASK_STOCK_NUMBER, task.getStockNumber());
			values.put(MySQLiteHelper.COL_TASK_TIMES_CLAIMED, task.getTimesClaimed());
			if(task.isArchived())
				values.put(MySQLiteHelper.COL_TASK_IS_ARCHIVED, MySQLiteHelper.SQLITE_TRUE);
			else
				values.put(MySQLiteHelper.COL_TASK_IS_ARCHIVED, MySQLiteHelper.SQLITE_FALSE);
			values.put(MySQLiteHelper.COL_TASK_IMPORTANCE, task.getImportance());
			values.put(MySQLiteHelper.COL_TASK_DATE_UPDATED, task.getDateUpdated().getTimeInMillis());
			if(task.isDue()) {
				values.put(MySQLiteHelper.COL_TASK_IS_DUE, MySQLiteHelper.SQLITE_TRUE);
				values.put(MySQLiteHelper.COL_TASK_DATE_DUE, task.getDateDue().getTimeInMillis());
			}
			else {
				values.put(MySQLiteHelper.COL_TASK_IS_DUE, MySQLiteHelper.SQLITE_FALSE);
				values.putNull(MySQLiteHelper.COL_TASK_DATE_DUE);
			}
			
			database.update(MySQLiteHelper.TAB_TASK, values, 
					MySQLiteHelper.COL_TASK_ID + " = " + task.getID(), null);
		}
		
		/**
		 * Delete task record from database (permanent). 
		 * Delete all claim logs associated with task
		 * @param task the task to delete
		 */
		public void deleteTask(Task task) {
			long id = task.getID();
			//database.delete(MySQLiteHelper.TAB_CLAIM_LOG,
			//		MySQLiteHelper.COL_TASK_ID + " = " + id, null);
			database.delete(MySQLiteHelper.TAB_GOAL, 
					MySQLiteHelper.COL_TASK_ID + " = " + id, null);
			ContentValues values = new ContentValues();
			values.putNull(MySQLiteHelper.COL_TASK_ID);
			database.update(MySQLiteHelper.TAB_CLAIM_LOG, values, 
					MySQLiteHelper.COL_TASK_ID + " = " + id, null);
			database.delete(MySQLiteHelper.TAB_TASK, 
					MySQLiteHelper.COL_TASK_ID + " = " + id, null);
		}
		
		/**
		 * Query list of all tasks in database
		 * @return list of tasks
		 */
		public List<Task> getAllTasks() {
			List<Task> tasks = new ArrayList<Task>();
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_TASK, taskColumns, 
					null, null, null, null, null);
			
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				Task task = cursorToTask(cursor);
				task.setCurrentGoal(getCurrentGoalForTask(task.getID()));
				tasks.add(task);
				cursor.moveToNext();
			}
			cursor.close();
			
			return tasks;
		}
		
		/**
		 * Returns the name of the task whose ID was provided
		 */
		public String getTaskName(long taskId) {
			String[] taskNameColumn = { MySQLiteHelper.COL_TASK_NAME };
			String taskName;
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_TASK, taskNameColumn, 
					MySQLiteHelper.COL_TASK_ID + " = " + taskId, 
					null, null, null, null);
			
			cursor.moveToFirst();
			if(!cursor.isAfterLast()) {
				taskName = cursor.getString(0);
				
				cursor.close();
				
				return taskName;
			}
			else {
				throw new IllegalArgumentException("No task associated with ID");
			}
			
		}
		
		/**
		 * Returns a list of the name and type of the task whose ID was provided
		 */
		private String[] getTaskNameType(long taskId) {
			String[] taskNameTypeColumns = { MySQLiteHelper.COL_TASK_NAME,
										MySQLiteHelper.COL_TASK_TYPE};
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_TASK, taskNameTypeColumns, 
					MySQLiteHelper.COL_TASK_ID + " = " + taskId, 
					null, null, null, null);
			
			cursor.moveToFirst();
			if(!cursor.isAfterLast()) {
				String[] taskNameType = {cursor.getString(0),	//task name
										 cursor.getString(1)};	//task type
				
				cursor.close();
				return taskNameType;
			}
			else {
				throw new IllegalArgumentException("No task associated with ID");
			}
			
		}
		
		/**
		 * Returns a list of all tasks with specified type
		 * 
		 * @param taskType type of tasks to return
		 * @return list of tasks
		 */
		public List<Task> getAllTasksOfType(String taskType, boolean isArchived) {
			List<Task> tasks = new ArrayList<Task>();
			
			String selection;
			if(isArchived)
				selection = MySQLiteHelper.COL_TASK_IS_ARCHIVED + " = " + MySQLiteHelper.SQLITE_TRUE;
			else
				selection = MySQLiteHelper.COL_TASK_IS_ARCHIVED + " = " + MySQLiteHelper.SQLITE_FALSE;
			
			selection += " AND " + MySQLiteHelper.COL_TASK_TYPE + " = \"" + taskType + "\"";
			
			String orderBy = MySQLiteHelper.COL_TASK_IMPORTANCE
					+ ", " + MySQLiteHelper.COL_TASK_DATE_DUE + " is null"
					+ ", " + MySQLiteHelper.COL_TASK_DATE_DUE
					+ ", " + MySQLiteHelper.COL_TASK_DATE_UPDATED
					+ ", " + MySQLiteHelper.COL_TASK_NAME;
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_TASK, taskColumns, 
					selection, null, null, null, orderBy);
			
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				Task task = cursorToTask(cursor);
				task.setCurrentGoal(getCurrentGoalForTask(task.getID()));
				tasks.add(task);
				cursor.moveToNext();
			}
			cursor.close();
			
			return tasks;
		}
		
		public List<Task> getRecentDueTasks(Calendar tasksAfterDate, Calendar tasksBeforeDate) {
			List<Task> tasksDue = new ArrayList<Task>();
			String selection;
			String[] selectionArgs;
			String orderBy = MySQLiteHelper.COL_TASK_DATE_DUE
					+ ", " + MySQLiteHelper.COL_TASK_IMPORTANCE
					+ ", " + MySQLiteHelper.COL_TASK_NAME;
			
			if(tasksAfterDate != null && tasksBeforeDate != null) {
				selection = MySQLiteHelper.COL_TASK_DATE_DUE + " >= ?"
						+ " AND " + MySQLiteHelper.COL_TASK_DATE_DUE + " < ?";
				selectionArgs = new String[2];
				selectionArgs[0] = String.valueOf(tasksAfterDate.getTimeInMillis());
				selectionArgs[1] = String.valueOf(tasksBeforeDate.getTimeInMillis());
			}
			else if(tasksAfterDate != null) {
				selection = MySQLiteHelper.COL_TASK_DATE_DUE + " >= ?";
				selectionArgs = new String[1];
				selectionArgs[0] = String.valueOf(tasksAfterDate.getTimeInMillis());
			}
			else if(tasksBeforeDate != null) {
				selection = MySQLiteHelper.COL_TASK_DATE_DUE + " < ?";
				selectionArgs = new String[1];
				selectionArgs[0] = String.valueOf(tasksBeforeDate.getTimeInMillis());
			}
			else
				throw new IllegalArgumentException("One of the parameters must have a value");
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_TASK, taskColumns, 
					selection, selectionArgs, null, null, orderBy);
			
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				//task[1] = DateFormat.getDateFormat(context).format(dateDue.getTime());
				Task task = cursorToTask(cursor);
				task.setCurrentGoal(getCurrentGoalForTask(task.getID()));
				tasksDue.add(task);
				cursor.moveToNext();
			}
			cursor.close();
			
			return tasksDue;
		}
		
		/**
		 * Query a single task with the specified ID
		 * @param id ID of task querying
		 * @return Task containing information in database
		 */
		public Task getTask(long id) {
			Cursor cursor = database.query(MySQLiteHelper.TAB_TASK, taskColumns, 
					MySQLiteHelper.COL_TASK_ID + " = " + id, null, null, null, null);
			
			cursor.moveToFirst();
			Task task = cursorToTask(cursor);
			task.setCurrentGoal(getCurrentGoalForTask(task.getID()));
			cursor.close();
			
			return task;
		}
		
		private Task cursorToTask(Cursor cursor) {
			Task task = new Task();
			task.setID(cursor.getLong(0));
			task.setName(cursor.getString(1));
			task.setType(cursor.getString(2));
			task.setDesc(cursor.getString(3));
			task.setBounty(cursor.getFloat(4));
			task.setStockType(cursor.getString(5));
			task.setStockNumber(cursor.getLong(6));
			task.setTimesClaimed(cursor.getLong(7));
			if(cursor.getLong(8) == MySQLiteHelper.SQLITE_TRUE)
				task.setArchived(true);
			else
				task.setArchived(false);
			task.setImportance(cursor.getString(9));
			Calendar dateUpdated = Calendar.getInstance();
			dateUpdated.setTimeInMillis(cursor.getLong(10));
			task.setDateUpdated(dateUpdated);
			if(cursor.getLong(11) == MySQLiteHelper.SQLITE_TRUE) {
				task.setDue(true);
				Calendar dateDue = Calendar.getInstance();
				dateDue.setTimeInMillis(cursor.getLong(12));
				task.setDateDue(dateDue);
			}
			else {
				task.setDue(false);
				task.setDateDue(null);
			}
			
			
			return task;
		}
		
		
		/**
		 * Adds a claim log record to the database
		 * 
		 * @param date 				date of claim
		 * @param taskID			ID of task claimed
		 * @param taskType			type of task
		 * @param comment			
		 * @param bounty			bounty claimed
		 * @param updatedBalance	balance after claim
		 * @param bountyDeviation	difference between bounty claimed and expected
		 * @param dueDifference		distance from due date
		 * @return
		 */
		public ClaimLog createClaimLog(Calendar date, long taskID, String taskType, String comment, 
				float bounty, float updatedBalance, float bountyDeviation, Long dueDifference){
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COL_CLAIM_DATE, date.getTimeInMillis());
			values.put(MySQLiteHelper.COL_TASK_ID, taskID);
			values.put(MySQLiteHelper.COL_TASK_TYPE, taskType);
			values.put(MySQLiteHelper.COL_CLAIM_COMMENT, comment);
			values.put(MySQLiteHelper.COL_CLAIM_BOUNTY, bounty);
			values.put(MySQLiteHelper.COL_CLAIM_UPDATED_BALANCE, updatedBalance);
			values.put(MySQLiteHelper.COL_CLAIM_BOUNTY_DEVIATION, bountyDeviation);
			if(dueDifference == null)
				values.putNull(MySQLiteHelper.COL_CLAIM_DUE_DIFFERENCE);
			else
				values.put(MySQLiteHelper.COL_CLAIM_DUE_DIFFERENCE, dueDifference);
			
			long insertID = database.insert(MySQLiteHelper.TAB_CLAIM_LOG, null, values);
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogColumns,
					MySQLiteHelper.COL_CLAIM_ID + " = " + insertID, null, null, null, null);
			cursor.moveToFirst();
			ClaimLog claimLog = cursorToClaimLog(cursor);
			cursor.close();
			
			return claimLog;
		}
		
		/**
		 * Update claim record in database
		 * @param claimLog new field values
		 */
		public void updateClaimLog(ClaimLog claimLog) {
			//consider cutting values update if this method becomes useful
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COL_CLAIM_DATE, claimLog.getClaimDate().getTimeInMillis());
			if(claimLog.getTaskID() == null)
				values.putNull(MySQLiteHelper.COL_TASK_ID);
			else
				values.put(MySQLiteHelper.COL_TASK_ID, claimLog.getTaskID());
			values.put(MySQLiteHelper.COL_TASK_TYPE, claimLog.getTaskType());
			values.put(MySQLiteHelper.COL_CLAIM_COMMENT, claimLog.getComment());
			values.put(MySQLiteHelper.COL_CLAIM_BOUNTY, claimLog.getBounty());
			values.put(MySQLiteHelper.COL_CLAIM_UPDATED_BALANCE, claimLog.getUpdatedBalance());
			values.put(MySQLiteHelper.COL_CLAIM_BOUNTY_DEVIATION, claimLog.getBountyDeviation());
			if(claimLog.getDueDifference() == null)
				values.putNull(MySQLiteHelper.COL_CLAIM_DUE_DIFFERENCE);
			else
				values.put(MySQLiteHelper.COL_CLAIM_DUE_DIFFERENCE, claimLog.getDueDifference());
			
			database.update(MySQLiteHelper.TAB_CLAIM_LOG, values, 
					MySQLiteHelper.COL_CLAIM_ID + " = " + claimLog.getId(), null);
		}
		
		/**
		 * Delete claim log record from database
		 * @param claimLog the claim log to delete
		 */
		public void deleteClaimLog(ClaimLog claimLog) {
			long id = claimLog.getId();
			database.delete(MySQLiteHelper.TAB_CLAIM_LOG, 
					MySQLiteHelper.COL_CLAIM_ID + " = " + id, null);
		}
		
		/**
		 * Query list of all claim logs in database; ordered by claim date; set limit to null to return all claims
		 * @param limit max number of claims to return, set to null to have no limit
		 * @param retNullTaskID whether to return claims whose task has been deleted
		 * @return list of claim logs
		 */
		public List<ClaimLog> getAllClaimLogs(Integer limit, boolean retNullTaskID) {
			List<ClaimLog> claimLogs = new ArrayList<ClaimLog>();
			String selection;
			
			if(retNullTaskID)
				selection = null;
			else
				selection = MySQLiteHelper.COL_TASK_ID + " is not null";
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogColumns, 
					selection, null, null, null, MySQLiteHelper.COL_CLAIM_DATE + " desc");
			
			cursor.moveToFirst();
			if(limit == null) {
				while(!cursor.isAfterLast()) {
					ClaimLog claimLog = cursorToClaimLog(cursor);
					claimLogs.add(claimLog);
					cursor.moveToNext();
				}
			}
			else {
				int i = 0;
				while(!cursor.isAfterLast() && i < limit) {
					ClaimLog claimLog = cursorToClaimLog(cursor);
					claimLogs.add(claimLog);
					cursor.moveToNext();
					i++;
				}
			}
			
			cursor.close();
			
			return claimLogs;
		}
		
		/**
		 * Query list of all claim logs in database; ordered by claim date; set limit to null to return all claims
		 * @param claimsAfterDate oldest age of claims to return
		 * @param claimsBeforeDate newest age of claims to return
		 * @param limit max number of claims to return, set to null to have no limit
		 * @param retNullTaskID whether to return claims whose task has been deleted
		 * @return list of claim logs
		 */
		public List<ClaimLog> getAllClaimLogs(Calendar claimsAfterDate, Calendar claimsBeforeDate, Integer limit, boolean retNullTaskID) {
			List<ClaimLog> claimLogs = new ArrayList<ClaimLog>();
			String selection = "";
			
			if(!retNullTaskID)
				selection += MySQLiteHelper.COL_TASK_ID + " is not null";
			if(!retNullTaskID && claimsAfterDate != null)
				selection += " AND ";
			if(claimsAfterDate != null)
				selection += MySQLiteHelper.COL_CLAIM_DATE + " >= " + claimsAfterDate.getTimeInMillis();
			if(claimsAfterDate != null && claimsBeforeDate != null)
				selection += " AND ";
			if(claimsBeforeDate != null)
				selection += MySQLiteHelper.COL_CLAIM_DATE + " < " + claimsBeforeDate.getTimeInMillis();
			
			if(selection.length() == 0)
				selection = null;
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogColumns, 
					selection, null, null, null, MySQLiteHelper.COL_CLAIM_DATE + " desc");
			
			cursor.moveToFirst();
			if(limit == null) {
				while(!cursor.isAfterLast()) {
					ClaimLog claimLog = cursorToClaimLog(cursor);
					claimLogs.add(claimLog);
					cursor.moveToNext();
				}
			}
			else {
				int i = 0;
				while(!cursor.isAfterLast() && i < limit) {
					ClaimLog claimLog = cursorToClaimLog(cursor);
					claimLogs.add(claimLog);
					cursor.moveToNext();
					i++;
				}
			}
			
			cursor.close();
			
			return claimLogs;
		}
		
		/**
		 * Return list of claim logs with associated task names
		 */
		public List<ClaimLogExt> getAllClaimLogsExt(Calendar claimsAfterDate, Calendar claimsBeforeDate, boolean retNullTaskID) {
			List<ClaimLogExt> claimsExt = new ArrayList<ClaimLogExt>();
			
			List<ClaimLog> claimLogs = getAllClaimLogs(claimsAfterDate, claimsBeforeDate, null, retNullTaskID);
			
			for(ClaimLog claim : claimLogs) {
				String taskName = getTaskName(claim.getTaskID());
				claimsExt.add(new ClaimLogExt(claim, taskName));
			}
			
			return claimsExt;
		}
		
		/**
		 * Query list of claim logs in database for a specific task; ordered by claim date; set limit to null to return all claims
		 * @param claimsAfterDate oldest age of claims to return
		 * @param claimsBeforeDate newest age of claims to return
		 * @param limit max number of claims to return, set to null to have no limit
		 * @param retNullTaskID whether to return claims whose task has been deleted
		 * @return list of claim logs
		 */
		public List<ClaimLog> getAllClaimLogs(long taskID, Calendar claimsAfterDate, Calendar claimsBeforeDate, 
				Integer limit, boolean retNullTaskID) {
			List<ClaimLog> claimLogs = new ArrayList<ClaimLog>();
			String selection = MySQLiteHelper.COL_TASK_ID + " = " + taskID;
			
			if(!retNullTaskID)
				selection += " AND " + MySQLiteHelper.COL_TASK_ID + " is not null";
			if(claimsAfterDate != null)
				selection += " AND " + MySQLiteHelper.COL_CLAIM_DATE + " >= " + claimsAfterDate.getTimeInMillis();
			if(claimsBeforeDate != null)
				selection += " AND " + MySQLiteHelper.COL_CLAIM_DATE + " < " + claimsBeforeDate.getTimeInMillis();
						
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogColumns, 
					selection, null, null, null, MySQLiteHelper.COL_CLAIM_DATE + " desc");
			
			cursor.moveToFirst();
			if(limit == null) {
				while(!cursor.isAfterLast()) {
					ClaimLog claimLog = cursorToClaimLog(cursor);
					claimLogs.add(claimLog);
					cursor.moveToNext();
				}
			}
			else {
				int i = 0;
				while(!cursor.isAfterLast() && i < limit) {
					ClaimLog claimLog = cursorToClaimLog(cursor);
					claimLogs.add(claimLog);
					cursor.moveToNext();
					i++;
				}
			}
			
			cursor.close();
			
			return claimLogs;
		}
		
		/**
		 * Query list of all claim logs in database for specific task; ordered by claim date
		 * 
		 * @param taskID ID of task of which to query logs
		 * @param limit max number of claims to return, set to null to have no limit
		 * @return list of claim logs
		 */
		public List<ClaimLog> getAllClaimLogs(long taskID, Integer limit) {
			List<ClaimLog> claimLogs = new ArrayList<ClaimLog>();
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogColumns, 
					MySQLiteHelper.COL_TASK_ID + " = " + taskID, null, null, null, 
					MySQLiteHelper.COL_CLAIM_DATE + " desc");
			
			cursor.moveToFirst();
			if(limit == null) {
				while(!cursor.isAfterLast()) {
					ClaimLog claimLog = cursorToClaimLog(cursor);
					claimLogs.add(claimLog);
					cursor.moveToNext();
				}
			}
			else {
				int i = 0;
				while(!cursor.isAfterLast() && i < limit) {
					ClaimLog claimLog = cursorToClaimLog(cursor);
					claimLogs.add(claimLog);
					cursor.moveToNext();
					i++;
				}
			}
			cursor.close();
			
			return claimLogs;
		}
		
		/**
		 * Return a claim log with the specified ID from database
		 * @param claimID ID of claim log to query
		 * @return claim log with specified ID
		 */
		public ClaimLog getClaimLog(long claimID) {
			ClaimLog claimLog = new ClaimLog();
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogColumns, 
					MySQLiteHelper.COL_CLAIM_ID + " = " + claimID, null, null, null, 
					MySQLiteHelper.COL_CLAIM_DATE);
			
			cursor.moveToFirst();
			if(!cursor.isAfterLast())
				claimLog = cursorToClaimLog(cursor);
			cursor.close();
			
			return claimLog;
		}
		
		private ClaimLog cursorToClaimLog(Cursor cursor) {
			ClaimLog claimLog = new ClaimLog();
			claimLog.setId(cursor.getLong(0));
			claimLog.setClaimDate(cursor.getLong(1));
			if(cursor.getType(2) == Cursor.FIELD_TYPE_NULL)
				claimLog.setTaskID(null);
			else
				claimLog.setTaskID(cursor.getLong(2));
			claimLog.setTaskType(cursor.getString(3));
			claimLog.setComment(cursor.getString(4));
			claimLog.setBounty(cursor.getFloat(5));
			claimLog.setUpdatedBalance(cursor.getFloat(6));
			claimLog.setBountyDeviation(cursor.getFloat(7));
			if(cursor.getType(8) == Cursor.FIELD_TYPE_NULL)
				claimLog.setDueDifference(null);
			else
				claimLog.setDueDifference(cursor.getLong(8));
			
			return claimLog;
		}
		
		public Goal createGoal(long taskID, float bountyTarget, Calendar startDate, Calendar endDate) {
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COL_TASK_ID, taskID);
			values.put(MySQLiteHelper.COL_GOAL_BOUNTY_TARGET, bountyTarget);
			values.put(MySQLiteHelper.COL_GOAL_DATE_START, startDate.getTimeInMillis());
			values.put(MySQLiteHelper.COL_GOAL_DATE_END, endDate.getTimeInMillis());
			
			long insertID = database.insert(MySQLiteHelper.TAB_GOAL, null, values);
			Cursor cursor = database.query(MySQLiteHelper.TAB_GOAL, goalColumns,
					MySQLiteHelper.COL_GOAL_ID + " = " + insertID, null, null, null, null);
			cursor.moveToFirst();
			Goal goal = cursorToGoal(cursor);
			cursor.close();
			
			List<ClaimLog> claims = getAllClaimLogs(goal.getTaskID(), goal.getDateStart(), goal.getDateEnd(), null, false);
			for(ClaimLog claim : claims) {
				goal.addBountyProgress(claim.getBounty());
				goal.addClaimDate(claim.getClaimDate());
			}
			
			return goal;
		}
		
		public void updateGoal(Goal goal) {
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COL_TASK_ID, goal.getTaskID());
			values.put(MySQLiteHelper.COL_GOAL_BOUNTY_TARGET, goal.getBountyTarget());
			values.put(MySQLiteHelper.COL_GOAL_DATE_START, goal.getDateStart().getTimeInMillis());
			values.put(MySQLiteHelper.COL_GOAL_DATE_END, goal.getDateEnd().getTimeInMillis());
			
			database.update(MySQLiteHelper.TAB_GOAL, values, 
					MySQLiteHelper.COL_GOAL_ID + " = " + goal.getId(), null);
		}
		
		public void deleteGoal(Goal goal) {
			database.delete(MySQLiteHelper.TAB_GOAL, 
					MySQLiteHelper.COL_GOAL_ID + " = " + goal.getId(), null);
		}
		
		public Goal getGoal(long goalID) {
			Cursor cursor = database.query(MySQLiteHelper.TAB_GOAL, goalColumns,
					MySQLiteHelper.COL_GOAL_ID + " = " + goalID, null, null, null, null);
			cursor.moveToFirst();
			Goal goal = cursorToGoal(cursor);
			cursor.close();
			
			List<ClaimLog> claims = getAllClaimLogs(goal.getTaskID(), goal.getDateStart(), goal.getDateEnd(), null, false);
			for(ClaimLog claim : claims) {
				goal.addBountyProgress(claim.getBounty());
				goal.addClaimDate(claim.getClaimDate());
			}
			
			return goal;
		}
		
		public List<Goal> getAllGoalsForTask(long taskID) {
			List<Goal> goalList = new ArrayList<Goal>();
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_GOAL, goalColumns, 
					MySQLiteHelper.COL_TASK_ID + " = " + taskID, null, null, null, 
					MySQLiteHelper.COL_GOAL_DATE_END + " desc");
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				Goal goal = cursorToGoal(cursor);
				
				List<ClaimLog> claims = getAllClaimLogs(goal.getTaskID(), goal.getDateStart(), goal.getDateEnd(), null, false);
				for(ClaimLog claim : claims) {
					goal.addBountyProgress(claim.getBounty());
					goal.addClaimDate(claim.getClaimDate());
				}
				
				goalList.add(goal);
				cursor.moveToNext();
			}
			cursor.close();
			
			return goalList;
		}
		
		/**
		 * 
		 * @param taskID
		 * @return goal or null if no current goal
		 */
		public Goal getCurrentGoalForTask(long taskID) {
			Goal goal;
			long currentTime = Calendar.getInstance().getTimeInMillis();
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_GOAL, goalColumns, 
					MySQLiteHelper.COL_TASK_ID + " = " + taskID + " AND " +
					MySQLiteHelper.COL_GOAL_DATE_END + " > " + currentTime, null, null, null, null);
			
			cursor.moveToFirst();
			if(!cursor.isAfterLast()) {
				goal = cursorToGoal(cursor);
				
				List<ClaimLog> claims = getAllClaimLogs(goal.getTaskID(), goal.getDateStart(), goal.getDateEnd(), null, false);
				for(ClaimLog claim : claims) {
					goal.addBountyProgress(claim.getBounty());
					goal.addClaimDate(claim.getClaimDate());
				}
			}
			else
				goal = null;
			
			cursor.close();
			return goal;
		}
		
		public List<Goal> getAllCurrentGoals() {
			List<Goal> goalList = getAllGoalsEndingBetween(Calendar.getInstance(), null);
			
			return goalList;
		}
		
		public List<NamedGoal> getAllCurrentGoalsNamed() {
			List<NamedGoal> namedGoalList = new ArrayList<NamedGoal>();
			List<Goal> goalList = getAllCurrentGoals();
			
			for(Goal goal : goalList) {
				String[] taskNameType = getTaskNameType(goal.getTaskID());
				NamedGoal namedGoal = new NamedGoal(goal, taskNameType[0], taskNameType[1]);
				namedGoalList.add(namedGoal);
			}
			
			return namedGoalList;
		}
		
		public List<Goal> getAllGoalsEndingBetween(Calendar afterThisDate, Calendar beforeThisDate) {
			List<Goal> goalList = new ArrayList<Goal>();
			
			String selection = "";
			if(afterThisDate != null)
				selection += MySQLiteHelper.COL_GOAL_DATE_END + " > " + afterThisDate.getTimeInMillis();
			if(afterThisDate != null && beforeThisDate != null)
				selection += " AND ";
			if(beforeThisDate != null)
				selection += MySQLiteHelper.COL_GOAL_DATE_END + " < " + beforeThisDate.getTimeInMillis();
			
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_GOAL, goalColumns, 
					selection, null, null, null, 
					MySQLiteHelper.COL_GOAL_DATE_END + " desc");
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				Goal goal = cursorToGoal(cursor);
				
				List<ClaimLog> claims = getAllClaimLogs(goal.getTaskID(), goal.getDateStart(), goal.getDateEnd(), null, false);
				for(ClaimLog claim : claims) {
					goal.addBountyProgress(claim.getBounty());
					goal.addClaimDate(claim.getClaimDate());
				}
				
				goalList.add(goal);
				cursor.moveToNext();
			}
			cursor.close();
			
			return goalList;
		}

		private Goal cursorToGoal(Cursor cursor) {
			Goal goal = new Goal();
			goal.setId(cursor.getLong(0));
			goal.setTaskID(cursor.getLong(1));
			goal.setBountyTarget(cursor.getFloat(2));
			goal.setDateStart(cursor.getLong(3));
			goal.setDateEnd(cursor.getLong(4));
			
			return goal;
		}
}
