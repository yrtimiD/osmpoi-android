// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.Collection;


/**
 * A data class representing a single OSM node.
 * 
 * @author Brett Henderson
 */
public class Node extends Entity{

	protected double latitude;
	protected double longitude;

	protected Node(){
		this(new CommonEntityData(), 0.0, 0.0);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 * @param latitude
	 *            The geographic latitude.
	 * @param longitude
	 *            The geographic longitude.
	 */
	public Node(CommonEntityData entityData, double latitude, double longitude) {
		super(entityData);

		this.latitude = latitude;
		this.longitude = longitude;
	}


	public Node(long nodeId){
		this(new CommonEntityData(nodeId, -1), 0L, 0L);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Node;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Node) {
			return compareTo((Node) o) == 0;
		} else {
			return false;
		}
	}

	/**
	 * Gets the latitude.
	 * 
	 * @return The latitude.
	 */
	public double getLatitude() {
		return latitude;
	}


	/**
	 * Sets the latitude.
	 * 
	 * @param latitude
	 *            The latitude.
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}


	/**
	 * Gets the longitude.
	 * 
	 * @return The longitude.
	 */
	public double getLongitude() {
		return longitude;
	}


	/**
	 * Sets the longitude.
	 * 
	 * @param longitude
	 *            The longitude.
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public boolean hasTags(){
		return this.getTags().size()>0;
	}

	/**
	 * ${@inheritDoc}.
	 */
	@Override
	public String toString() {
		String name = null;
		Collection<Tag> tags = getTags();
		for (Tag tag : tags) {
			if (tag.getKey() != null && tag.getKey().equalsIgnoreCase("name")) {
				name = tag.getValue();
				break;
			}
		}
		if (name != null) {
			return "Node(id=" + getId() + ", #tags=" + getTags().size() + ", name='" + name + "')";
		}
		return "Node(id=" + getId() + ", #tags=" + getTags().size() + ")";
	}


}
