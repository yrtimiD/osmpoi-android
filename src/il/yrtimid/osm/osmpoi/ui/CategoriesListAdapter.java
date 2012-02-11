/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;


import java.util.List;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.categories.Category;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.AndroidCharacter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * @author yrtimid
 *
 */
public class CategoriesListAdapter extends BaseAdapter {

	private Category category;
	private Context context;
	private LayoutInflater inflater;
	
	/**
	 * 
	 */
	public CategoriesListAdapter(Context context, Category category) {
		this.category = category;
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return category.getSubCategoriesCount();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return category.getSubCategories().get(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null){
			convertView = inflater.inflate(R.layout.category_item, parent, false);
		}
		
		ImageView iconView = (ImageView)convertView.findViewById(android.R.id.icon);
		TextView textView = (TextView)convertView.findViewById(android.R.id.text1);
		
		
		Category cat = category.getSubCategories().get(position);
		
		String name = (cat.isLocalizable())?getLocalName(cat.getName()):cat.getName();
		textView.setText(name);
		
		Drawable icon = getIcon(cat.getIcon());
		if (icon != null){
			iconView.setImageDrawable(icon);
		}else {
			iconView.setImageDrawable(null);
		}
		
		return convertView;
	}

	private String getLocalName(String key){
		if (key == null || key.length()==0) return key;
		if (false == Character.isLetter(key.charAt(0))) return key; //getIdentifier internally tries to convert name to integer, so if key=="10" we'll not get it resolved to real ID
		
		int resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
		if (resId == 0)
			return key;
		else 
			return context.getResources().getString(resId);
	}
	
	private Drawable getIcon(String key){
		if (key == null || key.length()==0) return null;
		if (false == Character.isLetter(key.charAt(0))) return null; //getIdentifier internally tries to convert name to integer, so if key=="10" we'll not get it resolved to real ID
		
		int resId = context.getResources().getIdentifier(key, "drawable", context.getPackageName());
		if (resId == 0){
			resId = context.getResources().getIdentifier(key, "drawable", "android");
			if (resId == 0)
				return null;
		}
		
		return context.getResources().getDrawable(resId);
	}
}
