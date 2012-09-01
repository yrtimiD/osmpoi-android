/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import il.yrtimid.osm.osmpoi.domain.Bound;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Relation;
import il.yrtimid.osm.osmpoi.domain.Way;

import java.sql.SQLException;

/**
 * @author yrtimid
 *
 */
public interface IDbFiller extends IDatabase{

	public abstract void clearAll() throws Exception;

	public abstract void initGrid() throws SQLException;

	public abstract void addEntity(Entity entity) throws SQLException;

	public abstract void addBound(Bound bound) throws SQLException;
	
	public abstract void addNode(Node node) throws SQLException;

	//public abstract void addNodes(Collection<Node> nodes);

	public abstract void addNodeTags(Node node) throws SQLException;
	
	//public abstract void addNodeTag(long nodeId, Tag tag);

	//public abstract void addNodesTags(Collection<Node> nodes);

	public abstract void addWay(Way way) throws SQLException;

	//public abstract void addWayTag(long wayId, Tag tag);
	
	public abstract void addWayTags(Way way) throws SQLException;
	
	//public abstract void addWayNode(long wayId, int index, Node wayNode);
	
	public abstract void addWayNodes(Way way) throws SQLException;

	public abstract void addRelation(Relation rel) throws SQLException;

	//public abstract void addRelationTag(long relId, Tag tag);
	
	public abstract void addRelationTags(Relation rel) throws SQLException;

	//public abstract void addRelationMember(long relId, int index, RelationMember mem);

	public abstract void addRelationMembers(Relation rel) throws SQLException;

	/**
	 * Splits large cells until no cells with node count greater than maxItems least
	 * @param maxItems
	 */
	public abstract void optimizeGrid(Integer maxItems) throws SQLException;

}