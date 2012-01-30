// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.pbf;

import il.yrtimid.osm.osmpoi.domain.Entity;


/**
 * Defines the interface for tasks consuming OSM data types.
 * 
 * @author Brett Henderson
 */
public interface Sink extends Task, Completable {
	
	/**
	 * Process the entity.
	 * 
	 * @param entityContainer
	 *            The entity to be processed.
	 */
	void process(Entity entity);
}
