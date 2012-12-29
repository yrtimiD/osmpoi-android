// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * A data class representing a single OSM way.
 * 
 * @author Brett Henderson
 */
public class Way extends Entity{
	
	private List<Node> nodes;
	private List<WayNode> wayNodes;
	
	protected Way() {
		this(new CommonEntityData());
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 */
	public Way(CommonEntityData entityData) {
		super(entityData);
		
		this.nodes = new ArrayList<Node>();
		this.wayNodes = new ArrayList<WayNode>();
	}
	
	public Way(long wayId){
		this(new CommonEntityData(wayId));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 * @param wayNodes
	 *            The way nodes to apply to the object
	 */
	public Way(CommonEntityData entityData, List<Node> nodes) {
		super(entityData);
		
		this.nodes = new ArrayList<Node>(nodes);
		this.wayNodes = new ArrayList<WayNode>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Way;
	}
	
	public List<Node> getNodes() {
		return nodes;
	}

	public List<WayNode> getWayNodes() {
		return wayNodes;
	}
	
    /**
     * Is this way closed? (A way is closed if the first node id equals the last node id.)
     *
     * @return True or false
     */
    public boolean isClosed() {
        return nodes.get(0).getId() == nodes.get(nodes.size() - 1).getId();
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
            return "Way(id=" + getId() + ", #tags=" +  getTags().size() + ", name='" + name + "')";
        }
        return "Way(id=" + getId() + ", #tags=" +  getTags().size() + ")";
    }
    
}
