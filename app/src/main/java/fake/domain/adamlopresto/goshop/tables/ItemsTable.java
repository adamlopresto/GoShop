package fake.domain.adamlopresto.goshop.tables;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class ItemsTable {
	public static final String TABLE = "items";

	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LIST = "list";
	public static final String COLUMN_NAME = "item_name";
	public static final String COLUMN_QUANTITY = "quantity";
	public static final String COLUMN_UNITS = "units";
	public static final String COLUMN_NOTES = "notes";
	public static final String COLUMN_PRICE = "price";
	public static final String COLUMN_STATUS = "status";
	//status is "H" (have), "N" (need), "C" (cart)
	public static final String COLUMN_CATEGORY = "category";
	//as of version 3
	//delimited by '#'
	public static final String COLUMN_VOICE_NAMES = "voice_names";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "+TABLE
				+ "(" 
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_LIST + " integer not null references "+ListsTable.TABLE+", "
				+ COLUMN_NAME + " text not null unique collate nocase, "
				+ COLUMN_QUANTITY + " real, "
				+ COLUMN_UNITS + " text, "
				+ COLUMN_NOTES + " text, "
				+ COLUMN_PRICE + " real, "
				+ COLUMN_STATUS + " text, " 
				+ COLUMN_CATEGORY + " text collate nocase, "
				+ COLUMN_VOICE_NAMES + " text collate nocase"
				+ ")"
		);
		
		db.execSQL("CREATE INDEX item_list ON "+TABLE+" ("+COLUMN_LIST+")");
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		if (oldVersion == 1){
			ContentValues values = new ContentValues(1);
			values.putNull(COLUMN_QUANTITY);
			db.update(TABLE, values, COLUMN_QUANTITY + "=1.0 AND "+COLUMN_UNITS+"='each'", null);
		}
		if (oldVersion < 3){
			db.execSQL("ALTER TABLE "+TABLE+" ADD "+COLUMN_VOICE_NAMES+" text collate nocase");
		}
	}

	public static void checkColumns(String[] projection) {
		String[] available = { COLUMN_ID, COLUMN_LIST, COLUMN_NAME,
				COLUMN_QUANTITY, COLUMN_UNITS, COLUMN_NOTES, COLUMN_PRICE, COLUMN_STATUS,
				COLUMN_CATEGORY
		};
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			requestedColumns.removeAll(availableColumns);
			// Check if all columns which are requested are available
			if (!requestedColumns.isEmpty()) {
				throw new IllegalArgumentException("Unknown columns in projection: "+requestedColumns);
			}
		}
	}
}
