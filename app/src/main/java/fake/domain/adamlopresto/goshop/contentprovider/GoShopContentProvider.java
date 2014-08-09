package fake.domain.adamlopresto.goshop.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import fake.domain.adamlopresto.goshop.DatabaseHelper;
import fake.domain.adamlopresto.goshop.tables.AislesTable;
import fake.domain.adamlopresto.goshop.tables.ItemAisleDetailView;
import fake.domain.adamlopresto.goshop.tables.ItemAisleTable;
import fake.domain.adamlopresto.goshop.tables.ItemsTable;
import fake.domain.adamlopresto.goshop.tables.StoresTable;

public class GoShopContentProvider extends ContentProvider {

	private DatabaseHelper helper;

	// Used for the UriMatcher
	// Odd numbers have an ID, evens don't.
	private static final int ITEMS = 0;
	private static final int ITEM_ID = 1;
	private static final int ITEM_AISLE = 2;
	private static final int ITEM_AISLE_ID = 3;
	private static final int STORE = 4;
	private static final int STORE_ID = 5;
	private static final int AISLE = 6;
	private static final int AISLE_ID = 7;
	private static final int STORES_WITH_ALL = 8;
	private static final int EVERYTHING_NEEDED = 11;

	public static final String AUTHORITY = "fake.domain.adamlopresto.goshop.contentprovider";
	public static final Uri BASE = Uri.parse("content://"+AUTHORITY);

	private static final String ITEM_BASE_PATH = "items";
	public static final Uri ITEM_URI = Uri.withAppendedPath(BASE, ITEM_BASE_PATH);

	private static final String ITEM_AISLE_BASE_PATH = "item_aisle";
	public static final Uri ITEM_AISLE_URI = Uri.withAppendedPath(BASE, ITEM_AISLE_BASE_PATH);

	private static final String STORES_BASE_PATH = "stores";
	public static final Uri STORES_URI = Uri.withAppendedPath(BASE, STORES_BASE_PATH);
	private static final String STORES_WITH_ALL_PATH = STORES_BASE_PATH+"/with_all";
	public static final Uri STORES_WITH_ALL_URI = Uri.withAppendedPath(BASE, STORES_WITH_ALL_PATH);

	private static final String AISLES_BASE_PATH = "aisles";
	public static final Uri AISLES_URI = Uri.withAppendedPath(BASE, AISLES_BASE_PATH);
	
