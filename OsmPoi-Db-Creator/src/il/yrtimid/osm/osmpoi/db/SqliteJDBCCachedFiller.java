/**
 * 
 */
package il.yrtimid.osm.osmpoi.db;

import il.yrtimid.osm.osmpoi.dal.IDbCachedFiller;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Way;

/**
 * @author yrtimid
 *
 */
public class SqliteJDBCCachedFiller extends SqliteJDBCFiller implements IDbCachedFiller {

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
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#endAdd()
	 */
	@Override
	public void endAdd() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addNodeIfBelongsToWay(il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addNodeIfBelongsToWay(Node node) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addNodeIfBelongsToRelation(il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addNodeIfBelongsToRelation(Node node) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addWayIfBelongsToRelation(il.yrtimid.osm.osmpoi.domain.Way)
	 */
	@Override
	public void addWayIfBelongsToRelation(Way way) {
		// TODO Auto-generated method stub
		
	}

}
