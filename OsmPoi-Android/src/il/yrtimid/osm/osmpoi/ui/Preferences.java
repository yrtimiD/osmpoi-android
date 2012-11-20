package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.ImportSettings;
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.services.FileProcessingService;

import java.io.File;
import java.util.List;
import com.kaloer.filepicker.FilePickerActivity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

//TODO: prevent executing more than one service task at a time
public class Preferences extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	public static final String SEARCH_SOURCE = "search_source";
	public static final String LAST_SEARCH = "last_search";

	public static final String RESULT_LANGUAGE = "result_language";
	public static final String IS_DB_ON_SDCARD = "is_db_on_sdcard";
	private static final String PREFERENCE_IMPORT_PBF = "preference_import_pbf";
	private static final String PREFERENCE_CLEAR_DB = "debug_clear_db";
	private static final String PREFERENCE_BUILD_GRID = "debug_rebuild_grid";
	private static final String PREFERENCE_DOWNLOAD = "preference_download";
	private static final String PREFERENCE_DEBUG_RESET = "preference_debug_reset";
	private static final String PREFERENCE_INCLUDE_EXCLUDE_RESET = "include_exclude_reset";
	public static final String NODES_INCLUDE = "nodes_include";
	public static final String NODES_EXCLUDE = "nodes_exclude";
	public static final String WAYS_INCLUDE = "ways_include";
	public static final String WAYS_EXCLUDE = "ways_exclude";
	public static final String RELATIONS_INCLUDE = "relations_include";
	public static final String RELATIONS_EXCLUDE = "relations_exclude";
	
	private static final int INTERNAL_PICK_FILE_REQUEST_FOR_IMPORT = 1;
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		findPreference(PREFERENCE_IMPORT_PBF).setOnPreferenceClickListener(this);
		findPreference(PREFERENCE_CLEAR_DB).setOnPreferenceClickListener(this);
		findPreference(SEARCH_SOURCE).setOnPreferenceChangeListener(this);
		findPreference(IS_DB_ON_SDCARD).setOnPreferenceChangeListener(this);
		findPreference(PREFERENCE_BUILD_GRID).setOnPreferenceClickListener(this);
		findPreference(PREFERENCE_DOWNLOAD).setOnPreferenceClickListener(this);
		findPreference(PREFERENCE_DEBUG_RESET).setOnPreferenceClickListener(this);
		findPreference(PREFERENCE_DOWNLOAD).setOnPreferenceClickListener(this);
		findPreference(PREFERENCE_INCLUDE_EXCLUDE_RESET).setOnPreferenceClickListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		OsmPoiApplication.Config.reloadConfig(this);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.preference.Preference.OnPreferenceClickListener#onPreferenceClick
	 * (android.preference.Preference)
	 */
	@Override
	public boolean onPreferenceClick(final Preference preference) {
		String key = preference.getKey();

		if (PREFERENCE_IMPORT_PBF.equals(key)) {
			Intent intent = new Intent(this, FilePickerActivity.class);
			intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, getHomeFolder(this).getPath());
			startActivityForResult(intent, INTERNAL_PICK_FILE_REQUEST_FOR_IMPORT);
			return true;
		} else if (PREFERENCE_CLEAR_DB.equals(key)) {
			ConfirmDialog.Confirm((Context) Preferences.this, getString(R.string.clean_db_confirm), new ConfirmDialog.Action() {
				@Override
				public void PositiveAction() {
					runClearDbService();
				}
			});
			return true;
		} else if (PREFERENCE_BUILD_GRID.equals(key)){
			runBuildGridService();
			return true;
		} else if (PREFERENCE_DOWNLOAD.equals(key)){
			Intent intent = new Intent(this, DownloadActivity.class);
			startActivity(intent);
		} else if (PREFERENCE_DEBUG_RESET.equals(key)){
			OsmPoiApplication.Config.reset(this);
			OsmPoiApplication.Config.reloadConfig(this);
			this.finish();
		} else if (PREFERENCE_INCLUDE_EXCLUDE_RESET.equals(key)){
			OsmPoiApplication.Config.resetIncludeExclude(this);
			OsmPoiApplication.Config.reloadConfig(this);
			this.finish();
		}

		return false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTERNAL_PICK_FILE_REQUEST_FOR_IMPORT) {
			if (resultCode == RESULT_OK) {
				final File f = new File(data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
				importPBF(f);
			}
		}
	}

	private void importPBF(final File f) {
		ConfirmDialog.Confirm((Context) Preferences.this, getString(R.string.import_confirm), new ConfirmDialog.Action() {
			@Override
			public void PositiveAction() {
				runPbfImportService(f);
			}
		});
	}

	private void runPbfImportService(final File f) {
		Intent serviceIntent = new Intent(Preferences.this, FileProcessingService.class);
		serviceIntent.putExtra(FileProcessingService.EXTRA_OPERATION, FileProcessingService.Operation.IMPORT_TO_DB.name());
		serviceIntent.putExtra(FileProcessingService.EXTRA_FILE_PATH, f.getPath());
		startService(serviceIntent);
	}

	private void runClearDbService() {
		Intent serviceIntent = new Intent(Preferences.this, FileProcessingService.class);
		serviceIntent.putExtra(FileProcessingService.EXTRA_OPERATION, FileProcessingService.Operation.CLEAR_DB.name());
		startService(serviceIntent);
	}

	private void runBuildGridService(){
		Intent serviceIntent = new Intent(Preferences.this, FileProcessingService.class);
		serviceIntent.putExtra(FileProcessingService.EXTRA_OPERATION, FileProcessingService.Operation.BUILD_GRID.name());
		startService(serviceIntent);
	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange
	 * (android.preference.Preference, java.lang.Object)
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
/*		if (SEARCH_SOURCE.equals(preference.getKey())) {
			SearchSourceType type = (SearchSourceType) Enum.valueOf(SearchSourceType.class, (String) newValue);

			Boolean success = OsmPoiApplication.Config.tryCreateSearchSource(this, type);
			if (success) {
				return true;
			} else {
				Toast.makeText(this, R.string.cant_create_search_source, Toast.LENGTH_LONG).show();
			}
		}else if (IS_DB_ON_SDCARD.equals(preference.getKey())){
			OsmPoiApplication.Config.reloadConfig(this);
			return true;
		}
		return false;
		*/
		
		return true;
	}

	/**
	 * 
	 * @param context
	 * @return path of the home folder on external storage, or null if internal
	 *         storage must be used.
	 */
	/*
	 * private String getHomeFolder() { SharedPreferences prefs =
	 * getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE); String homeFolder =
	 * prefs.getString(PREFERENCE_HOME_FOLDER, null); if (homeFolder == null) {
	 * // try external storage String state =
	 * Environment.getExternalStorageState(); if
	 * (Environment.MEDIA_MOUNTED.equals(state)) { File appDir =
	 * this.getExternalFilesDir(null); if (appDir != null) { try { homeFolder =
	 * appDir.getCanonicalPath(); } catch (IOException e) { Log.e(TAG,
	 * e.getMessage()); homeFolder = null; } }
	 * 
	 * } else { Log.w(TAG, "Can't use external storage. StorageState:" + state);
	 * }
	 * 
	 * if (homeFolder != null) { SharedPreferences.Editor prefsEditor =
	 * getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE).edit();
	 * prefsEditor.putString(PREFERENCE_HOME_FOLDER, homeFolder);
	 * prefsEditor.commit(); } } Log.i(TAG, "Home folder: " + (homeFolder ==
	 * null ? getFilesDir() : homeFolder)); return homeFolder; }
	 */

	/*
	 * public FileInputStream getInputFile(String name) throws
	 * FileNotFoundException { String homeFolder = getHomeFolder(); if
	 * (homeFolder == null) { return openFileInput(name); } else { return new
	 * FileInputStream(new File(homeFolder, name)); } }
	 */

