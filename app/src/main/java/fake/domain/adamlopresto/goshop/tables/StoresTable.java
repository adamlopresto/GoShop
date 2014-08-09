package fake.domain.adamlopresto.goshop.tables;

import java.util.Arrays;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

public class StoresTable {
	public static final String TABLE = "stores";

	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LIST = "list";
	public static final String COLUMN_NAME = "store_name";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+TABLE
				+ "(" 
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_LIST + " integer not null references "+ListsTable.TABLE+" ON DELETE CASCADE, "
				+ COLUMN_NAME + " text not null collate nocase"
				+ ", unique ("+COLUMN_LIST+", "+COLUMN_NAME+"))"
		);
		
		db.execSQL("CREATE INDEX stores_list ON "+TABLE+" ("+COLUMN_LIST+")");
		db.execSQL("CREATE INDEX stores_name ON "+TABLE+" ("+COLUMN_NAME+")");
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}

	public static void checkColumns(String[] projection) {
		String[] available = { COLUMN_ID, COLUMN_LIST, COLUMN_NAME
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
