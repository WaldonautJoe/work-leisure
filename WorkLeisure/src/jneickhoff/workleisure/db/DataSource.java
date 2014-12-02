package jneickhoff.workleisure.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
		
		private String[] claimLogCollumns = {
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
				String importance, boolean isDue, Date dateDue) {
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
				values.put(MySQLiteHelper.COL_TASK_DATE_DUE, dateDue.getTime());
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
			values.put(MySQLiteHelper.COL_TASK_DATE_UPDATED, task.getDateUpdated().getTime());
			if(task.isDue()) {
				values.put(MySQLiteHelper.COL_TASK_IS_DUE, MySQLiteHelper.SQLITE_TRUE);
				values.put(MySQLiteHelper.COL_TASK_DATE_DUE, task.getDateDue().getTime());
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
				tasks.add(task);
				cursor.moveToNext();
			}
			cursor.close();
			
			return tasks;
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
				selection = MySQLiteHelper.COL_TASK_DATE_DUE + " > ?"
						+ " AND " + MySQLiteHelper.COL_TASK_DATE_DUE + " < ?";
				selectionArgs = new String[2];
				selectionArgs[0] = String.valueOf(tasksAfterDate.getTimeInMillis());
				selectionArgs[1] = String.valueOf(tasksBeforeDate.getTimeInMillis());
			}
			else if(tasksAfterDate != null) {
				selection = MySQLiteHelper.COL_TASK_DATE_DUE + " > ?";
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
				tasksDue.add(cursorToTask(cursor));
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
			task.setDateUpdated(new Date(cursor.getLong(10)));
			if(cursor.getLong(11) == MySQLiteHelper.SQLITE_TRUE) {
				task.setDue(true);
				task.setDateDue(new Date(cursor.getLong(12)));
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
		public ClaimLog createClaimLog(Date date, long taskID, String taskType, String comment, 
				float bounty, float updatedBalance, float bountyDeviation, Long dueDifference){
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COL_CLAIM_DATE, date.getTime());
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
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogCollumns,
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
			values.put(MySQLiteHelper.COL_CLAIM_DATE, claimLog.getClaimDate().getTime());
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
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogCollumns, 
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
		 * @param date oldest age of claims to return
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
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogCollumns, 
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
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogCollumns, 
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
			
			Cursor cursor = database.query(MySQLiteHelper.TAB_CLAIM_LOG, claimLogCollumns, 
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
			claimLog.setClaimDate(new Date(cursor.getLong(1)));
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
}