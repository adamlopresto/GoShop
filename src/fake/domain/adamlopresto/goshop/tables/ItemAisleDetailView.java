package fake.domain.adamlopresto.goshop.tables;

import java.util.Arrays;
import java.util.HashSet;

import android.database.sqlite.SQLiteDatabase;

public class ItemAisleDetailView {

	public static final String VIEW = "item_aisle_detail";

	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ITEM = "item";   //item id
	public static final String COLUMN_ITEM_NAME = "item_name";
	public static final String COLUMN_LIST = "list";
	public static final String COLUMN_QUANTITY = "quantity";
	public static final String COLUMN_UNITS = "units";
	public static final String COLUMN_NOTES = "notes";
	public static final String COLUMN_PRICE = "price";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_CATEGORY = "category";
	public static final String COLUMN_AISLE = "aisle"; //aisle id
	public static final String COLUMN_STORE = "store";
	public static final String COLUMN_STORE_NAME = "store_name";
	public static final String COLUMN_AISLE_NAME = "aisle_name"; //
	public static final String COLUMN_DESC = "description"; 
	public static final String COLUMN_SORT = "sort";	

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE VIEW "+VIEW
				+ " AS SELECT " 
				+ ItemAisleTable.TABLE + "."+ ItemAisleTable.COLUMN_ID + " AS " + COLUMN_ID+", "
				+ COLUMN_ITEM + ", "
				+ ItemsTable.TABLE + "." + ItemsTable.COLUMN_NAME + " AS " + COLUMN_ITEM_NAME + ", "
				+ ItemsTable.TABLE + "." + ItemsTable.COLUMN_LIST + " AS " + COLUMN_LIST + ", "
				+ COLUMN_QUANTITY + ", "
				+ COLUMN_UNITS + ", "
				+ COLUMN_NOTES + ", "
				+ COLUMN_PRICE + ", "
				+ COLUMN_STATUS + ", "
				+ COLUMN_CATEGORY + ", "
				+ COLUMN_AISLE + ", "
				+ COLUMN_STORE + ", "
				+ StoresTable.TABLE + "." + StoresTable.COLUMN_NAME + " AS "+COLUMN_STORE_NAME + ", "
				+ AislesTable.TABLE + "." + AislesTable.COLUMN_NAME + " AS "+COLUMN_AISLE_NAME + ", "
				+ COLUMN_DESC + ", "
				+ COLUMN_SORT
				+ " FROM " + ItemsTable.TABLE + " INNER JOIN " + ItemAisleTable.TABLE 
				+ " ON " + ItemsTable.TABLE + "." + ItemsTable.COLUMN_ID + "=" + ItemAisleTable.COLUMN_ITEM
				+ " INNER JOIN " + AislesTable.TABLE 
				+ " ON " + ItemAisleTable.COLUMN_AISLE + "=" + AislesTable.TABLE +"."+AislesTable.COLUMN_ID
				+ " INNER JOIN " + StoresTable.TABLE 
				+ " ON " + AislesTable.COLUMN_STORE + "=" + StoresTable.TABLE+"."+StoresTable.COLUMN_ID
				);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		/*
		db.execSQL("DROP VIEW "+VIEW);
		onCreate(db);
		*/
	}

	public static void checkColumns(String[] projection) {
		String[] available = {
				COLUMN_ID,
				COLUMN_ITEM,
				COLUMN_ITEM_NAME,
				COLUMN_LIST,
				COLUMN_QUANTITY,
				COLUMN_UNITS,
				COLUMN_NOTES,
				COLUMN_PRICE,
				COLUMN_STATUS,
				COLUMN_CATEGORY,
				COLUMN_AISLE,
				COLUMN_STORE,
				COLUMN_STORE_NAME,
				COLUMN_AISLE_NAME,
				COLUMN_DESC,
				COLUMN_SORT,
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

