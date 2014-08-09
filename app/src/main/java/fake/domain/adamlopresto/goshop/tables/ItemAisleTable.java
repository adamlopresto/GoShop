package fake.domain.adamlopresto.goshop.tables;

import java.util.Arrays;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

public class ItemAisleTable {

	public static final String TABLE = "item_aisle";

	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ITEM = "item";   //item id
	public static final String COLUMN_AISLE = "aisle"; //aisle id
	
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+TABLE
				+ "(" 
				+ COLUMN_ID    + " integer primary key autoincrement, "
				+ COLUMN_ITEM + " integer not null references "+ItemsTable.TABLE+" ON DELETE CASCADE, "
				+ COLUMN_AISLE  + " integer not null references "+AislesTable.TABLE
				+ " ON DELETE CASCADE, UNIQUE ("+COLUMN_ITEM+","+COLUMN_AISLE+"))"
		);
		
		db.execSQL("CREATE INDEX itemaisles_item ON "+TABLE+" ("+COLUMN_ITEM+")");
		db.execSQL("CREATE INDEX itemaisles_aisle ON "+TABLE+" ("+COLUMN_AISLE+")");
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		String[] available = { COLUMN_ID, COLUMN_ITEM, COLUMN_AISLE};
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

