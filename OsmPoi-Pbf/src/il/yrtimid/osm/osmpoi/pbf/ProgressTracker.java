/**
 * 
 */
package il.yrtimid.osm.osmpoi.pbf;

/**
* Maintains state about execution progress. It calculates when the next update
* is due, and provides statistics on execution.
* 
* @author Brett Henderson
*/
public class ProgressTracker {
	
	private int interval;
	private boolean initialized;
	private long firstUpdateTimestamp;
	private long lastUpdateTimestamp;
	private long objectCount;
	private double objectsPerSecond;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param interval
	 *            The interval between logging progress reports in milliseconds.
	 */
	public ProgressTracker(int interval) {
		this.interval = interval;
		
		initialized = false;
	}
	
	
	/**
	 * Indicates if an update is due. This should be called once per object that
	 * is processed.
	 * 
	 * @return True if an update is due.
	 */
	public boolean updateRequired() {
		if (!initialized) {
			lastUpdateTimestamp = System.currentTimeMillis();
			firstUpdateTimestamp = System.currentTimeMillis();
			objectCount = 0;
			objectsPerSecond = 0;
			
			initialized = true;
			
			return false;
			
		} else {
			long currentTimestamp;
			long duration;
			
			// Calculate the time since the last update.
			currentTimestamp = System.currentTimeMillis();
			duration = currentTimestamp - lastUpdateTimestamp;
			
			// Increment the processed object count.
			objectCount++;
			
			if (duration > interval || duration < 0) {
				lastUpdateTimestamp = currentTimestamp;
				
				// Calculate the number of objects processed per second.
				objectsPerSecond = (double) objectCount * 1000 / (currentTimestamp-firstUpdateTimestamp);
			
				return true;
				
			} else {
				return false;
			}
		}
	}
	
	
	/**
	 * Provides the number of objects processed per second. This only becomes
	 * valid after updateRequired returns true for the first time.
	 * 
	 * @return The number of objects processed per second in the last timing
	 *         interval.
	 */
	public double getObjectsPerSecond() {
		return objectsPerSecond;
	}
}
