package fake.domain.adamlopresto.goshop.tables;

import java.util.Arrays;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

public class AislesTable {
	public static final String TABLE = "aisles";

	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_STORE = "store";
	
	/* 
	 * Either the aisle number or the very short name of the aisle for unnumbered aisles.
	 */
	public static final String COLUMN_NAME = "aisle_name"; //
	
	/*
	 * For numbered aisles, a brief description of the contents of the aisle. For unnumbered aisles, can be a location,
	 * like "along the back wall"
	 */
	public static final String COLUMN_DESC = "description"; 
	
	/*
	 * 3 digit sort code, to sort asciibetically.
	 * Aisle 16 is 165 (middle of the 16x range). Before the first
	 * numbered aisle is 00x (produce, etc). After the last is 99x.
	 * "Unfiled" is 000.
	 */
	public static final String COLUMN_SORT = "sort";	

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+TABLE
				+ "(" 
				+ COLUMN_ID    + " integer primary key autoincrement, "
				+ COLUMN_STORE + " integer not null references "+StoresTable.TABLE+" ON DELETE CASCADE, "
				+ COLUMN_NAME  + " text not null collate nocase, "
				+ COLUMN_DESC  + " text, "
				+ COLUMN_SORT  + " text collate nocase, "
				+ " UNIQUE ("+COLUMN_STORE+", "+COLUMN_NAME+")"
				+ ")"
		);
		
		db.execSQL("CREATE INDEX aisles_list ON "+TABLE+"("+COLUMN_STORE+")");
		db.execSQL("CREATE INDEX aisles_sort ON "+TABLE+"("+COLUMN_SORT+")");
		db.execSQL("CREATE INDEX aisles_name ON "+TABLE+"("+COLUMN_NAME+")");
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		//TODO: fill this in
	}

	public static void checkColumns(String[] projection) {
		String[] available = { COLUMN_ID, COLUMN_STORE, COLUMN_NAME, COLUMN_DESC, COLUMN_SORT
		};
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
}
