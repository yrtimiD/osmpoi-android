/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import il.yrtimid.osm.osmpoi.categories.CategoriesLoader;
import il.yrtimid.osm.osmpoi.categories.Category;
import il.yrtimid.osm.osmpoi.dal.CachedDbOpenHelper;
import il.yrtimid.osm.osmpoi.dal.DbAnalyzer;
import il.yrtimid.osm.osmpoi.dal.DbSearcher;
import il.yrtimid.osm.osmpoi.dal.DbStarred;
import il.yrtimid.osm.osmpoi.dal.IDbCachedFiller;
import il.yrtimid.osm.osmpoi.formatters.EntityFormatter;
import il.yrtimid.osm.osmpoi.formatters.EntityFormattersLoader;
import il.yrtimid.osm.osmpoi.logging.AndroidLogHandler;
import il.yrtimid.osm.osmpoi.logging.Log;
import il.yrtimid.osm.osmpoi.ui.Preferences;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;

import android.preference.PreferenceManager;

/**
 * @author yrtimid
 * 
 */
public class OsmPoiApplication extends Application {
	public static ISearchSource searchSource;
	public static Category mainCategory;
	private static Location location;
	public static LocationChangeManager locationManager;
	public static OrientationChangeManager orientationManager;
	public static Databases databases;
	public static List<EntityFormatter> formatters;
	private static final String POI_DATABASE_NAME = "poi.db";
	private static final String ADDRESS_DATABASE_NAME = "address.db";
	private static final String STARRED_DATABASE_NAME = "starred.db";
	
	
	
	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		OsmPoiApplication.locationManager = new LocationChangeManager(getApplicationContext());
		OsmPoiApplication.orientationManager = new OrientationChangeManager(getApplicationContext());
		OsmPoiApplication.databases = new Databases(getApplicationContext());
		
		Logger logger = Logger.getLogger(Log.TAG);
		logger.addHandler(new AndroidLogHandler());
	} 

	public static Boolean hasLocation(){
		return location!=null;
	}
	
	public static Location getCurrentLocation() {
		return location;
	}

	public static Point getCurrentLocationPoint() {
		return new Point(location.getLatitude(), location.getLongitude());
	}

	
	public static boolean setCurrentLocation(Location location) {
		if (Util.isBetterLocation(location, OsmPoiApplication.location)) {
			OsmPoiApplication.location = location;
			//currentSearch.setCenter( new Point(location.getLatitude(), location.getLongitude()));
			return true;
		}
		else {
			return false;
		}
	}
	
	public static class Config {
		public static void reset(Context context){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = prefs.edit();
			editor.clear();
			editor.commit();
		}
		
		public static void reloadConfig(Context context) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

			searchSourceType = (SearchSourceType) Enum.valueOf(SearchSourceType.class, prefs.getString(Preferences.SEARCH_SOURCE, "NONE"));
			resultLanguage = prefs.getString(Preferences.RESULT_LANGUAGE, "");

			setupDbLocation(context);
			
			OsmPoiApplication.databases.reset();
			
			tryCreateSearchSource(context);
			
			OsmPoiApplication.mainCategory = CategoriesLoader.load(context, OsmPoiApplication.searchSource);
		
			OsmPoiApplication.formatters = EntityFormattersLoader.load(context);

		}

		private static Boolean setupDbLocation(Context context) {
			poiDbLocation = null;
			addressDbLocation = null;
			
			try{
				File folder = Preferences.getHomeFolder(context);
				if (folder.canWrite()){
					poiDbLocation = new File(folder, POI_DATABASE_NAME);
					addressDbLocation = new File(folder, ADDRESS_DATABASE_NAME);
					starredDbLocation = new File(folder, STARRED_DATABASE_NAME);
				}else{
					throw new RuntimeException("DB path isn't writable: "+folder.getPath());
				}
			}catch(Exception e){
				Log.wtf("Checking external storage DB", e);
				throw new RuntimeException("Can't load databases",e);
			}
			
			return (poiDbLocation!=null) && (addressDbLocation!=null);
		}

		private static SearchSourceType searchSourceType;
		private static String resultLanguage;
		private static File poiDbLocation;
		private static File addressDbLocation;
		private static File starredDbLocation;
		
		public static SearchSourceType getSearchSourceType() {
			return searchSourceType;
		}

		public static String getResultLanguage() {
			return resultLanguage;
		}

		public static File getPoiDbLocation() {
			return poiDbLocation;
		}
		public static File getAddressDbLocation() {
			return addressDbLocation;
		}
		
		public static File getStarredDbLocation() {
			return starredDbLocation;
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
					OsmPoiApplication.searchSource = DBSearchSource.create(context);
					break;
				case ONLINE:
					OsmPoiApplication.searchSource = OverpassAPISearchSource.create(context);
					break;
				default:
					OsmPoiApplication.searchSource = EmptySearchSource.create(context);
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

	public class Databases {
		private Context context;
		private DbStarred starred;
		private DbSearcher poiSearcher;
		private DbSearcher addrSearcher;
		private DbAnalyzer poiAnalyzer;
		private DbAnalyzer addrAnalyzer;
		private CachedDbOpenHelper cachedPoiBasic;
		private CachedDbOpenHelper cachedAddrBasic;
		
		public Databases(Context context) {
			this.context = context;
		}
		
		public void reset(){
			this.starred = null;
		}
		
		public DbStarred getStarredDb(){
			if (starred == null) starred = new DbStarred(context,  OsmPoiApplication.Config.getStarredDbLocation());
			return starred;
		}
		
		public DbSearcher getPoiSearcherDb(){
			if (poiSearcher == null) poiSearcher = new DbSearcher(context,  OsmPoiApplication.Config.getPoiDbLocation());
			return poiSearcher;
		}
		
		public DbSearcher getAddressSearcherDb(){
			if (addrSearcher == null) addrSearcher = new DbSearcher(context,  OsmPoiApplication.Config.getAddressDbLocation());
			return addrSearcher;
		}

		
		public IDbCachedFiller getPoiDb(){
			if (cachedPoiBasic == null) cachedPoiBasic = new CachedDbOpenHelper(context,  OsmPoiApplication.Config.getPoiDbLocation());
			return cachedPoiBasic;
		}
		
		public IDbCachedFiller getAddressDb(){
			if (cachedAddrBasic == null) cachedAddrBasic = new CachedDbOpenHelper(context,  OsmPoiApplication.Config.getAddressDbLocation());
			return cachedAddrBasic;
		}
		
		public DbAnalyzer getPoiAnalizerDb(){
			if (poiAnalyzer == null) poiAnalyzer = new DbAnalyzer(context,  OsmPoiApplication.Config.getPoiDbLocation());
			return poiAnalyzer;
		}

		public DbAnalyzer getAddressAnalizerDb(){
			if (addrAnalyzer == null) addrAnalyzer = new DbAnalyzer(context,  OsmPoiApplication.Config.getAddressDbLocation());
			return addrAnalyzer;
		}

		
	}
}

