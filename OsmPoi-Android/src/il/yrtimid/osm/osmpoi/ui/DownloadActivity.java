/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.logging.Log;
import il.yrtimid.osm.osmpoi.services.FileProcessingService;
import il.yrtimid.osm.osmpoi.ui.DownloadItem.ItemType;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * @author yrtimid
 * 
 */
public class DownloadActivity extends ListActivity implements ListView.OnScrollListener {
	private DownloadListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		// setContentView(R.layout.select_download);

		getListView().setOnScrollListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		DownloadItem urls = loadList();

		adapter = new DownloadListAdapter(this, R.layout.select_download_row, urls);
		setListAdapter(adapter);
	}

	private DownloadItem loadList() {
		DownloadItem root = new DownloadItem();
		root.Name = "/";
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
						} else {// we at sub-folder
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
							} else {// we at sub-folder
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

	@Override
	protected void onListItemClick(ListView parent, View view, int position, long id) {
		DownloadItem item = adapter.getItem(position);
		if (item.Type == ItemType.FOLDER) {
			adapter.changeLevel(item);
			parent.setSelection(0);
		} else {

			checkDownloadItemAvailability(item);
		}
	}

	private void checkDownloadItemAvailability(final DownloadItem item) {
		new AsyncTask<DownloadItem, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(DownloadItem... params) {
				try {
					DownloadItem item = params[0];
					URL u = new URL(item.Url);
					HttpURLConnection conn;
					conn = (HttpURLConnection) u.openConnection();
					item.Size = conn.getContentLength();
					item.LastModified = new Date(conn.getLastModified());
					return conn.getResponseCode() == 200;
				} catch (IOException e) {
					Log.wtf("checkDownloadItem", e);
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				setProgressBarIndeterminateVisibility(false);
				confirmDownloading(result, item);
			}
		}.execute(item);
		setProgressBarIndeterminateVisibility(true);
	}

	private void confirmDownloading(Boolean itemAvailable, DownloadItem item) {
		if (itemAvailable) {
			final String url = item.Url;

			final ConfirmDialog.Action runDownloadAndImportServiceAction =
					new ConfirmDialog.Action() {
						@Override
						public void PositiveAction() {
							runDownloadAndImportService(url);
						}
					};

			final ConfirmDialog.Action confirmDownloadAction =
					new ConfirmDialog.Action() {
						@Override
						public void PositiveAction() {
							ConfirmDialog.Confirm(DownloadActivity.this, getString(R.string.import_confirm), runDownloadAndImportServiceAction);
						}
					};
			String lastModified = DateFormat.getMediumDateFormat(this).format(item.LastModified);
			String size = Util.formatSize(item.Size);
			String message = getString(R.string.confirm_download_and_import, item.Name, size, lastModified);
			ConfirmDialog.Confirm(this, message, confirmDownloadAction);
		} else {
			Util.showAlert(this, getString(R.string.file_unavailable, item.Name));
		}
	}

	private void runDownloadAndImportService(String url) {
		Intent serviceIntent = new Intent(this, FileProcessingService.class);
		serviceIntent.putExtra(FileProcessingService.EXTRA_OPERATION, FileProcessingService.Operation.IMPORT_TO_DB.name());
		serviceIntent.putExtra(FileProcessingService.EXTRA_FILE_PATH, url);

		startService(serviceIntent);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android
	 * .widget.AbsListView, int)
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.
	 * AbsListView, int, int, int)
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}
}
