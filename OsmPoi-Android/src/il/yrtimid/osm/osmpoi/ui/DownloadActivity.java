/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.services.FileProcessingService;
import il.yrtimid.osm.osmpoi.ui.DownloadItem.ItemType;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author yrtimid
 * 
 */
public class DownloadActivity extends Activity implements OnItemClickListener {
	private DownloadListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.select_download);
	}

	@Override
	protected void onResume() {
		super.onResume();

		DownloadItem urls = loadList();
		ListView lv = (ListView) this.findViewById(R.id.list);
		adapter = new DownloadListAdapter(this, R.layout.select_download_row, urls);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
	}

	private DownloadItem loadList() {
		DownloadItem root = new DownloadItem();
		root.Name="/";
		root.Type = ItemType.FOLDER;
		root.SubItems = new ArrayList<DownloadItem>();
		
		List<String> urls = new ArrayList<String>();
		try {
			InputStream stream = this.getAssets().open("pbf_list");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0 && URLUtil.isValidUrl(line))
					urls.add(line);
			}
		} catch (IOException e) {
			Log.wtf("loadList reading file", e);
		}

		if (urls.size() > 0) {
			// find base url (same for all urls in list
			String base = urls.get(0);
			base = base.substring(0, base.lastIndexOf('/'));
			for (String url : urls) {
				while (false == base.regionMatches(0, url, 0, base.length())) {
					base = base.substring(0, base.lastIndexOf('/'));
					if (base.length() == 0)
						break;
				}
			}
			base += "/";

			Map<String, DownloadItem> levelsMap = new HashMap<String, DownloadItem>();
			for (String url : urls) {
				String rel = url.substring(base.length());// relative path
				String currentLevel = "";
				DownloadItem currentFolder = null;
				for (String level : rel.split("/")) {
					if (level.endsWith(".pbf")) {
						DownloadItem newFile = new DownloadItem();
						newFile.Type = ItemType.FILE;
						newFile.Name = level;
						newFile.Url = url;

						if (currentFolder == null) {// we at root
							root.SubItems.add(newFile);
							newFile.Parent = root;
						} else {//we at sub-folder
							currentFolder.SubItems.add(newFile);
							newFile.Parent = currentFolder;
						}
					}
					else {
						currentLevel += level + "/";

						if (levelsMap.containsKey(currentLevel)) {
							currentFolder = levelsMap.get(currentLevel);
						}
						else {
							DownloadItem newFolder = new DownloadItem();
							newFolder.Type = ItemType.FOLDER;
							newFolder.Name = level;
							newFolder.Url = base + currentLevel;
							newFolder.SubItems = new ArrayList<DownloadItem>();
							if (currentFolder == null) {// we at root
								root.SubItems.add(newFolder);
								newFolder.Parent = root;
							} else {//we at sub-folder
								currentFolder.SubItems.add(newFolder);
								newFolder.Parent = currentFolder;
							}
							currentFolder = newFolder;
							levelsMap.put(currentLevel, currentFolder);
						}
					}
				}
			}
		}
		

		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DownloadItem item = adapter.getItem(position);
		if (item.Type == ItemType.FOLDER) {
			adapter.changeLevel(item);
			parent.setSelection(0);
		} else {
			final String url = item.Url;
			ConfirmDialog.Confirm(this, String.format(getString(R.string.confirm_download_and_import), item.Name),
					new ConfirmDialog.Action() {
						@Override
						public void PositiveAction() {
							ConfirmDialog.Confirm(DownloadActivity.this, getString(R.string.import_confirm),
									new ConfirmDialog.Action() {
										@Override
										public void PositiveAction() {
											runDownloadAndImportService(url);
										}
									}
									);
						}
					}
					);
		}
	}

	private void runDownloadAndImportService(String url) {
		Intent serviceIntent = new Intent(this, FileProcessingService.class);
		serviceIntent.putExtra(FileProcessingService.EXTRA_OPERATION, FileProcessingService.Operation.IMPORT_TO_DB.name());
		serviceIntent.putExtra(FileProcessingService.EXTRA_FILE_PATH, url);

		startService(serviceIntent);

	}
}
