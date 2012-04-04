/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.services.FileProcessingService;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author yrtimid
 * 
 */
public class SelectDownloadDialogPreference extends DialogPreference implements OnItemClickListener{
	private List<String> list = new ArrayList<String>();
	private String baseUrl;
	private SelectDownloadListAdapter adapter;
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SelectDownloadDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setDialogLayoutResource(R.layout.select_download);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public SelectDownloadDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.select_download);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.preference.DialogPreference#onPrepareDialogBuilder(android.app
	 * .AlertDialog.Builder)
	 */
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
		builder.setTitle("Select file to download");
		builder.setPositiveButton(null, null);
		builder.setNegativeButton(null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.preference.DialogPreference#onBindDialogView(android.view.View)
	 */
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		loadList();
		ListView lv = (ListView)view.findViewById(R.id.list);
		adapter = new SelectDownloadListAdapter(getContext(), R.layout.select_download_row, this.list);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
	}

	private void loadList() {
		List<String> urls = new ArrayList<String>();
		try {
			InputStream stream = getContext().getAssets().open("pbf_list");
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
			//find base url (same for all urls in list
			String base = urls.get(0);
			base = base.substring(0, base.lastIndexOf('/'));
			for (String url : urls) {
				while (false == base.regionMatches(0, url, 0, base.length())){
					base = base.substring(0, base.lastIndexOf('/'));
					if (base.length() == 0) break;
				}
			}
			this.baseUrl = base;
			for(String url : urls){
				list.add(url.substring(this.baseUrl.length()));
			}
		}
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
		String item = adapter.getItem(position);
		if (item.equals("..")){
			adapter.changeLevel("..");
			parent.setSelection(0);
		}else if (item.endsWith("/")) {
			adapter.changeLevel(item);
			parent.setSelection(0);
		}else {
			final String url = baseUrl + adapter.getRoot() + item;
			ConfirmDialog.Confirm(getContext(), String.format(getContext().getString(R.string.confirm_download_and_import), item), 
					new ConfirmDialog.Action() {
						@Override
						public void PositiveAction() {
							ConfirmDialog.Confirm(getContext(), getContext().getString(R.string.import_confirm), 
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

			getDialog().dismiss();
		}
	}
	
	private void runDownloadAndImportService(String url){
		Intent serviceIntent = new Intent(getContext(), FileProcessingService.class);
		serviceIntent.putExtra(FileProcessingService.EXTRA_OPERATION, FileProcessingService.Operation.IMPORT_TO_DB.name());
		serviceIntent.putExtra(FileProcessingService.EXTRA_FILE_PATH, url);
		
		getContext().startService(serviceIntent);
	
	}

}
