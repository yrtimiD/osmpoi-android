// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;


/**
 * An enum representing the different data types in the OSM data model.
 * 
 * @author Brett Henderson
 */
public enum EntityType {
	None,
	/**
	 * Representation of the latitude/longitude bounding box of the entity stream.
	 */
	Bound,
	
	/**
	 * Represents a geographical point.
	 */
	Node,
	
	/**
	 * Represents a set of segments forming a path.
	 */
	Way,
	
	/**
	 * Represents a relationship between multiple entities.
	 */
	Relation
}
