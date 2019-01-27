package fake.domain.adamlopresto.goshop;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import fake.domain.adamlopresto.goshop.contentprovider.GoShopContentProvider;
import fake.domain.adamlopresto.goshop.tables.ItemsTable;

public class GoShopBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    Object[] pdus=(Object[])intent.getExtras().get("pdus");
	    StringBuilder text=new StringBuilder(); 
	    // get sender from first PDU 
	    
	    SmsMessage shortMessage;

		for (Object pdu : pdus) {
			shortMessage = SmsMessage.createFromPdu((byte[]) pdu);
			text.append(shortMessage.getDisplayMessageBody());
		}

	    String msg = text.toString();
	    if (!msg.startsWith("GoShop:\n")){
	    	return;
	    }

		addItems(context, msg);
	}

	public static void addItems(Context context, @Nullable String msg) {
		if (msg == null) return;

		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		String[] items = msg.split("\n");
		int itemsAdded = 0;

		ContentValues cv = new ContentValues(1);
		cv.put(ItemsTable.COLUMN_STATUS, "N");

		ContentValues insertValues = new ContentValues(3);
		insertValues.put(ItemsTable.COLUMN_STATUS, "N");
		insertValues.put(ItemsTable.COLUMN_LIST, 1);

		String[] args = new String[1];

		db.beginTransaction();
		for (String item : items) {
		    if ("GoShop:".equals(item))
		    	continue;
			args[0] = item;
			if (db.update(ItemsTable.TABLE, cv, ItemsTable.COLUMN_NAME + "= ?", args) <= 0) {
				if (db.update(ItemsTable.TABLE, cv,
						"'#' ||" + ItemsTable.COLUMN_VOICE_NAMES +
								"|| '#' LIKE '%#' || ? || '#%'", args) <= 0) {
					insertValues.put(ItemsTable.COLUMN_NAME, item + " (new)");
					db.insert(ItemsTable.TABLE, null, insertValues);
				}
			}

			itemsAdded++;
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();

		context.getContentResolver().notifyChange(GoShopContentProvider.ITEM_URI, null);
		context.getContentResolver().notifyChange(GoShopContentProvider.ITEM_AISLE_URI, null);

		Toast.makeText(context, "Updated list. Marked "+(itemsAdded)+" items needed.", Toast.LENGTH_LONG).show();
	}

}
