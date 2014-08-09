package fake.domain.adamlopresto.goshop;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.ViewSwitcher;

public class ItemCursorAdapter extends SimpleCursorAdapter {

	private Context context;
	public ItemCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context=context;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor){
		if (cursor.getLong(0) == -1L){
			if (cursor.isLast())
				view.setVisibility(View.GONE);
			else {
				view.setVisibility(View.VISIBLE);
				((ViewSwitcher)view).setDisplayedChild(1);
			}
		} else{
			view.setVisibility(View.VISIBLE);
			((ViewSwitcher)view).setDisplayedChild(0);
			super.bindView(view, context, cursor);
		}
	}
	
	@Override
	public boolean isEnabled(int position){
		Cursor c = getCursor();
		if (c == null)
			return true;
		if (c.moveToPosition(position))
			return c.getLong(0) != -1L;
		return true;
	}
	
	@Override
	public boolean areAllItemsEnabled(){
		return false;
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
