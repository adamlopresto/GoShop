package fake.domain.adamlopresto.goshop;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

import fake.domain.adamlopresto.goshop.contentprovider.GoShopContentProvider;
import fake.domain.adamlopresto.goshop.tables.ItemAisleDetailView;
import fake.domain.adamlopresto.goshop.tables.ItemsTable;
import fake.domain.adamlopresto.goshop.tables.StoresTable;

public class MainListActivity extends ListActivity 
	implements LoaderManager.LoaderCallbacks<Cursor>,
		SharedPreferences.OnSharedPreferenceChangeListener,
		SearchView.OnQueryTextListener, OnActionExpandListener, OnNavigationListener
{

	private AbsListView.MultiChoiceModeListener mActionModeCallback = new AbsListView.MultiChoiceModeListener() {
		private MenuItem editItem;
	
		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			final int checkedCount = getListView().getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    //mode.setSubtitle(null);
                    break;
                case 1:
                    //mode.setSubtitle("One item selected");
                    editItem.setVisible(true);
                    break;
                default:
                	editItem.setVisible(false);
                    //mode.setSubtitle("" + checkedCount + " items selected");
                    break;
            }
		}
			
	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.context_edit_share_delete, menu);
	        editItem = menu.findItem(R.id.edit);
	        //mode.setTitle("Tasks");
	        return true;
	    }

	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
	    // may be called multiple times if the mode is invalidated.
	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; // Return false if nothing is done
	    }

	    // Called when the user selects a contextual menu item
	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.edit:{
	            	long tempId = getListView().getCheckedItemIds()[0];
	            	/*
	            	if (oneStore){
	            		Cursor c = getContentResolver().query(Uri.parse(GoShopContentProvider.ITEM_AISLE_URI+"/"+tempId), new String[]{ItemAisleDetailView.COLUMN_ITEM}, null, null, null);
	            		c.moveToFirst();
	            		tempId = c.getLong(0);
	            		c.close();
	            	}
	            	*/
	            	final long id = tempId;
	                mode.finish(); // Action picked, so close the CAB
	                Intent i = new Intent(MainListActivity.this, ItemDetailActivity.class);
	                Uri uri = Uri.parse(GoShopContentProvider.ITEM_URI + "/"+ id);
	                i.putExtra(GoShopContentProvider.CONTENT_ITEM_TYPE, uri);
	                startActivity(i);
	                return true;

	            }
	            case R.id.send:{
	            	ListView lv = getListView();
	            	SparseBooleanArray checked = lv.getCheckedItemPositions();
	            	Cursor c = adapter.getCursor();
	            	StringBuffer sb = new StringBuffer("GoShop:");
	            	for (int i = 0 ; i < checked.size() ; i++){
	            		int pos = checked.keyAt(i);
	            		c.moveToPosition(pos);
	            		sb.append('\n');
	            		sb.append(c.getString(c.getColumnIndex("item_name")));
	            	}
	            	sendSMS(sb.toString());
	            	return true;
	            }
	            case R.id.delete:{
	            	final Uri uri = GoShopContentProvider.ITEM_URI;
	            	final long[] ids = getListView().getCheckedItemIds();
	            	new AlertDialog.Builder(MainListActivity.this)
	            	.setMessage("Delete these items?")
	            	.setNegativeButton(android.R.string.cancel, null)
	            	.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ContentResolver res = getContentResolver();
							String where = ItemsTable.COLUMN_ID + "=?";
							String[] idArray = new String[1];
							
							for (long id : ids){
								idArray[0] = String.valueOf(id);
								res.delete(uri, where, idArray);
							}
							getLoaderManager().restartLoader(0, null, MainListActivity.this);
						}
	            		
	            	}).show();
	            	
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            }
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	    }
	};
	

	//private static final int EDIT_ID = Menu.FIRST + 1;
	//private static final int DELETE_ID = EDIT_ID + 1;
	private SharedPreferences prefs;
	
	private boolean showAll;
	private boolean oneStore;
	private String query;
	
	public static long list;
	public static long store;
	private ItemCursorAdapter adapter;
	private SimpleCursorAdapter spinnerAdapter;
	
	private static final int ITEM_LOADER = 0;
	private static final int STORE_LOADER = 1;
	
	private MenuItem newMenuItem;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);

		getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(mActionModeCallback);
		
		//getListView().setDividerHeight(2);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		showAll = prefs.getBoolean(SettingsFragment.PREF_SHOW_ALL, false);
		list = Long.valueOf(prefs.getString(SettingsFragment.PREF_LIST, "1"));
		store = Long.valueOf(prefs.getString(SettingsFragment.PREF_STORE, "-1"));
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		spinnerAdapter = new SimpleCursorAdapter(actionBar.getThemedContext(), android.R.layout.simple_spinner_item, 
				null,
				new String[]{StoresTable.COLUMN_NAME}, new int[]{android.R.id.text1}, 0);
		spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		
		actionBar.setListNavigationCallbacks(spinnerAdapter, this);
		
		createAdapter();
		handleIntent(getIntent());

		//registerForContextMenu(getListView());
		
		getLoaderManager().restartLoader(STORE_LOADER, null, this);
	}
	
	private void createAdapter(){
		String[] from = new String[] {ItemAisleDetailView.COLUMN_ITEM_NAME, ItemAisleDetailView.COLUMN_PRICE, ItemAisleDetailView.COLUMN_QUANTITY, ItemAisleDetailView.COLUMN_UNITS, ItemAisleDetailView.COLUMN_NOTES, ItemAisleDetailView.COLUMN_STATUS, ItemAisleDetailView.COLUMN_AISLE_NAME, ItemAisleDetailView.COLUMN_CATEGORY};;
		int[] to = new int[] {R.id.row_item_name,R.id.row_item_price, R.id.row_item_quantity, R.id.row_item_units, R.id.row_item_notes, R.id.row_item_status, R.id.row_item_aisle, R.id.row_item_category};
		
		adapter = new ItemCursorAdapter(this, R.layout.main_row, null, from,
				to, 0);
		
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				switch(columnIndex){
				case 2:
					if (cursor.isNull(columnIndex))
						view.setVisibility(View.GONE);
					else	{
						view.setVisibility(View.VISIBLE);
						((TextView)view).setText(NumberFormat.getCurrencyInstance(Locale.US).format(cursor.getDouble(columnIndex)));
					}
					return true;
				case 3:
				case 4:
					if (cursor.isNull(3)){
						view.setVisibility(View.GONE);
					} else {
						view.setVisibility(View.VISIBLE);
					} //fall through to default processing
					break;
				case 5:
                case 8: {
					String item = cursor.getString(columnIndex);
					if (TextUtils.isEmpty(item) || "Unfiled".equals(item)) {
						view.setVisibility(View.GONE);

					} else {
						view.setVisibility(View.VISIBLE);
					} //fall through to default processing
					break;
				}
				}
				return false;
			}
		});

		setListAdapter(adapter);
	}
	
	
	
	@Override
	public void onNewIntent(Intent intent){
		setIntent(intent);
		handleIntent(intent);
	}
	
	private void handleIntent(Intent intent){
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
			if (query != null)
				query = query.trim();
		} else {
			query = null;
		}
		resetLoader();
	}
	
	private void resetLoader(){
		findViewById(R.id.progress).setVisibility(View.VISIBLE);
		getLoaderManager().restartLoader(ITEM_LOADER,null,this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main_list, menu);		
		
		// Get the SearchView and set the searchable configuration
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    MenuItem menuItem = menu.findItem(R.id.menu_search);
	    menuItem.setOnActionExpandListener(this);
	    SearchView searchView = (SearchView)menuItem.getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setOnQueryTextListener(this);
	    
	    newMenuItem = menu.findItem(R.id.menu_create);
	    newMenuItem.setVisible(false);
	    
		return true;
	}
	
	@Override
	public boolean onSearchRequested(){
		return super.onSearchRequested();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		menu.findItem(R.id.menu_show_all).setChecked(showAll);
		return true;
	}

	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_create:
				createItem();
				return true;
			case R.id.menu_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.menu_show_all:
				prefs.edit().putBoolean(SettingsFragment.PREF_SHOW_ALL, !showAll).apply();
				return true;
		/*
		case R.id.menu_import:
			startActivity(new Intent(this, ImportActivity.class));
			return true;
		*/
			case R.id.menu_send:
			{

				String body = getSendableString();
				sendSMS(body);

				return true;

			}
			case R.id.menu_checkout:
				new AlertDialog.Builder(this)
						.setTitle("Checkout")
						.setMessage("Remove purchased items from list?")
						.setPositiveButton("Checkout", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								ContentValues cv = new ContentValues();
								cv.put(ItemsTable.COLUMN_STATUS, "H");
								getContentResolver().update(GoShopContentProvider.ITEM_URI, cv, ItemsTable.COLUMN_STATUS+"='P'", null);
								//resetLoader();
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// do nothing
							}
						})
						.show();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@NonNull
	private String getSendableString() {
		StringBuilder sb = new StringBuilder("GoShop:");
		Cursor c = adapter.getCursor();
		int pos = c.getPosition();
		int col = c.getColumnIndexOrThrow(ItemsTable.COLUMN_NAME);
		int stat = c.getColumnIndexOrThrow(ItemsTable.COLUMN_STATUS);
		c.moveToFirst();
		while (!c.isAfterLast()){
            if ("N".equals(c.getString(stat))){
                sb.append("\n");
                sb.append(c.getString(col));
            }
            c.moveToNext();
        }
		c.moveToPosition(pos);

		return sb.toString();
	}

	@SuppressLint("DefaultLocale")
	private void createItem() {
		Intent i = new Intent(this, ItemDetailActivity.class);
		if (!TextUtils.isEmpty(query)){
			String name = query.substring(0, 1).toUpperCase()+query.substring(1);
			i.putExtra("name", name);
		}
		startActivity(i);
	}

	//@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == ITEM_LOADER){
			Uri uri;
			String[] projection;
			String selection=null;
			String[] selectionArgs = null;
			String sort;
			oneStore = store != -1L;		

			if (TextUtils.isEmpty(query)){
				if (showAll) {
					sort = ItemAisleDetailView.COLUMN_ITEM_NAME;
				} else {
					selection = "status <> 'H'";
					sort = "case status when 'N' then 1 when 'P' then 2 else 3 end";
					if (oneStore){
						sort += ", "+ItemAisleDetailView.COLUMN_SORT;
					}
					sort += ", "+ItemAisleDetailView.COLUMN_ITEM_NAME;
				}
			} else {
				oneStore = false;
				selection = ItemAisleDetailView.COLUMN_ITEM_NAME + " LIKE ? OR " 
						+ ItemAisleDetailView.COLUMN_NOTES + " LIKE ? OR "
						+ ItemAisleDetailView.COLUMN_CATEGORY + " LIKE ?";
				selectionArgs = new String[]{"%"+query+"%", "%"+query+"%", "%"+query+"%"};
				sort = ItemAisleDetailView.COLUMN_ITEM_NAME + " LIKE "+DatabaseUtils.sqlEscapeString(query+"%")+" desc, "
						+ ItemAisleDetailView.COLUMN_ITEM_NAME;
			}

			if (oneStore){
				projection = new String[]{ ItemAisleDetailView.COLUMN_ITEM + " as _id",
						ItemAisleDetailView.COLUMN_ITEM_NAME, 
						ItemAisleDetailView.COLUMN_PRICE, 
						ItemAisleDetailView.COLUMN_QUANTITY,
						ItemAisleDetailView.COLUMN_UNITS,
						ItemAisleDetailView.COLUMN_NOTES, 
						ItemAisleDetailView.COLUMN_STATUS, 
						ItemAisleDetailView.COLUMN_AISLE_NAME,
						ItemAisleDetailView.COLUMN_CATEGORY
				};
				if (showAll){
					uri = GoShopContentProvider.ITEM_AISLE_URI;
					selection = DatabaseUtils.concatenateWhere(selection, ItemAisleDetailView.COLUMN_STORE + "=?");
					selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[]{String.valueOf(store)});
				} else {
					return new CursorLoader(this, 
						Uri.withAppendedPath(GoShopContentProvider.EVERYTHING_NEEDED_URI, String.valueOf(store)),
						projection, null, null, null);
				}

			} else {
				uri = GoShopContentProvider.ITEM_URI;
				projection = new String[]{ ItemsTable.COLUMN_ID,
						ItemAisleDetailView.COLUMN_ITEM_NAME, ItemAisleDetailView.COLUMN_PRICE,
						ItemAisleDetailView.COLUMN_QUANTITY,
						ItemAisleDetailView.COLUMN_UNITS,
						ItemAisleDetailView.COLUMN_NOTES, ItemAisleDetailView.COLUMN_STATUS, 
						"NULL AS "+ItemAisleDetailView.COLUMN_AISLE_NAME,
						ItemAisleDetailView.COLUMN_CATEGORY
				};
			}

			return new CursorLoader(this, uri, projection, selection, selectionArgs, sort);
		} else if (id == STORE_LOADER){
			return new CursorLoader(this, GoShopContentProvider.STORES_WITH_ALL_URI, null, null, null, null);
		} else {
			throw new RuntimeException("Unexpected loader request for ID:"+id);
		}
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()){
		case ITEM_LOADER:
			findViewById(R.id.progress).setVisibility(View.GONE);
			adapter.swapCursor(data);
			break;
		case STORE_LOADER:
			spinnerAdapter.swapCursor(data);
			data.moveToFirst();
			int rowCount = data.getCount();
			for (int i = 0; i<rowCount && !data.isAfterLast(); ++i){
				if(data.getLong(0)==store){
					getActionBar().setSelectedNavigationItem(i);
					return;
				}
				data.moveToNext();
			}
		}

	}

	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()){
		case ITEM_LOADER:
			adapter.swapCursor(null);
			break;
		case STORE_LOADER:
			spinnerAdapter.swapCursor(null);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (id == -1L)
			return;
		
		Uri uri = Uri.parse(GoShopContentProvider.ITEM_URI + "/"+ id);
		
		String[] projection = new String[] {ItemsTable.COLUMN_STATUS};
		Cursor c = getContentResolver().query(uri, projection, null, null, null);
		c.moveToFirst();
		String status = c.getString(c.getColumnIndexOrThrow(ItemsTable.COLUMN_STATUS));
		c.close();
		if ("N".equals(status)){
			status = "P";
		} else if ("P".equals(status)){
			status = (showAll || !TextUtils.isEmpty(query)) ? "H" : "N";
		} else {
			status = "N";
		}
		
		ContentValues values = new ContentValues();
		values.put(ItemsTable.COLUMN_STATUS, status);
		getContentResolver().update(uri, values, null, null);
		//resetLoader();
	}

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if(key.equals(SettingsFragment.PREF_SHOW_ALL)){
			showAll = prefs.getBoolean(key, showAll);
			resetLoader();
		} else if (key.equals(SettingsFragment.PREF_LIST)){
			list = Long.valueOf(prefs.getString(key, "1"));
			prefs.edit().putString(SettingsFragment.PREF_STORE, "-1").apply();
		} else if (key.equals(SettingsFragment.PREF_STORE)){
			store = Long.valueOf(prefs.getString(SettingsFragment.PREF_STORE, "-1"));
			//createAdapter();
			resetLoader();
		}
	}

	@Override
	public boolean onQueryTextChange(String text) {
		query = text.trim();
		
		resetLoader();
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		return false;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem arg0) {
		newMenuItem.setVisible(false);
		query = null;
		//createAdapter();
		resetLoader();
		return true;
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem arg0) {
		newMenuItem.setVisible(true);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		prefs.edit().putString(SettingsFragment.PREF_STORE, String.valueOf(itemId)).apply();
		return true;
	}
	
	private void sendSMS(String body){
		String destination = prefs.getString(
				SettingsFragment.PREF_SMS_RECIPIENT, "");
		if (!PhoneNumberUtils.isWellFormedSmsAddress(destination)) {
			new AlertDialog.Builder(this)
					.setTitle("Bad phone number")
					.setMessage(
							"The phone number "
									+ destination
									+ " doesn't seem to be a valid number to send sms to.")
					.setPositiveButton("OK", null).show();
			return;
		}


		/*
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.setData(Uri.parse("sms:" + destination));
		sendIntent.putExtra("sms_body", body);
		startActivity(sendIntent);
		*/
		SmsManager.getDefault().sendTextMessage(destination, null, body, null, null);
	}
}
