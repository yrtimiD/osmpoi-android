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
public class Way extends Entity implements Comparable<Way> {
	
	private List<Node> wayNodes;
	
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
		
		this.wayNodes = new ArrayList<Node>();
	}
	
	public Way(long wayId){
		this(new CommonEntityData(wayId, -1));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 * @param wayNodes
	 *            The way nodes to apply to the object
	 */
	public Way(CommonEntityData entityData, List<Node> wayNodes) {
		super(entityData);
		
		this.wayNodes = new ArrayList<Node>(wayNodes);
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Way;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Way) {
			return compareTo((Way) o) == 0;
		} else {
			return false;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return (int) getId();
	}


	/**
	 * Compares this node list to the specified node list. The comparison is
	 * based on a direct comparison of the node ids.
	 * 
	 * @param comparisonWayNodes
	 *            The node list to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	protected int compareNodes(List<Node> comparisonWayNodes) {
		Iterator<Node> i;
		Iterator<Node> j;
		
		// The list with the most entities is considered bigger.
		if (wayNodes.size() != comparisonWayNodes.size()) {
			return wayNodes.size() - comparisonWayNodes.size();
		}
		
		// Check the individual way nodes.
		i = wayNodes.iterator();
		j = comparisonWayNodes.iterator();
		while (i.hasNext()) {
			int result = i.next().compareTo(j.next());
			
			if (result != 0) {
				return result;
			}
		}
		
		// There are no differences.
		return 0;
	}


	/**
	 * Compares this way to the specified way. The way comparison is based on a
	 * comparison of id, version, timestamp, wayNodeList and tags in that order.
	 * 
	 * @param comparisonWay
	 *            The way to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	public int compareTo(Way comparisonWay) {
		int wayNodeListResult;
		
		if (this.getId() < comparisonWay.getId()) {
			return -1;
		}
		if (this.getId() > comparisonWay.getId()) {
			return 1;
		}
		
		
		
		if (this.getTimestamp()<comparisonWay.getTimestamp()) {
			return -1;
		}
		if (this.getTimestamp()>comparisonWay.getTimestamp()) {
			return 1;
		}
		
		wayNodeListResult = compareNodes(
			comparisonWay.getWayNodes()
		);
		
		if (wayNodeListResult != 0) {
			return wayNodeListResult;
		}
		
		return compareTags(comparisonWay.getTags());
	}
	
	public List<Node> getWayNodes() {
		return wayNodes;
	}

    /**
     * Is this way closed? (A way is closed if the first node id equals the last node id.)
     *
     * @return True or false
     */
    public boolean isClosed() {
        return wayNodes.get(0).getId() == wayNodes.get(wayNodes.size() - 1).getId();
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
