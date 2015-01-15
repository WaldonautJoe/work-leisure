package jneickhoff.workleisure.db;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import jneickhoff.workleisure.R;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {
	
	public static final String TAB_TASK = "Tasks";
	public static final String COL_TASK_ID = "TaskID";
	public static final String COL_TASK_NAME = "TaskName";
	public static final String COL_TASK_TYPE = "TaskType";
	public static final String COL_TASK_DESC = "TaskDesc";
	public static final String COL_TASK_BOUNTY = "TaskBounty";
	public static final String COL_TASK_STOCK_TYPE = "TaskStockType";
	public static final String COL_TASK_STOCK_NUMBER = "TaskStockNumber";
	public static final String COL_TASK_TIMES_CLAIMED = "TaskTimesClaimed";
	
	public static final String COL_TASK_IS_ARCHIVED = "TaskIsArchived";
	public static final String COL_TASK_IMPORTANCE = "TaskImportance";
	public static final String COL_TASK_DATE_UPDATED = "TaskDateUpdated";
	public static final String COL_TASK_IS_DUE = "TaskIsDue";
	public static final String COL_TASK_DATE_DUE = "TaskDateDue";
	
	public static final String TAB_CLAIM_LOG = "ClaimLogs";
	public static final String COL_CLAIM_ID = "ClaimID";
	public static final String COL_CLAIM_DATE = "ClaimDate";
	public static final String COL_CLAIM_COMMENT = "ClaimComment";
	public static final String COL_CLAIM_BOUNTY = "ClaimBounty";
	public static final String COL_CLAIM_UPDATED_BALANCE = "ClaimUpdatedBalance";
	public static final String COL_CLAIM_BOUNTY_DEVIATION = "ClaimBountyDeviation";
	public static final String COL_CLAIM_DUE_DIFFERENCE = "ClaimDueDifference";
	
	public static final String TAB_GOAL = "Goal";
	public static final String COL_GOAL_ID = "GoalID";
	public static final String COL_GOAL_BOUNTY_TARGET = "GoalBountyTarget";
	public static final String COL_GOAL_DATE_START = "GoalDateStart";
	public static final String COL_GOAL_DATE_END = "GoalDateEnd";
	
	public static final int SQLITE_TRUE = 1;
	public static final int SQLITE_FALSE = 0;
	
	private static final String DATABASE_NAME = "workleisure.db";
	private static final int DATABASE_VERSION = 8;
	
	private Context context;
	
	private static final String CREATE_TAB_TASK = "create table " + TAB_TASK + "("
			+ COL_TASK_ID + " integer primary key autoincrement, "
			+ COL_TASK_NAME + " text not null, "
			+ COL_TASK_TYPE + " text not null, "
			+ COL_TASK_DESC + " text, "
			+ COL_TASK_BOUNTY + " real, "
			+ COL_TASK_STOCK_TYPE + " text, "
			+ COL_TASK_STOCK_NUMBER + " integer, "
			+ COL_TASK_TIMES_CLAIMED + " integer, "
			+ COL_TASK_IS_ARCHIVED + " integer, "
			+ COL_TASK_IMPORTANCE + " text, "
			+ COL_TASK_DATE_UPDATED + " integer, "
			+ COL_TASK_IS_DUE + " integer, "
			+ COL_TASK_DATE_DUE + " integer);";
	
	private static final String CREATE_TAB_CLAIM_LOG = "create table " + TAB_CLAIM_LOG + "("
			+ COL_CLAIM_ID + " integer primary key autoincrement, "
			+ COL_CLAIM_DATE + " integer, "
			+ COL_TASK_ID + " integer, "
			+ COL_TASK_STOCK_TYPE + " text, "
			+ COL_CLAIM_COMMENT + " text, "
			+ COL_CLAIM_BOUNTY + " real, "
			+ COL_CLAIM_UPDATED_BALANCE + " real, "
			+ COL_CLAIM_BOUNTY_DEVIATION + " real, "
			+ COL_CLAIM_DUE_DIFFERENCE + " integer);";
	
	private static final String CREATE_TAB_GOAL = "create table " + TAB_GOAL + "("
			+ COL_GOAL_ID + " integer primary key autoincrement, "
			+ COL_TASK_ID + " integer, "
			+ COL_GOAL_BOUNTY_TARGET + " real, "
			+ COL_GOAL_DATE_START + " integer, "
			+ COL_GOAL_DATE_END + " integer);";
	
	public MySQLiteHelper (Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TAB_TASK);
		database.execSQL(CREATE_TAB_CLAIM_LOG);
		database.execSQL(CREATE_TAB_GOAL);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Calendar c = Calendar.getInstance();
		
		switch(oldVersion) {
		case 1:
			db.execSQL("alter table " + TAB_TASK + " ADD COLUMN " 
					+ COL_TASK_IS_ARCHIVED + " integer;");
			db.execSQL("alter table " + TAB_TASK + " ADD COLUMN " 
					+ COL_TASK_IMPORTANCE + " text;");
			db.execSQL("alter table " + TAB_TASK + " ADD COLUMN "
					+ COL_TASK_DATE_UPDATED + " integer;");
			db.execSQL("alter table " + TAB_TASK + " ADD COLUMN "
					+ COL_TASK_IS_DUE + " integer;");
			db.execSQL("alter table " + TAB_TASK + " ADD COLUMN "
					+ COL_TASK_DATE_DUE + " integer;");
			
			ContentValues values1 = new ContentValues();
			values1.put(COL_TASK_IS_ARCHIVED, SQLITE_FALSE);
			values1.put(COL_TASK_IMPORTANCE, Task.IMPORTANCE_LOW);
			values1.put(COL_TASK_DATE_UPDATED, c.getTimeInMillis());
			values1.put(COL_TASK_IS_DUE, SQLITE_FALSE);
			values1.put(COL_TASK_DATE_DUE, c.getTimeInMillis());
			db.update(TAB_TASK, values1, null, null);
			
			db.execSQL(CREATE_TAB_CLAIM_LOG);
		
		case 2:
		case 3:
		case 4:
		case 5:
			db.execSQL("alter table " + TAB_CLAIM_LOG + " ADD COLUMN "
					+ COL_CLAIM_UPDATED_BALANCE + " float;");
			db.execSQL("alter table " + TAB_CLAIM_LOG + " ADD COLUMN "
					+ COL_TASK_TYPE + " text;");
			
			SharedPreferences sharedPref = context.getSharedPreferences(
					context.getString(R.string.user_balances), Context.MODE_PRIVATE);
			float balance = sharedPref.getFloat(context.getString(R.string.default_balance), 0f);
			
			Map<Long, String> idTypePair = new HashMap<Long, String>();
			String[] pairColumns = {COL_TASK_ID, COL_TASK_TYPE};
			Cursor curTaskTypes = db.query(MySQLiteHelper.TAB_TASK, pairColumns, 
					null, null, null, null, null);
			curTaskTypes.moveToFirst();
			while(!curTaskTypes.isAfterLast()) {
				idTypePair.put(curTaskTypes.getLong(0), curTaskTypes.getString(1));
				curTaskTypes.moveToNext();
			}
			curTaskTypes.close();
			
			for(long taskID : idTypePair.keySet()) {
				ContentValues balanceTypeValues = new ContentValues();
				balanceTypeValues.put(COL_CLAIM_UPDATED_BALANCE, balance);
				balanceTypeValues.put(COL_TASK_TYPE, idTypePair.get(taskID));
				db.update(TAB_CLAIM_LOG, balanceTypeValues, COL_TASK_ID + " = " + taskID, null);
			}
			
		case 6:
			ContentValues dueDifferenceValues = new ContentValues();
			dueDifferenceValues.putNull(COL_CLAIM_DUE_DIFFERENCE);
			db.update(TAB_CLAIM_LOG, dueDifferenceValues, COL_CLAIM_DUE_DIFFERENCE + " = 0", null);
			
		case 7:
			db.execSQL(CREATE_TAB_GOAL);
			
			break;
		}
		
	}
			
}
