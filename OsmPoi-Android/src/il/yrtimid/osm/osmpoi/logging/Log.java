/**
 * 
 */
package il.yrtimid.osm.osmpoi.logging;


/**
 * @author yrtimid
 * use setprop log.tag.OsmPoi VERBOSE to enable logging
 */
public class Log {
		public static final String TAG = "OsmPoi";

//		public static void i(String tag, String string) {
//			if (android.util.Log.isLoggable(TAG, android.util.Log.INFO))
//				android.util.Log.i(tag, string);
//		}
//
//		public static void w(String tag, String string) {
//			if (android.util.Log.isLoggable(TAG, android.util.Log.WARN))
//		    android.util.Log.w(tag, string);
//		}
//		
//		public static void e(String tag, String string) {
//			if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR))
//		    android.util.Log.e(tag, string);
//		}
//		
//		public static void d(String tag, String string) {
//			if (android.util.Log.isLoggable(TAG, android.util.Log.DEBUG))
//		    android.util.Log.d(tag, string);
//		}
//		
//		public static void v(String tag, String string) {
//			if (android.util.Log.isLoggable(TAG, android.util.Log.VERBOSE))
//		    android.util.Log.v(tag, string);
//		}
//		
//		public static void wtf(String tag, String message, Throwable throwable) {
//			android.util.Log.wtf(tag, message, throwable);
//		}
		
		public static void i(String string) {
			if (android.util.Log.isLoggable(TAG, android.util.Log.INFO))
				android.util.Log.i(TAG, string);
		}

		public static void w(String string) {
			if (android.util.Log.isLoggable(TAG, android.util.Log.WARN))
		    android.util.Log.w(TAG, string);
		}
		
		public static void e(String string) {
			if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR))
		    android.util.Log.e(TAG, string);
		}
		
		public static void d(String string) {
			if (android.util.Log.isLoggable(TAG, android.util.Log.DEBUG))
		    android.util.Log.d(TAG, string);
		}
		
		public static void v(String string) {
			if (android.util.Log.isLoggable(TAG, android.util.Log.VERBOSE))
		    android.util.Log.v(TAG, string);
		}
		
		public static void wtf(String message, Throwable throwable) {
			android.util.Log.e(TAG, message, throwable);
		}
}
