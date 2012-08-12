/**
 * 
 */
package il.yrtimid.osm.osmpoi.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import il.yrtimid.osm.osmpoi.dal.IDbFiller;
import il.yrtimid.osm.osmpoi.dal.Queries;
import il.yrtimid.osm.osmpoi.domain.Bound;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Relation;
import il.yrtimid.osm.osmpoi.domain.RelationMember;
import il.yrtimid.osm.osmpoi.domain.Tag;
import il.yrtimid.osm.osmpoi.domain.Way;

/**
 * @author yrtimid
 *
 */
public class SqliteJDBCFiller extends SqliteJDBCCreator implements IDbFiller {

	
	/**
	 * @throws Exception 
	 * 
	 */
	public SqliteJDBCFiller(String filePath) throws Exception {
		super(filePath);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#clearAll()
	 */
	@Override
	public void clearAll() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#initGrid()
	 */
	@Override
	public void initGrid() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addEntity(il.yrtimid.osm.osmpoi.domain.Entity)
	 */
	@Override
	public void addEntity(Entity entity) {
		if (entity instanceof Node)
			addNode((Node) entity);
		else if (entity instanceof Way)
			addWay((Way) entity);
		else if (entity instanceof Relation)
			addRelation((Relation) entity);
		else if (entity instanceof Bound)
			addBound((Bound)entity);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addBound(il.yrtimid.osm.osmpoi.domain.Bound)
	 */
	@Override
	public void addBound(Bound bound) {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT INTO "+Queries.BOUNDS_TABLE+" (top, bottom, left, right) VALUES(?,?,?,?)");
			statement.setDouble(1, bound.getTop());
			statement.setDouble(2, bound.getBottom());
			statement.setDouble(3, bound.getLeft());
			statement.setDouble(4, bound.getRight());
			long id = statement.executeUpdate();
			if (id == -1)
				throw new SQLException("Bound was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addNode(il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addNode(Node node) {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT INTO "+Queries.NODES_TABLE+" (id, timestamp, lat, lon, grid_id) VALUES(?,?,?,?,?)");
			
			statement.setLong(1, node.getId());
			statement.setLong(2, node.getTimestamp());
			statement.setDouble(3,  node.getLatitude());
			statement.setDouble(4,  node.getLongitude());
			statement.setInt(5, 1);

			long id = statement.executeUpdate();
			if (id == -1)
				throw new SQLException("Node was not inserted");

			Collection<Tag> tags = node.getTags();
			long nodeId = node.getId();
			for (Tag tag : tags) {
				addNodeTag(nodeId, tag);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addNodes(java.util.Collection)
	 */
	@Override
	public void addNodes(Collection<Node> nodes) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addNodeTag(long, il.yrtimid.osm.osmpoi.domain.Tag)
	 */
	@Override
	public void addNodeTag(long nodeId, Tag tag) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addNodesTags(java.util.Collection)
	 */
	@Override
	public void addNodesTags(Collection<Node> nodes) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addWay(il.yrtimid.osm.osmpoi.domain.Way)
	 */
	@Override
	public void addWay(Way way) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addWayTag(long, il.yrtimid.osm.osmpoi.domain.Tag)
	 */
	@Override
	public void addWayTag(long wayId, Tag tag) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addWayNode(long, int, il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addWayNode(long wayId, int index, Node wayNode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addRelation(il.yrtimid.osm.osmpoi.domain.Relation)
	 */
	@Override
	public void addRelation(Relation rel) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addRelationTag(long, il.yrtimid.osm.osmpoi.domain.Tag)
	 */
	@Override
	public void addRelationTag(long relId, Tag tag) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addRelationMember(long, int, il.yrtimid.osm.osmpoi.domain.RelationMember)
	 */
	@Override
	public void addRelationMember(long relId, int index, RelationMember mem) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#optimizeGrid(java.lang.Integer)
	 */
	@Override
	public void optimizeGrid(Integer maxItems) {
		// TODO Auto-generated method stub

	}

}
