/**
 * 
 */
package il.yrtimid.osm.osmpoi.pbf;

import il.yrtimid.osm.osmpoi.domain.Entity;

/**
 * @author yrtimid
 *
 */
public class NullSink implements Sink {

	/* (non-Javadoc)
	 * @see org.openstreetmap.osmosis.core.lifecycle.Completable#complete()
	 */
	@Override
	public void complete() {
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.osmosis.core.lifecycle.Releasable#release()
	 */
	@Override
	public void release() {
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.osmosis.core.task.v0_6.Sink#process(org.openstreetmap.osmosis.core.container.v0_6.EntityContainer)
	 */
	@Override
	public void process(Entity entity) {
	}

}
