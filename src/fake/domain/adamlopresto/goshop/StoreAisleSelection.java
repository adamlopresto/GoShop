package fake.domain.adamlopresto.goshop;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import fake.domain.adamlopresto.goshop.contentprovider.GoShopContentProvider;
import fake.domain.adamlopresto.goshop.tables.AislesTable;
import fake.domain.adamlopresto.goshop.tables.StoresTable;

public class StoreAisleSelection extends ListActivity 
implements LoaderManager.LoaderCallbacks<Cursor>{

	SimpleCursorAdapter storeAdapter;
	SimpleCursorAdapter aisleAdapter;
	
	private static final int STORE_LOADER = 0;
	private static final int AISLE_LOADER = 1;
	
	private long store = MainListActivity.store;
	private long aisle = -1;
	
	private static final int EDIT_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = EDIT_ID + 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_store_aisle_selection);
		// Show the Up button in the action bar.
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		String[] storeFrom = new String[]{StoresTable.COLUMN_NAME};
		int[] storeTo = new int[]{android.R.id.text1};
		storeAdapter = new SimpleCursorAdapter(ab.getThemedContext(), android.R.layout.simple_spinner_item, null, 
				storeFrom, storeTo, 0);	
		
		storeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		ab.setListNavigationCallbacks(storeAdapter, new ActionBar.OnNavigationListener() {
			
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				store = itemId;
				
				getLoaderManager().restartLoader(AISLE_LOADER, null, StoreAisleSelection.this);
				return true;
			}
		});
		
		String[] aisleFrom = new String[]{AislesTable.COLUMN_NAME, AislesTable.COLUMN_DESC};
		int[] aisleTo = new int[]{android.R.id.text1, android.R.id.text2};
		aisleAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_activated_2, null, 
				aisleFrom, aisleTo, 0);	
		getListView().setAdapter(aisleAdapter);
		
		getLoaderManager().initLoader(STORE_LOADER, null, this);
		
		getListView().setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				aisle = id;
				setResult(RESULT_OK, new Intent().putExtra("aisle", aisle));
			}
		});
		
		findViewById(R.id.new_aisle).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				newAisle();
			}
		});
		
		registerForContextMenu(getListView());
		
	}
	
	private void newAisle(){
		Intent i = new Intent(this, AisleEditActivity.class);
		i.putExtra("store", store);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_store_aisle_selection, menu);
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
			//NavUtils.navigateUpFromSameTask(this);
			finish();
			return true;
		case R.id.menu_cancel:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		getLoaderManager().restartLoader(AISLE_LOADER, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch(id){
		case STORE_LOADER:
			return new CursorLoader(this, GoShopContentProvider.STORES_URI,
					new String[]{StoresTable.COLUMN_ID, StoresTable.COLUMN_NAME}, 
					null, null, StoresTable.COLUMN_NAME);
		case AISLE_LOADER:
			return new CursorLoader(this, GoShopContentProvider.AISLES_URI,
					new String[]{AislesTable.COLUMN_ID, AislesTable.COLUMN_NAME, AislesTable.COLUMN_DESC},
					AislesTable.COLUMN_STORE+"=?", new String[]{String.valueOf(store)}, 
					AislesTable.COLUMN_SORT);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		switch(loader.getId()){
		case STORE_LOADER:
			storeAdapter.swapCursor(c);
			c.moveToFirst();
			int rowCount = c.getCount();
			for (int i = 0; i<rowCount && !c.isAfterLast(); ++i){
				if(c.getLong(0)==store){
					getActionBar().setSelectedNavigationItem(i);
					return;
				}
				c.moveToNext();
			}
			break;
		case AISLE_LOADER:
			aisleAdapter.swapCursor(c);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch(loader.getId()){
		case STORE_LOADER:
			storeAdapter.swapCursor(null);
			break;
		case AISLE_LOADER:
			aisleAdapter.swapCursor(null);
			break;
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, EDIT_ID, 0, R.string.menu_edit);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		long id = info.id;
		Uri uri = Uri.parse(GoShopContentProvider.AISLES_URI + "/"+ id);

		switch (item.getItemId()) {
		case EDIT_ID:
			startActivity(new Intent(this, AisleEditActivity.class)
					.putExtra(GoShopContentProvider.CONTENT_ITEM_TYPE, uri));
			return true;
		case DELETE_ID:
			getContentResolver().delete(uri, null, null);
			//fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}
}
