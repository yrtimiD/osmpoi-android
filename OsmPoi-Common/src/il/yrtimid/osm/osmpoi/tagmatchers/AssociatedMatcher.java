/**
 * 
 */
package il.yrtimid.osm.osmpoi.tagmatchers;

import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.domain.EntityType;

/**
 * @author yrtimid
 *
 */
public class AssociatedMatcher extends TagMatcher {

	EntityType entityType;
	Long id;
	
	public AssociatedMatcher(EntityType type, Long id) {
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
		switch (entity.getType()){
		case Node:
			break;
		case Way:
			if (this.entityType.equals(EntityType.Node)){
				for(Node n : ((Way)entity).getWayNodes()){
					if (n.getId() == id) return true;
				}
			}
			break;
		case Relation:
			for(RelationMember m : ((Relation)entity).getMembers()){
				if (this.entityType.equals(m.getMemberType()) && this.id == m.getMemberId()) return true;
			}
			break;
		}
				
		return false;
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
