/**
 * 
 */
package il.yrtimid.osm.osmpoi.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import il.yrtimid.osm.osmpoi.dal.IDbCachedFiller;
import il.yrtimid.osm.osmpoi.dal.Queries;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Way;

/**
 * @author yrtimid
 *
 */
public class SqliteJDBCCachedFiller extends SqliteJDBCFiller implements IDbCachedFiller {
	private static int MAX_UNCOMMITTED_ITEMS = 10000;
	int uncommittedItems = 0;
	
	/**
	 * @param filePath
	 * @throws Exception
	 */
	public SqliteJDBCCachedFiller(String filePath) throws Exception {
		super(filePath);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#beginAdd()
	 */
	@Override
	public void beginAdd() {
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#endAdd()
	 */
	@Override
	public void endAdd() {
		try {
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.db.SqliteJDBCFiller#addEntity(il.yrtimid.osm.osmpoi.domain.Entity)
	 */
	@Override
	public void addEntity(Entity entity) throws SQLException {
		super.addEntity(entity);
		uncommittedItems++;
		if (uncommittedItems>=MAX_UNCOMMITTED_ITEMS){
			try {
				conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			uncommittedItems = 0;
		}
	}
	

	@Override
	public void addNodeIfBelongsToWay(Node node) throws SQLException{
		Statement statement = null;
		ResultSet cur = null;
		boolean exists = false;
		try {
			statement = conn.createStatement();

			String sql = "SELECT way_id from "+Queries.WAY_NODES_TABLE+" WHERE node_id="+node.getId();
			cur = statement.executeQuery(sql);
			if (cur.next()){
				long way_id = cur.getLong(1);
				exists = true;
			}
			cur.close();
			statement.close();
		}finally{
			if (cur != null)
				cur.close();
			if (statement != null)
				statement.close();
		}
		
		if (exists)
			addEntity(node);
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addNodeIfBelongsToRelation(il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addNodeIfBelongsToRelation(Node node) throws SQLException{
		Statement statement = null;
		ResultSet cur = null;
		boolean exists = false;
		try {
			statement = conn.createStatement();

			String sql = "SELECT relation_id from "+Queries.MEMBERS_TABLE+" WHERE type='Node' AND ref="+node.getId();
			cur = statement.executeQuery(sql);
			if (cur.next()){
				long way_id = cur.getLong(1);
				exists = true;
			}
			cur.close();
			statement.close();
		}finally{
			if (cur != null)
				cur.close();
			if (statement != null)
				statement.close();
		}
		
		if (exists)
			addEntity(node);
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addWayIfBelongsToRelation(il.yrtimid.osm.osmpoi.domain.Way)
	 */
	@Override
	public void addWayIfBelongsToRelation(Way way) throws SQLException{
		Statement statement = null;
		ResultSet cur = null;
		boolean exists = false;
		try {
			statement = conn.createStatement();

			String sql = "SELECT relation_id from "+Queries.MEMBERS_TABLE+" WHERE type='Way' AND ref="+way.getId();
			cur = statement.executeQuery(sql);
			if (cur.next()){
				long way_id = cur.getLong(1);
				exists = true;
			}
			cur.close();
			statement.close();
		}finally{
			if (cur != null)
				cur.close();
			if (statement != null)
				statement.close();
		}
		
		if (exists)
			addEntity(way);
	}

}
