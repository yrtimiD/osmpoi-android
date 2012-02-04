/**
 * 
 */
package il.yrtimid.osm.osmpoi.tagmatchers;

import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.EntityType;

/**
 * @author yrtimid
 *
 */
public class IdMatcher extends TagMatcher {

	EntityType entityType;
	Long id;
	
	public IdMatcher(EntityType type, Long id) {
		this.entityType = type;
		this.id = id;
	}	
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher#isMatch(java.lang.CharSequence, java.lang.CharSequence)
	 */
	@Override
	public Boolean isMatch(CharSequence key, CharSequence value) {
		return false;
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher#isMatch(il.yrtimid.osm.osmpoi.domain.Entity)
	 */
	@Override
	public Boolean isMatch(Entity entity) {
		return entity.getType() == entityType && entity.getId() == id;
	}

	/**
	 * @return the entityType
	 */
	public EntityType getEntityType() {
		return entityType;
	}
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	
}