/*	public static FileOutputStream getOutputFile(Context context, String name) throws FileNotFoundException {
		String homeFolder = getHomeFolder(context);
		if (homeFolder == null) {
			return context.openFileOutput(name, MODE_PRIVATE);
		} else {
			return new FileOutputStream(new File(homeFolder, name));
		}
	}*/

	public static File getHomeFolder(Context context) {
		//File appDir = context.getExternalFilesDir(null); //api 8+
		File extStorageDir = Environment.getExternalStorageDirectory();
		File appDir = new File(extStorageDir, "/Android/data/"+context.getPackageName()+"/files/");
		if (appDir.exists() == false){
			try{
				appDir.mkdirs();
			}catch(Exception e){
				Log.wtf("Creating home folder: "+appDir.getPath(), e);
			}
		}
		return appDir;
	}

	public static void resetSearchSourceType(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Preferences.SEARCH_SOURCE);
		editor.commit();
	}

	public static void setLastSearch(Context context, CharSequence value) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(LAST_SEARCH, value.toString());
		editor.commit();
	}

	public static CharSequence getLastSearch(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(LAST_SEARCH, "");
	}

	public boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo info : services) {
			if (info.service.getClassName().equals(FileProcessingService.class.getName())) {
				return true;
			}
		}

		return false;
	}
	
	public static ImportSettings getImportSettings(Context context){
		ImportSettings settings = new ImportSettings();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		settings.setBuildGrid(prefs.getBoolean("debug_import_build_grid", true));
		settings.setClearBeforeImport(prefs.getBoolean("debug_import_cleardb", true));
		
		updateSettings(settings,EntityType.Node, prefs.getString(NODES_INCLUDE, ""), true);
		updateSettings(settings,EntityType.Node, prefs.getString(NODES_EXCLUDE, ""), false);

		updateSettings(settings,EntityType.Way, prefs.getString(WAYS_INCLUDE, ""), true);
		updateSettings(settings,EntityType.Way, prefs.getString(WAYS_EXCLUDE, ""), false);

		updateSettings(settings,EntityType.Relation, prefs.getString(RELATIONS_INCLUDE, ""), true);
		updateSettings(settings,EntityType.Relation, prefs.getString(RELATIONS_EXCLUDE, ""), false);

 		settings.setImportAddresses(prefs.getBoolean("import_addresses", false));
 		
 		settings.setGridCellSize(Integer.parseInt(prefs.getString("grid_size", "1000")));
 		
		return settings;
	}

	private static void updateSettings(ImportSettings settings, EntityType type, String value, boolean isInclude) {
		String[] lines = value.split("\n");
		for(String l : lines){
			String[] t = l.split("=");
			if (t.length == 2){
				settings.setKeyValue(type, t[0], t[1], isInclude);
			}
		}
		
	}
	
	
	
	/*private void checkFileSize(String url){
		try {
			URL u = new URL(url);
			URLConnection conn;
			conn = u.openConnection();
			int totalSize = conn.getContentLength();
			if (totalSize>200*1024*1024) {
				ConfirmDialog.Confirm((Context) Preferences.this, getString(R.string.large_file_confirm), new ConfirmDialog.Action() {
					@Override
					public void PositiveAction() {
						
					}
				});
			}
		} catch (IOException e) {
			Log.wtf("checkFileSize", e);
		}
	}
	*/
}
