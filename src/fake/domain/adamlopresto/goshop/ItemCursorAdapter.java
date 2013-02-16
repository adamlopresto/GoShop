package fake.domain.adamlopresto.goshop;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class ItemCursorAdapter extends SimpleCursorAdapter {

	private Context context;
	public ItemCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context=context;
	}

	@Override
	public void setViewImage (ImageView v, String value){
		Resources res = context.getResources();
	    
		if ("H".equals(value)){
			v.setImageDrawable(res.getDrawable(R.drawable.circle));
		} else if ("N".equals(value)){
			v.setImageDrawable(res.getDrawable(R.drawable.empty_check_box));
		} else {
			v.setImageDrawable(res.getDrawable(R.drawable.checked_check_box));
		}
	}
	
}
