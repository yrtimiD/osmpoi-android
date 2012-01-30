/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;


import java.util.List;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.categories.Category;
import android.content.Context;
import android.text.AndroidCharacter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
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
		
		TextView text = (TextView)convertView.findViewById(android.R.id.text1);
		
		
		Category cat = category.getSubCategories().get(position);
		text.setText(getLocalName(cat.getName()));
		
		return convertView;
	}

	private String getLocalName(String key){
		int resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
		if (resId == 0)
			return key;
		else 
			return context.getResources().getString(resId);
	}
}
