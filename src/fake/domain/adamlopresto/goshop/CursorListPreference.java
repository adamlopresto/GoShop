/*
 * Adapted from http://jfd-android.blogspot.com/2011/05/listpreference-using-sql-cursor.html
 */
package fake.domain.adamlopresto.goshop;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class CursorListPreference extends ListPreference {
	
	DatabaseHelper helper;

	public CursorListPreference(Context context) {
		super(context);
		helper = new DatabaseHelper(context);
	}

	public CursorListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		helper = new DatabaseHelper(context);
	}

	public void setData(String inTableName, String inColumnName, boolean includeAll) {
		
	    SQLiteDatabase db = helper.getReadableDatabase();
		 
	    CharSequence[] lEntries;
	    CharSequence[] lEntryValues;
	     
	    Cursor lCursor = db.query(
	        inTableName, //Table to select from
	        new String[]{"_id", inColumnName}, //Column to retrieve
	        null, null, null, null,
	        inColumnName); //Sorting
	    if ((lCursor.getCount() == 0) || !lCursor.moveToFirst()) {
	    	if (includeAll){
	    		lEntries = new CharSequence[]{"All"};
	    		lEntryValues = new CharSequence[]{"-1"};
	    	} else {
	    		lEntries = new CharSequence[]{};
	    		lEntryValues = new CharSequence[]{};
	    	}
	        //Logger.i("No entry found for " + inColumnName + " in table " + inTableName);
	    } else {
	        lCursor.moveToFirst();
	        int i = 0;
	        if (includeAll){
	        	lEntries = new CharSequence[lCursor.getCount()+1];
	        	lEntryValues = new CharSequence[lCursor.getCount()+1];
	        	lEntries[0]="All";
	        	lEntryValues[0]="-1";
	        	i=1;
	        } else {
	        	lEntries = new CharSequence[lCursor.getCount()];
	        	lEntryValues = new CharSequence[lCursor.getCount()];
	        }
	        do {
	            lEntries[i] = lCursor.getString(lCursor.getColumnIndex(inColumnName));
	            lEntryValues[i] = lCursor.getString(lCursor.getColumnIndex("_id"));
	            ++i;
	        } while (lCursor.moveToNext());
	    }
	    lCursor.close();
	     
	    this.setEntries(lEntries);
	    this.setEntryValues(lEntryValues);
	}
}
