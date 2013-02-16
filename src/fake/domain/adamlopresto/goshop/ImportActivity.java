package fake.domain.adamlopresto.goshop;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;
import fake.domain.adamlopresto.goshop.tables.AislesTable;
import fake.domain.adamlopresto.goshop.tables.ItemAisleTable;
import fake.domain.adamlopresto.goshop.tables.ItemsTable;
import fake.domain.adamlopresto.goshop.tables.StoresTable;

public class ImportActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		new ImportTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_import, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class ImportTask extends AsyncTask<Void,String,String>{
		private SQLiteDatabase db;

		@Override
		protected String doInBackground(Void... ignored) {
			publishProgress("Initializing");
			
			int n = 0;
			
			File directory = Environment.getExternalStorageDirectory();
			// Assumes that a file article.rss is available on the SD card
			File file = new File(directory + "/ToMarket.csv");
			if (!file.exists()) {
				throw new RuntimeException("File not found");
			}
			Log.e("Testing", "Starting to read");
			CSVReader reader = null;
			try {
				reader = new CSVReader(new FileReader(file), ',', '"', 1);
				String[] line;
				db = new DatabaseHelper(getApplicationContext()).getWritableDatabase();
				ContentValues cv = new ContentValues();
				while ((line = reader.readNext()) != null && !isCancelled()) {
					cv.clear();
					long listId = 1L; //TODO: Generalize
					String itemName;
					cv.put(ItemsTable.COLUMN_LIST, listId); 
					cv.put(ItemsTable.COLUMN_NAME, itemName=fixCommas(line[1]));
					cv.put(ItemsTable.COLUMN_CATEGORY, line[2]);
					cv.put(ItemsTable.COLUMN_STATUS, "H");
					cv.put(ItemsTable.COLUMN_NOTES, fixCommas(line[5]));
					cv.put(ItemsTable.COLUMN_QUANTITY, 
							"0".equals(line[8])
							? "1" 
							: line[8]);
					cv.put(ItemsTable.COLUMN_UNITS, line[17]);
					
					long item = db.insertOrThrow(ItemsTable.TABLE, null, cv);
					
					publishProgress("Imported item #"+(++n)+" "+line[1]);
					
					cv.clear();
					cv.put(ItemAisleTable.COLUMN_ITEM, item);
					for (String storeLine : line[3].split(";")){
						String[] storeElems = storeLine.split("\\[|\\]",3);
						String store = storeElems[0];
						String aisle = storeElems[1];
						if (!"All".equals(store)){
							long storeId = getStore(listId, store);
							long aisleId = getAisle(storeId, aisle, itemName);
							
							cv.put(ItemAisleTable.COLUMN_AISLE, aisleId);
							
							db.insertOrThrow(ItemAisleTable.TABLE, null, cv);
							
						}
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return "Import complete. Imported "+n+" items";
		}
		
		private String fixCommas(String in){
			return in.replaceAll("@comma@", ",");
		}
		
		private long getStore(long listId, String store){
			
			Cursor c = db.query(StoresTable.TABLE, new String[]{StoresTable.COLUMN_ID}, StoresTable.COLUMN_LIST + "=? AND "+StoresTable.COLUMN_NAME+"=?", 
					new String[]{String.valueOf(listId), store}, null, null, null);
			if (c.moveToFirst()){
				return c.getLong(c.getColumnIndexOrThrow(StoresTable.COLUMN_ID));
			}
			c.close();
			
			ContentValues cv = new ContentValues();
			cv.put(StoresTable.COLUMN_LIST, listId);
			cv.put(StoresTable.COLUMN_NAME, store);
			return db.insertOrThrow(StoresTable.TABLE, null, cv);
		}
		
		private long getAisle(long storeId, String aisle, String item){
			if ("".equals(aisle)){
				aisle = "Unfiled";
			}
			Cursor c = db.query(AislesTable.TABLE, 
					new String[]{AislesTable.COLUMN_ID}, 
					AislesTable.COLUMN_STORE+"=? AND "+AislesTable.COLUMN_NAME+"=?", 
					new String[]{String.valueOf(storeId), aisle}, 
					null, null, null);
			if (c.moveToFirst()){
				return c.getLong(c.getColumnIndexOrThrow(AislesTable.COLUMN_ID));
			}
			c.close();
			
			ContentValues cv = new ContentValues();
			cv.put(AislesTable.COLUMN_STORE, storeId);
			cv.put(AislesTable.COLUMN_NAME, aisle);
			cv.put(AislesTable.COLUMN_DESC, item);
			try {
				Long sort = Long.valueOf(aisle);
				cv.put(AislesTable.COLUMN_SORT, String.format(Locale.US, "%02d5", sort));
				
			} catch (NumberFormatException e){
				if (aisle.equals("Unfiled")){
					cv.put(AislesTable.COLUMN_SORT, "000");
				} else {
					cv.put(AislesTable.COLUMN_SORT, "005");
				}
			}
			return db.insertOrThrow(AislesTable.TABLE, null, cv);
		}
		
		@Override
		protected void onProgressUpdate(String... status){
			((TextView)findViewById(R.id.import_status)).setText(status[0]);
		}
		
		@Override
		protected void onPostExecute(String status){
			//((TextView)findViewById(R.id.import_status)).setText(status);
		}
		
	}
}
