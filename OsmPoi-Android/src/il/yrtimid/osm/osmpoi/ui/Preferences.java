package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.ImportSettings;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.logging.Log;

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
import android.widget.Toast;

//TODO: prevent executing more than one service task at a time
public class Preferences extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	public static final String SEARCH_SOURCE = "search_source";
	public static final String LAST_SEARCH = "last_search";
	public static final String RESULT_LANGUAGE = "result_language";
	public static final String INSTALL_DB = "preference_install_db";

	private static final int INTERNAL_PICK_FILE_REQUEST_FOR_IMPORT = 1;
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		findPreference(INSTALL_DB).setOnPreferenceClickListener(this);
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

	/*
	 * (non-Javadoc)
	 * 
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
		if (INSTALL_DB.equals(key)) {
			Intent intent = new Intent(this, FilePickerActivity.class);
			intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, android.os.Environment.getExternalStorageDirectory().getPath());
			startActivityForResult(intent, INTERNAL_PICK_FILE_REQUEST_FOR_IMPORT);
			return true;
		}
		return false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTERNAL_PICK_FILE_REQUEST_FOR_IMPORT) {
			if (resultCode == RESULT_OK) {
				final File f = new File(data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
				installOfflineDbFile(f);
			}
		}
	}

	private void installOfflineDbFile(File source) {
		try {
			File home = getHomeFolder(this);
			String fileName = source.getName();
			File target = new File(home.getAbsolutePath() + "/" + fileName);
			Boolean success = source.renameTo(target);
			
			if (success){
				Toast.makeText(this, fileName + " installed successfully", Toast.LENGTH_SHORT).show();
			}else {
				//TODO: implement fallback and use copyFile
				Toast.makeText(this, fileName + " was not installed!", Toast.LENGTH_LONG).show();
			}
		} catch (Exception ex) {
			Log.wtf("Can't install offline db file", ex);
		}
	}

//	public static void copyFile(File sourceFile, File destFile) throws IOException {
//	    if(!destFile.exists()) {
//	        destFile.createNewFile();
//	    }
//
//	    FileChannel source = null;
//	    FileChannel destination = null;
//
//	    try {
//	        source = new FileInputStream(sourceFile).getChannel();
//	        destination = new FileOutputStream(destFile).getChannel();
//	        destination.transferFrom(source, 0, source.size());
//	    }
//	    finally {
//	        if(source != null) {
//	            source.close();
//	        }
//	        if(destination != null) {
//	            destination.close();
//	        }
//	    }
//	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange
	 * (android.preference.Preference, java.lang.Object)
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		/*
		 * if (SEARCH_SOURCE.equals(preference.getKey())) { SearchSourceType
		 * type = (SearchSourceType) Enum.valueOf(SearchSourceType.class,
		 * (String) newValue);
		 * 
		 * Boolean success =
		 * OsmPoiApplication.Config.tryCreateSearchSource(this, type); if
		 * (success) { return true; } else { Toast.makeText(this,
		 * R.string.cant_create_search_source, Toast.LENGTH_LONG).show(); }
		 * }else if (IS_DB_ON_SDCARD.equals(preference.getKey())){
		 * OsmPoiApplication.Config.reloadConfig(this); return true; } return
		 * false;
		 */

		return true;
	}

	public static File getHomeFolder(Context context) {
		// File appDir = context.getExternalFilesDir(null); //api 8+
		File extStorageDir = Environment.getExternalStorageDirectory();
		File appDir = new File(extStorageDir, "/Android/data/" + context.getPackageName() + "/files/");
		if (appDir.exists() == false) {
			try {
				appDir.mkdirs();
			} catch (Exception e) {
				Log.wtf("Creating home folder: " + appDir.getPath(), e);
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

	private static void updateSettings(ImportSettings settings, EntityType type, String value, boolean isInclude) {
		String[] lines = value.split("\n");
		for (String l : lines) {
			String[] t = l.split("=");
			if (t.length == 2) {
				settings.setKeyValue(type, t[0], t[1], isInclude);
			}
		}

	}
}
