/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import il.yrtimid.osm.osmpoi.domain.Bound;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Relation;
import il.yrtimid.osm.osmpoi.domain.RelationMember;
import il.yrtimid.osm.osmpoi.domain.Tag;
import il.yrtimid.osm.osmpoi.domain.Way;

import java.util.Collection;

/**
 * @author yrtimid
 *
 */
public interface IDbFiller extends IDatabase{

	public abstract void clearAll();

	public abstract void initGrid();

	public abstract void addEntity(Entity entity);

	public abstract void addBound(Bound bound);
	
	public abstract void addNode(Node node);

	public abstract void addNodes(Collection<Node> nodes);

	public abstract void addNodeTag(long nodeId, Tag tag);

	public abstract void addNodesTags(Collection<Node> nodes);

	public abstract void addWay(Way way);

	public abstract void addWayTag(long wayId, Tag tag);

	public abstract void addWayNode(long wayId, int index, Node wayNode);

	public abstract void addRelation(Relation rel);

	public abstract void addRelationTag(long relId, Tag tag);

	public abstract void addRelationMember(long relId, int index, RelationMember mem);

	/**
	 * Splits large cells until no cells with node count greater than maxItems least
	 * @param maxItems
	 */
	public abstract void optimizeGrid(Integer maxItems);

}