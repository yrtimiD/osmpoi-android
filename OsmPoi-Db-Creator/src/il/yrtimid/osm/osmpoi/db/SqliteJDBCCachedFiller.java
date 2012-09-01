/**
 * 
 */
package il.yrtimid.osm.osmpoi.db;

import java.sql.SQLException;

import il.yrtimid.osm.osmpoi.dal.IDbCachedFiller;
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
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addNodeIfBelongsToWay(il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addNodeIfBelongsToWay(Node node) throws SQLException {
		// TODO Auto-generated method stub
		addEntity(node);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addNodeIfBelongsToRelation(il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addNodeIfBelongsToRelation(Node node) throws SQLException {
		// TODO Auto-generated method stub
		addEntity(node);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addWayIfBelongsToRelation(il.yrtimid.osm.osmpoi.domain.Way)
	 */
	@Override
	public void addWayIfBelongsToRelation(Way way) throws SQLException {
		// TODO Auto-generated method stub
		addEntity(way);
	}

}
