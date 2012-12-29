// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * A data class representing a single OSM relation.
 * 
 * @author Brett Henderson
 */
public class Relation extends Entity{
	private List<RelationMember> members;
	
	protected Relation(){
		this(new CommonEntityData());
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 */
	public Relation(CommonEntityData entityData) {
		super(entityData);
		
		this.members = new ArrayList<RelationMember>();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 * @param members
	 *            The members to apply to the object.
	 */
	public Relation(CommonEntityData entityData, List<RelationMember> members) {
		super(entityData);
		
		this.members = new ArrayList<RelationMember>(members);
	}
	
	public Relation(long relationId){
		this(new CommonEntityData(relationId, -1));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Relation;
	}
	
	public List<RelationMember> getMembers() {
		return members;
	}

    /** 
     * ${@inheritDoc}.
     */
    @Override
    public String toString() {
        String type = null;
        Collection<Tag> tags = getTags();
        for (Tag tag : tags) {
            if (tag.getKey() != null && tag.getKey().equalsIgnoreCase("type")) {
                type = tag.getValue();
                break;
            }
        }
        if (type != null) {
            return "Relation(id=" + getId() + ", #tags=" +  getTags().size() + ", type='" + type + "')";
        }
        return "Relation(id=" + getId() + ", #tags=" +  getTags().size() + ")";
    }

    

}
