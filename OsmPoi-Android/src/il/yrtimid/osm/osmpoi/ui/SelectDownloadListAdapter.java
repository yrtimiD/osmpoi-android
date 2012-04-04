/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * @author yrtimid
 * 
 */
public class SelectDownloadListAdapter extends ArrayAdapter<String>  {

	String root = "/";
	List<String> currentLevel = new ArrayList<String>();

	/**
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public SelectDownloadListAdapter(Context context, int textViewResourceId, List<String> objects) {
		super(context, textViewResourceId, objects);
		calcCurrentLevel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return currentLevel.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public String getItem(int position) {
		return currentLevel.get(position);
	}

	private void calcCurrentLevel() {
		currentLevel.clear();
		HashSet<String> hash = new HashSet<String>();
		for (int i = 0; i < super.getCount(); i++) {
			String s = super.getItem(i);
			if (s.startsWith(root)) {
				int lastSlash = s.indexOf('/', root.length() + 1);
				if (lastSlash != -1)
					hash.add(s.substring(root.length(), lastSlash+1));
				else
					hash.add(s.substring(root.length()));
			}
		}

		currentLevel.addAll(hash);
		Collections.sort(currentLevel);
		if (!"/".equals(root))
			currentLevel.add(0, "..");
	}

	public void changeLevel(String newLevel){
		if ("..".equals(newLevel)){
			if (root.length()>1){
				root = root.substring(0,root.length()-1);
				root = root.substring(0,root.lastIndexOf('/')+1);
			}
		}else {
			root = root + newLevel;
		}
		calcCurrentLevel();
		notifyDataSetChanged();
	}
	
	public String getRoot() {
		return root;
	}
	
}
