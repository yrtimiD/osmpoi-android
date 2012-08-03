/**
 * 
 */
package il.yrtimid.osm.osmpoi.pbf;

import il.yrtimid.osm.osmpoi.domain.Entity;

/**
 * @author yrtimid
 *
 */
public class PeriodicProgressNotifier implements SinkSource {
	private Sink sink;
	private ProgressTracker progressTracker;
	private ProgressNotifier progressNotifier;
	private Long count = 0L;
	
	public PeriodicProgressNotifier(int interval, ProgressNotifier notifier) {
		progressTracker = new ProgressTracker(interval);
		progressNotifier = notifier;
	}
	
	public Long getCount(){
		return count;
	}
	
	public void process(Entity entity) {
		count++;
		if (progressTracker.updateRequired() && progressNotifier != null) {
				progressNotifier.onProgressChange(new ProgressNotifier.Progress(count, (int)progressTracker.getObjectsPerSecond()+ " obj/sec"));
		}
		
		sink.process(entity);
	}
	
	public void complete() {
		if (progressNotifier != null){
			progressNotifier.onProgressChange(new ProgressNotifier.Progress(count, count, (int)progressTracker.getObjectsPerSecond()+ " obj/sec"));
		}

		sink.complete();
	}
	
	
	public void release() {
		sink.release();
	}
	
	
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
