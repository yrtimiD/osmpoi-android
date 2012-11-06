/**
 * 
 */
package il.yrtimid.osm.osmpoi.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import il.yrtimid.osm.osmpoi.Pair;
import il.yrtimid.osm.osmpoi.dal.IDbFiller;
import il.yrtimid.osm.osmpoi.dal.Queries;
import il.yrtimid.osm.osmpoi.Log;
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

	
	public SqliteJDBCFiller(String filePath) throws Exception {
		super(filePath);
	}

	@Override
	public void clearAll() throws Exception {
		drop();

		//dropAllTables(db);
		create();
	}

	@Override
	public void initGrid() throws SQLException{
		//db.execSQL("UPDATE "+NODES_TABLE+" SET grid_id=1");
		execSQL("DROP TABLE IF EXISTS "+Queries.GRID_TABLE);
		execSQL(Queries.SQL_CREATE_GRID_TABLE);
		
		String sql_generate_grid = "INSERT INTO grid (minLat, minLon, maxLat, maxLon)"
								+" SELECT min(lat) minLat, min(lon) minLon, max(lat) maxLat, max(lon) maxLon"
								+" FROM nodes";	
		Log.d(sql_generate_grid);
		execSQL(sql_generate_grid);
		
		String updateNodesGrid = "UPDATE nodes SET grid_id = 1 WHERE grid_id <> 1";
		Log.d(updateNodesGrid);
		execSQL(updateNodesGrid);
	}

	@Override
	public void addEntity(Entity entity) throws SQLException {
		if (entity instanceof Node)
			addNode((Node) entity);
		else if (entity instanceof Way)
			addWay((Way) entity);
		else if (entity instanceof Relation)
			addRelation((Relation) entity);
		else if (entity instanceof Bound)
			addBound((Bound)entity);
	}

	public void addBound(Bound bound) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT INTO "+Queries.BOUNDS_TABLE+" (top, bottom, left, right) VALUES(?,?,?,?)");
			statement.setDouble(1, bound.getTop());
			statement.setDouble(2, bound.getBottom());
			statement.setDouble(3, bound.getLeft());
			statement.setDouble(4, bound.getRight());
			int res = statement.executeUpdate();
			if (res != 1)
				throw new SQLException("Bound was not inserted");
		}finally{
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void addNode(Node node) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT OR IGNORE INTO "+Queries.NODES_TABLE+" (id, timestamp, lat, lon, grid_id) VALUES(?,?,?,?,?)");
			
			statement.setLong(1, node.getId());
			statement.setLong(2, node.getTimestamp());
			statement.setDouble(3,  node.getLatitude());
			statement.setDouble(4,  node.getLongitude());
			statement.setInt(5, 1);

			statement.executeUpdate();

			addNodeTags(node);
			
		}finally{
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void addNodeTags(Node node) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT OR IGNORE INTO "+Queries.NODES_TAGS_TABLE+" (node_id, k, v) VALUES(?,?,?)");

			final long id = node.getId();
			statement.setLong(1, id);

			for(Tag tag:node.getTags()){
				statement.setString(2, tag.getKey());
				statement.setString(3, tag.getValue());

				statement.executeUpdate();
			}
		}finally{
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void addWay(Way way) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT OR IGNORE INTO "+Queries.WAYS_TABLE+" (id, timestamp) VALUES(?,?)");
			
			statement.setLong(1, way.getId());
			statement.setLong(2, way.getTimestamp());

			statement.executeUpdate();

			addWayTags(way);
			addWayNodes(way);
			
		}finally{
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void addWayTags(Way way) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT OR IGNORE INTO "+Queries.WAY_TAGS_TABLE+" (way_id, k, v) VALUES(?,?,?)");

			statement.setLong(1, way.getId());

			for(Tag tag : way.getTags()){
				statement.setString(2, tag.getKey());
				statement.setString(3, tag.getValue());

				statement.executeUpdate();
			}
		}finally{
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void addWayNodes(Way way) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT OR IGNORE INTO "+Queries.WAY_NODES_TABLE+" (way_id, node_id) VALUES(?,?)");

			statement.setLong(1, way.getId());
			for(Node node : way.getWayNodes()){
				statement.setLong(2, node.getId());
				
				statement.executeUpdate();
			}
		}finally{
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void addRelation(Relation rel) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT OR IGNORE INTO "+Queries.RELATIONS_TABLE+" (id, timestamp) VALUES(?,?)");

			statement.setLong(1, rel.getId());
			statement.setLong(2, rel.getTimestamp());

			statement.executeUpdate();

			addRelationTags(rel);
			addRelationMembers(rel);
			
		}finally{
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void addRelationTags(Relation rel) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT OR IGNORE INTO "+Queries.RELATION_TAGS_TABLE+" (relation_id, k, v) VALUES(?,?,?)");

			final long id = rel.getId();
			statement.setLong(1, id);

			for(Tag tag : rel.getTags()){
				statement.setString(2, tag.getKey());
				statement.setString(3, tag.getValue());

				statement.executeUpdate();
			}
		}finally{
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void addRelationMembers(Relation rel) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement("INSERT OR IGNORE INTO "+Queries.MEMBERS_TABLE+" (relation_id, type, ref, role) VALUES(?,?,?,?)");
			statement.setLong(1, rel.getId());
			
			for(RelationMember mem : rel.getMembers()){
				statement.setString(2, mem.getMemberType().name());
				statement.setLong(3, mem.getMemberId());
				statement.setString(4, mem.getMemberRole());

				statement.executeUpdate();
			}
		}finally{
			if (statement != null)
				statement.close();
		}
	}

	@Override
	public void optimizeGrid(Integer maxItems) throws SQLException {
		Collection<Pair<Integer,Integer>> cells = null;
		conn.setAutoCommit(false);
		do{
			cells = getBigCells(maxItems);
			Log.d("OptimizeGrid: "+cells.size()+" cells needs optimization for "+maxItems+" items");
			for(Pair<Integer,Integer> cell : cells){
				Log.d("OptimizeGrid: cell_id="+cell.getA()+", cell size="+cell.getB());
				splitGridCell(cell.getA());
				conn.commit();
			}
		}while(cells.size() > 0);
	}
	
	/**
	 * finds cells which have nodes count greater than minItems
	 * @param minItems
	 * @return
	 * @throws SQLException 
	 */
	private Collection<Pair<Integer,Integer>> getBigCells(Integer minItems) throws SQLException{
		Statement statement = null;
		Collection<Pair<Integer,Integer>> gridIds = new ArrayList<Pair<Integer,Integer>>();
		ResultSet cur = null;
		try{
			statement = conn.createStatement();
			String sql = "SELECT grid_id, count(id) [count] FROM "+Queries.NODES_TABLE+" GROUP BY grid_id HAVING count(id)>"+minItems.toString();
			cur = statement.executeQuery(sql);
			while(cur.next()){
				Integer id = cur.getInt("grid_id");
				Integer count = cur.getInt("count");
				
				gridIds.add(new Pair<Integer, Integer>(id, count));
			}
		}finally{
			if (cur != null) 
				cur.close();
			if (statement != null) 
				statement.close();
		}
		return gridIds;
	}
	
	/**
	 * Splits cell into 4 pieces and updates theirs nodes with the new split
	 * @param id ID of the cell to split
	 * @throws SQLException 
	 */
	private void splitGridCell(Integer id) throws SQLException{
		Statement statement = null;
		PreparedStatement prepStatement = null;
		ResultSet cur = null;
		try {
			statement = conn.createStatement();

			Log.d("splitGridCell id:"+id);
			//calc new cell size to be 1/2 of the old one
			String getNewCellSizeSql = "SELECT round((maxLat-minLat)/2,7) dLat, round((maxLon-minLon)/2,7) dLon from "+Queries.GRID_TABLE+" WHERE id="+id.toString();
			cur = statement.executeQuery(getNewCellSizeSql);
			cur.next();
			Double newCellSizeLat = cur.getDouble(1);
			Double newCellSizeLon = cur.getDouble(2);
			cur.close();
			statement.close();
			
			Log.d("newCellSizeLat="+newCellSizeLat+" newCellSizeLon="+newCellSizeLon);
			
			String create4NewCellsSql;
			create4NewCellsSql = "INSERT INTO grid (minLat, minLon, maxLat, maxLon) \n"
					+" SELECT * FROM (\n"
					+" SELECT minLat, minLon, minLat+%1$f, minLon+%2$f FROM grid where id = %3$d\n"
					+" union all\n"
					+" SELECT minLat+%1$f, minLon, maxLat, minLon+%2$f FROM grid where id = %3$d\n"
					+" union all\n"
					+" SELECT minLat, minLon+%2$f, minLat+%1$f, maxLon FROM grid where id = %3$d\n"
					+" union all\n"
					+" SELECT minLat+%1$f, minLon+%2$f, maxLat, maxLon FROM grid where id = %3$d\n"
					+" )\n";
			
			create4NewCellsSql = String.format(create4NewCellsSql, newCellSizeLat, newCellSizeLon, id);
			Log.d(create4NewCellsSql);
			prepStatement = conn.prepareStatement(create4NewCellsSql);
			prepStatement.executeUpdate();
			prepStatement.close();
			
			//delete old cell
			statement = conn.createStatement();
			String deleteOldCellSql = "DELETE FROM "+Queries.GRID_TABLE+" WHERE id="+id.toString();
			Log.d(deleteOldCellSql);
			int deleteResult = statement.executeUpdate(deleteOldCellSql);
			Log.d("deleted "+deleteResult+" cells");
			statement.close();
			
			//update nodes to use new cells
			statement = conn.createStatement();
			String updateNodesSql = "UPDATE nodes SET grid_id = (SELECT g.id FROM "+Queries.GRID_TABLE+" g WHERE lat>=minLat AND lat<=maxLat AND lon>=minLon AND lon<=maxLon limit 1) WHERE grid_id="+id.toString();
			Log.d(updateNodesSql);
			statement.executeUpdate(updateNodesSql);
		}finally{
			if (cur != null)
				cur.close();
			if (statement != null)
				statement.close();
			if (prepStatement != null)
				prepStatement.close();
		}
	}

}
