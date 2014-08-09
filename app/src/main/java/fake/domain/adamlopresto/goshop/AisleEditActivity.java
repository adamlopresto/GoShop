package fake.domain.adamlopresto.goshop;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import fake.domain.adamlopresto.goshop.contentprovider.GoShopContentProvider;
import fake.domain.adamlopresto.goshop.tables.AislesTable;

public class AisleEditActivity extends Activity {
	
	private boolean cancel = false;
	
	private Uri uri;
	
	private EditText mName;
	private EditText mSort;
	private EditText mDesc;
	private long storeId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aisle_edit);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Check from the saved Instance
		uri = (savedInstanceState == null) ? null : (Uri) savedInstanceState
				.getParcelable(GoShopContentProvider.CONTENT_ITEM_TYPE);
		
		mName = (EditText) findViewById(R.id.item_detail_name);
		mSort = (EditText) findViewById(R.id.item_detail_sort);
		mDesc = (EditText) findViewById(R.id.item_detail_description);
	
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			uri = extras.getParcelable(GoShopContentProvider.CONTENT_ITEM_TYPE);

			if (uri != null) {
				fillData(uri);
			} else {
				storeId = extras.getLong("store");
			}
		}
	}
	
	private void fillData(Uri uri){
		String[] projection = { 
				AislesTable.COLUMN_NAME, AislesTable.COLUMN_SORT, 
				AislesTable.COLUMN_DESC, AislesTable.COLUMN_STORE 
		};
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();

			mName.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(AislesTable.COLUMN_NAME)));
			mSort.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(AislesTable.COLUMN_SORT)));
			mDesc.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(AislesTable.COLUMN_DESC)));
			storeId = cursor.getLong(cursor.getColumnIndexOrThrow(AislesTable.COLUMN_STORE));

			// Always close the cursor
			cursor.close();
		}
			
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_aisle_edit, menu);
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
			finish();
			return true;
		case R.id.menu_cancel:
			cancel = true;
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPause(){
		saveState();
		super.onPause();
	}
	
	private void saveState(){
		if (cancel) return;
		
		String name = mName.getText().toString();

		// Only save if there's a name
		if (TextUtils.isEmpty(mName.getText().toString())){
			return;
		}

		ContentValues values = new ContentValues();
		values.put(AislesTable.COLUMN_NAME, name.trim());
		values.put(AislesTable.COLUMN_SORT, mSort.getText().toString().trim());
		values.put(AislesTable.COLUMN_DESC, mDesc.getText().toString().trim());
		values.put(AislesTable.COLUMN_STORE, storeId);

		if (uri == null) {
			uri = getContentResolver().insert(GoShopContentProvider.AISLES_URI, values);

		} else {
			// Update todo
			getContentResolver().update(uri, values, null, null);
			//makeToast("Updated: "+todoUri.toString());
		}

	}

}
