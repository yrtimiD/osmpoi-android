/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.R;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TwoLineListItem;

/**
 * @author yrtimid
 * 
 */
public class DownloadListAdapter extends BaseAdapter {

	LayoutInflater inflater;

	List<DownloadItem> items;
	DownloadItem parent;

	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public DownloadListAdapter(Context context, int textViewResourceId, DownloadItem rootItem) {
		inflater = LayoutInflater.from(context);
		this.items = rootItem.SubItems;
		parent = rootItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getCount()
	 */
	@Override
	public int getCount() {
		if (parent == null)
			return items.size();
		else
			return items.size() + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public DownloadItem getItem(int position) {
		if (position == 0)
			return parent;
		else
			return items.get(position - 1);
	}

	public void changeLevel(DownloadItem newLevel) {
		if (newLevel.Parent != null)
			parent = newLevel.Parent;
		
		items = newLevel.SubItems;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DownloadItem item = getItem(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.results_view_row, parent, false);
		}

		TwoLineListItem listItem = (TwoLineListItem) convertView;

		if (position == 0 && parent != null)
			listItem.getText1().setText("..");
		else
			listItem.getText1().setText(item.Name);

		listItem.getText2().setText("");

		return convertView;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

}
