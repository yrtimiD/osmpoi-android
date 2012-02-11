package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.ImportSettings;
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.SearchSourceType;
import il.yrtimid.osm.osmpoi.dal.CachedDbOpenHelper;
import il.yrtimid.osm.osmpoi.services.FileProcessingService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
import android.widget.Toast;

//TODO: prevent executing more than one service task at a time
public class Preferences extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	public static final String SEARCH_SOURCE = "search_source";
	public static final String LAST_SEARCH = "last_search";

	public static final String RESULT_LANGUAGE = "result_language";
	public static final String IS_DB_ON_SDCARD = "is_db_on_sdcard";
	private static final String PREFERENCE_IMPORT_PBF = "preference_import_pbf";
	private static final String PREFERENCE_CLEAR_DB = "preference_clear_db";
	private static final String PREFERENCE_ABOUT = "preference_about";
	
	
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
		findPreference(PREFERENCE_ABOUT).setOnPreferenceClickListener(this);
		findPreference(IS_DB_ON_SDCARD).setOnPreferenceChangeListener(this);
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
			intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, Environment.getExternalStorageDirectory().getPath());
			startActivityForResult(intent, INTERNAL_PICK_FILE_REQUEST_FOR_IMPORT);
			return true;
		} else if (PREFERENCE_CLEAR_DB.equals(key)) {
			ConfirmDialog.Confirm((Context) Preferences.this, getString(R.string.clean_db_confirm), new ConfirmDialog.Action() {
				@Override
				public void PositiveAction() {
					runClearDbService();
				}
			});
		} else if (PREFERENCE_ABOUT.equals(key)) {
			startActivity(new Intent(this, AboutActivity.class));
			return true;
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

	public void runPbfImportService(final File f) {
		Intent serviceIntent = new Intent(Preferences.this, FileProcessingService.class);
		serviceIntent.putExtra(FileProcessingService.EXTRA_OPERATION, FileProcessingService.Operation.IMPORT_TO_DB.name());
		serviceIntent.putExtra(FileProcessingService.EXTRA_FILE_PATH, f.getPath());
		startService(serviceIntent);
	}

	public void runClearDbService() {
		Intent serviceIntent = new Intent(Preferences.this, FileProcessingService.class);
		serviceIntent.putExtra(FileProcessingService.EXTRA_OPERATION, FileProcessingService.Operation.CLEAR_DB.name());
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
		
		settings.setBuildGrid(prefs.getBoolean("preference_import_build_grid", true));
		settings.setClearBeforeImport(prefs.getBoolean("preference_clear_db_before_import", true));
		
		settings.setNodeKey("highway", prefs.getBoolean("include_nodes_highway", false));
		settings.setNodeKey("*", prefs.getBoolean("include_nodes_other", true));
		
		settings.setWayKey("building",prefs.getBoolean("include_way_building", true));
		settings.setWayKey("highway",prefs.getBoolean("include_way_highway", true));
		settings.setWayKey("*", prefs.getBoolean("include_ways_other", false));
		
		
		settings.setRelationKey("landuse",prefs.getBoolean("include_relation_landuse", false)); 
		settings.setRelationKey("natural",prefs.getBoolean("include_relation_natural", false)); 
		settings.setRelationKey("leisure",prefs.getBoolean("include_relation_leisure", false)); 
		settings.setRelationKey("boundary",prefs.getBoolean("include_relation_boundary", false)); 
		settings.setRelationKey("area",prefs.getBoolean("include_relation_area", false)); 
		settings.setRelationKey("waterway",prefs.getBoolean("include_relation_waterway", false)); 
 		settings.setRelationKey("*",prefs.getBoolean("include_relations_other", true)); 
		
		return settings;
	}
}
