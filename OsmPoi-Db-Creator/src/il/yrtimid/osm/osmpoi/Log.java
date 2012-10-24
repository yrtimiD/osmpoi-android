/**
 * 
 */
package il.yrtimid.osm.osmpoi;


/**
 * @author yrtimid
 * use setprop log.tag.OsmPoi VERBOSE to enable logging
 */
public class Log {
		static final String TAG = "OsmPoi";
		
		public static void i(String string) {
			System.out.println(TAG+": "+string);
		}

		public static void w(String string) {
			System.out.println(TAG+": "+string);
		}
		
		public static void e(String string) {
			System.out.println(TAG+": "+string);
		}
		
		public static void d(String string) {
			System.out.println(TAG+": "+string);
		}
		
		public static void v(String string) {
			System.out.println(TAG+": "+string);
		}
		
		public static void wtf(String message, Throwable throwable) {
			System.out.println(TAG+": "+message);
			throwable.printStackTrace(System.out);
		}
}
