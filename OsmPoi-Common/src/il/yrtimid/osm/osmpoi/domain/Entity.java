// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.Collection;

/**
 * A data class representing a single OSM entity. All top level data types
 * inherit from this class.
 * 
 * @author Brett Henderson
 */
public abstract class Entity implements Comparable<Entity>{
	
	protected CommonEntityData entityData;
	
	protected Entity(){}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The data to store in the entity. This instance is used directly and is not cloned.
	 */
	public Entity(CommonEntityData entityData) {
		this.entityData = entityData;
	}
	
	/**
	 * Returns the specific data type represented by this entity.
	 * 
	 * @return The entity type enum value.
	 */
	public abstract EntityType getType();


	/**
	 * Gets the identifier.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return entityData.getId();
	}


	/**
	 * Sets the identifier.
	 * 
	 * @param id
	 *            The identifier.
	 */
	public void setId(long id) {
		entityData.setId(id);
	}
	
	/**
	 * Gets the timestamp in date form. This is the standard method for
	 * retrieving timestamp information.
	 * 
	 * @return The timestamp.
	 */
	public long getTimestamp() {
		return entityData.getTimestamp();
	}


	/**
	 * Sets the timestamp in date form. This is the standard method of updating a timestamp.
	 * 
	 * @param timestamp
	 *            The timestamp.
	 */
	public void setTimestamp(long timestamp) {
		entityData.setTimestamp(timestamp);
	}

	/**
	 * Returns the attached tags. If the class is read-only, the collection will
	 * be read-only.
	 * 
	 * @return The tags.
	 */
	public TagCollection getTags() {
		return entityData.getTags();
	}
	
	/**
	 * Compares the tags on this entity to the specified tags. The tag
	 * comparison is based on a comparison of key and value in that order.
	 * 
	 * @param comparisonTags
	 *            The tags to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	protected int compareTags(Collection<Tag> comparisonTags) {
		return entityData.compareTags(comparisonTags);
	}
	
	public int compareTo(Entity o){
		if (this.getType() != o.getType()){
			return this.getType().compareTo(o.getType());
		}else {
			return (Long.valueOf(this.getId())).compareTo(Long.valueOf(o.getId()));
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Entity){
			Entity e = (Entity)o;
			return this.getType() == e.getType() && this.getId() == e.getId(); 
		}else{
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		/*
		 * As per the hashCode definition, this doesn't have to be unique it just has to return the
		 * same value for any two objects that compare equal. Using both id and version will provide
		 * a good distribution of values but is simple to calculate.
		 */
		return (int) getId();
	}
}