	private static final String EVERYTHING_NEEDED_BASE_PATH = "everything_needed";
	public static final Uri EVERYTHING_NEEDED_URI = Uri.withAppendedPath(BASE, EVERYTHING_NEEDED_BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE;
			
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/GoShopItem";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, ITEM_BASE_PATH, ITEMS);
		sURIMatcher.addURI(AUTHORITY, ITEM_BASE_PATH + "/#", ITEM_ID);
		sURIMatcher.addURI(AUTHORITY, ITEM_AISLE_BASE_PATH, ITEM_AISLE);
		sURIMatcher.addURI(AUTHORITY, ITEM_AISLE_BASE_PATH+"/#", ITEM_AISLE_ID);
		sURIMatcher.addURI(AUTHORITY, STORES_BASE_PATH, STORE);
		sURIMatcher.addURI(AUTHORITY, STORES_BASE_PATH+"/#", STORE_ID);
		sURIMatcher.addURI(AUTHORITY, AISLES_BASE_PATH, AISLE);
		sURIMatcher.addURI(AUTHORITY, AISLES_BASE_PATH+"/#", AISLE_ID);
		sURIMatcher.addURI(AUTHORITY, STORES_WITH_ALL_PATH, STORES_WITH_ALL);
		sURIMatcher.addURI(AUTHORITY, EVERYTHING_NEEDED_BASE_PATH+"/#", EVERYTHING_NEEDED);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = helper.getWritableDatabase();
		int rowsDeleted = 0;
		String id;
		switch (uriType) {
		case ITEM_ID:
			id = uri.getLastPathSegment();
			selection = DatabaseUtils.concatenateWhere(ItemsTable.COLUMN_ID + "=?", selection);
			selectionArgs = appendSelectionArgs(new String[]{id}, selectionArgs);
			//notify of this particular item
			getContext().getContentResolver().notifyChange(uri, null);
			//fall through
		case ITEMS:
			rowsDeleted = sqlDB.delete(ItemsTable.TABLE, selection,
					selectionArgs);
			if (rowsDeleted > 0) {
				getContext().getContentResolver().notifyChange(ITEM_URI, null);
				getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
			}
			break;
		case ITEM_AISLE_ID:
			id = uri.getLastPathSegment();
			selection = DatabaseUtils.concatenateWhere(ItemAisleTable.COLUMN_ID + "=?", selection);
			selectionArgs = appendSelectionArgs(new String[]{id}, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			//fall through
		case ITEM_AISLE:
			rowsDeleted = sqlDB.delete(ItemAisleTable.TABLE, selection,
					selectionArgs);
			if (rowsDeleted > 0)
				getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
			break;
		case STORE_ID:
			id = uri.getLastPathSegment();
			selection = DatabaseUtils.concatenateWhere(StoresTable.COLUMN_ID + "=?", selection);
			selectionArgs = appendSelectionArgs(new String[]{id}, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			//fall through
		case STORE:
			rowsDeleted = sqlDB.delete(StoresTable.TABLE, selection,
					selectionArgs);
			if (rowsDeleted > 0){
				getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
				getContext().getContentResolver().notifyChange(STORES_URI, null);
			}
			break;
		case AISLE_ID:
			id = uri.getLastPathSegment();
			selection = DatabaseUtils.concatenateWhere(AislesTable.COLUMN_ID + "=?", selection);
			selectionArgs = appendSelectionArgs(new String[]{id}, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			//fall through
		case AISLE:
			rowsDeleted = sqlDB.delete(AislesTable.TABLE, selection,
					selectionArgs);
			if (rowsDeleted > 0){
				getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
				getContext().getContentResolver().notifyChange(AISLES_URI, null);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = helper.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case ITEMS:
			id = sqlDB.insertOrThrow(ItemsTable.TABLE, null, values);
			break;
		case ITEM_AISLE:
			id = sqlDB.insertOrThrow(ItemAisleTable.TABLE, null, values);
			break;
		case STORE:
			id = sqlDB.insertOrThrow(StoresTable.TABLE, null, values);
			break;
		case AISLE:
			id = sqlDB.insertOrThrow(AislesTable.TABLE, null, values);
			break;
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(uri+"/"+id);
	}

	@Override
	public boolean onCreate() {
		helper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setDistinct(true);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case EVERYTHING_NEEDED:{
			String store = uri.getLastPathSegment();
			/*
			ItemAisleDetailView.COLUMN_ITEM + " as _id",
			ItemAisleDetailView.COLUMN_ITEM_NAME, 
			ItemAisleDetailView.COLUMN_PRICE, 
			ItemAisleDetailView.COLUMN_QUANTITY,
			ItemAisleDetailView.COLUMN_UNITS,
			ItemAisleDetailView.COLUMN_NOTES, 
			ItemAisleDetailView.COLUMN_STATUS, 
			ItemAisleDetailView.COLUMN_AISLE_NAME
			*/
			Cursor c = helper.getReadableDatabase().rawQuery(
				"SELECT _id, item_name, price, quantity, units, notes, " +
					"status, category, aisle_name " +
				"FROM ("+
				"SELECT item as _id, item_name, price, quantity, units, notes, " +
					"status, category, aisle_name, 1 as sort1, sort " +
				"FROM item_aisle_detail where status <> 'H' and store=? " +
				"UNION SELECT -1 as _id, null as item_name, null as price, null as quantity, " +
 				"null as units, null as notes, null as status, null as category, " +
 				"null as aisle_name, 2 as sort1, null as sort " +
				"UNION SELECT _id, item_name, price, quantity, units, notes, " +
					"status, category, null as aisle_name, 3 as sort1, null as sort " +
				"FROM items i where status <> 'H' and not exists " +
					"(select * from item_aisle ia inner join aisles a " +
					"on ia.item = i._id and ia.aisle = a._id and a.store = ?) " +
				"ORDER by sort1, " +
				"status, "+
				"sort, item_name"+
				")",
				new String[]{store, store}
			);
			c.setNotificationUri(getContext().getContentResolver(), ITEM_AISLE_URI);
			return c;
		}
		case ITEM_ID:
			queryBuilder.appendWhere(ItemsTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			//fall through!
		case ITEMS:
			// Adding the ID to the original query
			queryBuilder.setTables(ItemsTable.TABLE);
			break;
		case ITEM_AISLE_ID:
			queryBuilder.appendWhere(ItemAisleDetailView.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			//fall through
		case ITEM_AISLE:
			queryBuilder.setTables(ItemAisleDetailView.VIEW);
			break;
		case STORE_ID:
			queryBuilder.appendWhere(StoresTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			//fall through
		case STORE:
			queryBuilder.setTables(StoresTable.TABLE);
			break;
		case STORES_WITH_ALL:
			Cursor c = helper.getReadableDatabase().rawQuery(
					//"SELECT _id, list, store_name FROM (SELECT -1 AS _id, 0 AS list, 'All' AS store_name, 0 as sortfield UNION SELECT _id, list, store_name, 1 as sortfield FROM stores ORDER BY sortfield, store_name)", null);
					"SELECT _id, list, store_name FROM (SELECT -1 AS _id, 0 AS list, 'All' AS store_name, 0 as sortfield UNION SELECT store as _id, list, store_name, 1 as sortfield FROM item_aisle_detail WHERE status <> 'H' GROUP BY store HAVING count(item) > 0 ORDER BY sortfield, store_name)", null);
			c.setNotificationUri(getContext().getContentResolver(), ITEM_AISLE_URI);
			return c;
		case AISLE_ID:
			queryBuilder.appendWhere(AislesTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			//fall through
		case AISLE:
			queryBuilder.setTables(AislesTable.TABLE);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = helper.getWritableDatabase();
		int rowsUpdated = 0;

		//If it's odd, then it has an ID appended.
		if ((uriType % 2) == 1){
			String id = uri.getLastPathSegment();
			selection = appendSelection(selection, "_id = ?");
			selectionArgs = appendSelectionArg(selectionArgs, id);
			uriType--;
		}

		switch (uriType) {
		case ITEMS:
			rowsUpdated = sqlDB.update(ItemsTable.TABLE, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
			getContext().getContentResolver().notifyChange(ITEM_URI, null);
			break;
		case AISLE:
			rowsUpdated = sqlDB.update(AislesTable.TABLE, values, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(ITEM_AISLE_URI, null);
			getContext().getContentResolver().notifyChange(AISLES_URI, null);
			break;
		case STORE:
			rowsUpdated = sqlDB.update(StoresTable.TABLE, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private static String[] appendSelectionArgs(String originalValues[], String newValues[]){
		if (originalValues == null){
			return newValues;
		}
		if (newValues == null){
			return originalValues;
		}
		return DatabaseUtils.appendSelectionArgs(originalValues, newValues);
	}
	
	private static String appendSelection(String original, String newSelection){
		return DatabaseUtils.concatenateWhere(original, newSelection);
	}
	
	private static String[] appendSelectionArg(String[] originalValues, String newValue){
		return appendSelectionArgs(originalValues, new String[]{newValue});
	}

}
