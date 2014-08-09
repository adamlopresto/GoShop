package fake.domain.adamlopresto.goshop.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class ListsTable {
	public static final String TABLE = "lists";
	
	//as of version 1
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "list_name";
	
	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+TABLE
				+ "(" 
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_NAME + " text not null unique) "
				);
		
		ContentValues cv = new ContentValues(1);
		cv.put(COLUMN_NAME, "Groceries");
		db.insert(TABLE, null, cv);
	}
	
	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		//intentionally blank
	}
}
