/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import java.util.List;

import il.yrtimid.osm.osmpoi.categories.Category;
import il.yrtimid.osm.osmpoi.formatters.EntityFormatter;
import il.yrtimid.osm.osmpoi.parcelables.SearchParameters;
import il.yrtimid.osm.osmpoi.ui.Preferences;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import android.preference.PreferenceManager;

/**
 * @author yrtimid
 * 
 */
public class OsmPoiApplication extends Application {
	public static ISearchSource searchSource;
	public static SearchParameters currentSearch;
	public static Category mainCategory;
	private static Location location;
	public static List<EntityFormatter> formatters;

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		OsmPoiApplication.locationManager = new LocationChangeManager(getApplicationContext());
	}
	
	public static Boolean hasLocation(){
		return location!=null;
	}
	
	public static Location getCurrentLocation() {
		return location;
	}

	public static boolean setCurrentLocation(Location location) {
		if (Util.isBetterLocation(location, OsmPoiApplication.location)) {
			OsmPoiApplication.location = location;
			currentSearch.setCenter( new Point(location.getLatitude(), location.getLongitude()));
			return true;
		}
		else {
			return false;
		}
	}

	public static class Config {
		public static void reloadConfig(Context context) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

			searchSourceType = (SearchSourceType) Enum.valueOf(SearchSourceType.class, prefs.getString(Preferences.SEARCH_SOURCE, "NONE"));
			resultLanguage = prefs.getString(Preferences.RESULT_LANGUAGE, "");

			if (currentSearch == null){
				currentSearch = new SearchParameters();
			}
			
			if (searchSource == null)
				tryCreateSearchSource(context);
		}

		private static SearchSourceType searchSourceType;
		private static String resultLanguage;

		public static SearchSourceType getSearchSourceType() {
			return searchSourceType;
		}

		public static String getResultLanguage() {
			return resultLanguage;
		}

		public static Boolean tryCreateSearchSource(Context context) {
			return tryCreateSearchSource(context, searchSourceType);
		}

		public static Boolean tryCreateSearchSource(Context context, SearchSourceType type) {
			try {
				if (OsmPoiApplication.searchSource != null)
					OsmPoiApplication.searchSource.close();

				switch (type) {
				case DB:
					OsmPoiApplication.searchSource = new DBSearchSource(context);
					break;
				default:
					OsmPoiApplication.searchSource = null;
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				OsmPoiApplication.searchSource = null;
				return false;
			}

			return true;
		}

	}

	public static LocationChangeManager locationManager;
	
}
