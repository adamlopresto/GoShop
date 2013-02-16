package fake.domain.adamlopresto.goshop;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import fake.domain.adamlopresto.goshop.tables.AislesTable;
import fake.domain.adamlopresto.goshop.tables.ItemAisleDetailView;
import fake.domain.adamlopresto.goshop.tables.ItemAisleTable;
import fake.domain.adamlopresto.goshop.tables.ItemsTable;
import fake.domain.adamlopresto.goshop.tables.ListsTable;
import fake.domain.adamlopresto.goshop.tables.StoresTable;


public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "GoShop";
	private static final int CURRENT_VERSION = 2;
	/*
	 * Version history:
	 * 1: initial release
	 * 2: reset quantity to null if 1 and units = 'each'
	 */
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, CURRENT_VERSION);
	}

	@Override
	public void onOpen(SQLiteDatabase db){
		db.execSQL("PRAGMA foreign_keys = ON;");
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		ListsTable.onCreate(db);
		ItemsTable.onCreate(db);
		StoresTable.onCreate(db);
		AislesTable.onCreate(db);
		ItemAisleTable.onCreate(db);
		ItemAisleDetailView.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		ListsTable.onUpgrade(db, oldVersion, newVersion);
		ItemsTable.onUpgrade(db, oldVersion, newVersion);
		StoresTable.onUpgrade(db, oldVersion, newVersion);
		AislesTable.onUpgrade(db, oldVersion, newVersion);
		ItemAisleTable.onUpgrade(db, oldVersion, newVersion);
		ItemAisleDetailView.onUpgrade(db, oldVersion, newVersion);
	}
/*	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
		//noop
	}
*/

}
