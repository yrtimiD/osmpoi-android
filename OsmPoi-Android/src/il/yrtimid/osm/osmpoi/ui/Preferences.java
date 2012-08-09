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
	private static final String PREFERENCE_DEBUG_SHOW = "debug_show_debug_preferences";
	
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
		findPreference(PREFERENCE_DEBUG_SHOW).setOnPreferenceChangeListener(this);
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
		
		settings.setKey(EntityType.Node, "name*", true);
		settings.setKey(EntityType.Node, "highway", prefs.getBoolean("include_node_highway", false));
		settings.setKey(EntityType.Node, "building",prefs.getBoolean("include_node_building", false));
		settings.setKey(EntityType.Node, "barrier", prefs.getBoolean("include_node_barrier", false));
		settings.setKey(EntityType.Node, "*", prefs.getBoolean("include_node_other", true));
		
		settings.setKey(EntityType.Way, "name*", true);
		settings.setKey(EntityType.Way, "building",prefs.getBoolean("include_way_building", false));
		settings.setKey(EntityType.Way, "highway",prefs.getBoolean("include_way_highway", false));
		settings.setKey(EntityType.Way, "*", prefs.getBoolean("include_way_other", false));
		
		
		//settings.setKey(EntityType.Relation, "name*", true);
		settings.setKey(EntityType.Relation, "landuse",prefs.getBoolean("include_relation_landuse", false)); 
		settings.setKey(EntityType.Relation, "natural",prefs.getBoolean("include_relation_natural", false)); 
		settings.setKey(EntityType.Relation, "leisure",prefs.getBoolean("include_relation_leisure", false)); 
		settings.setKey(EntityType.Relation, "boundary",prefs.getBoolean("include_relation_boundary", false)); 
		settings.setKey(EntityType.Relation, "area",prefs.getBoolean("include_relation_area", false)); 
		settings.setKey(EntityType.Relation, "waterway",prefs.getBoolean("include_relation_waterway", false)); 
		settings.setKeyValue(EntityType.Relation, "type", "restriction", prefs.getBoolean("include_relation_restriction", false));
		settings.setKeyValue(EntityType.Relation, "type", "enforcement", prefs.getBoolean("include_relation_enforcement", false));
		settings.setKeyValue(EntityType.Relation, "type", "network", prefs.getBoolean("include_relation_network", true));
		settings.setKeyValue(EntityType.Relation, "type", "operator", prefs.getBoolean("include_relation_operator", true));
		settings.setKey(EntityType.Relation, "*",prefs.getBoolean("include_relation_other", false)); 
		
 		settings.setImportAddresses(prefs.getBoolean("import_addresses", false));
 		
 		settings.setGridSize(Integer.parseInt(prefs.getString("grid_size", "1000")));
 		
		return settings;
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
