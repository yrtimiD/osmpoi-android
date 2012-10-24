/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.Pair;
import il.yrtimid.osm.osmpoi.domain.Bound;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Relation;
import il.yrtimid.osm.osmpoi.domain.RelationMember;
import il.yrtimid.osm.osmpoi.domain.Tag;
import il.yrtimid.osm.osmpoi.domain.Way;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author yrtimid
 *
 */
public class DbFiller extends DbCreator implements IDbFiller {
	
	/**
	 * @param context
	 * @param dbLocation
	 */
	public DbFiller(Context context, File dbLocation) {
		super(context, dbLocation);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDatabase#create()
	 */
	@Override
	public void create() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#clearAll()
	 */
	@Override
	public void clearAll() throws Exception {
		drop();
		SQLiteDatabase db = getWritableDatabase();
		db.setLockingEnabled(false);
		//dropAllTables(db);
		createAllTables(db);
		db.setLockingEnabled(true);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#initGrid()
	 */
	@Override
	public void initGrid() throws SQLException {
		SQLiteDatabase db = getWritableDatabase();
		//db.execSQL("UPDATE "+NODES_TABLE+" SET grid_id=1");
		db.execSQL("DROP TABLE IF EXISTS "+Queries.GRID_TABLE);
		db.execSQL(Queries.SQL_CREATE_GRID_TABLE);
	
		String sql_generate_grid = "INSERT INTO grid (minLat, minLon, maxLat, maxLon)"
								+" SELECT min(lat) minLat, min(lon) minLon, max(lat) maxLat, max(lon) maxLon"
								+" FROM nodes";	
		Log.d(sql_generate_grid);
		db.execSQL(sql_generate_grid);
		
		String updateNodesGrid = "UPDATE nodes SET grid_id = 1 WHERE grid_id <> 1";
		Log.d(updateNodesGrid);
		db.execSQL(updateNodesGrid);
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addEntity(il.yrtimid.osm.osmpoi.domain.Entity)
	 */
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
	
	@Override
	public void addBound(Bound bound) throws SQLException {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("top", bound.getTop());
			values.put("bottom", bound.getBottom());
			values.put("left", bound.getLeft());
			values.put("right", bound.getRight());

			long id = db.insert(Queries.BOUNDS_TABLE, null, values);
			if (id == -1)
				throw new SQLException("Bound was not inserted");
		} finally{
		}
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addNode(il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addNode(Node node) throws SQLException {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", node.getId());
			values.put("timestamp", node.getTimestamp());
			values.put("lat", node.getLatitude());
			values.put("lon", node.getLongitude());
			values.put("grid_id", 1);

			db.insertWithOnConflict(Queries.NODES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

			addNodeTags(node);
		} finally{
		}
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addNodeTag(long, il.yrtimid.osm.osmpoi.domain.Tag)
	 */
	@Override
	public void addNodeTags(Node node) throws SQLException {
		try {
			SQLiteDatabase db = getWritableDatabase();
			for(Tag tag : node.getTags()){
				ContentValues values = new ContentValues();
				values.put("node_id", node.getId());
				values.put("k", tag.getKey());
				values.put("v", tag.getValue());
	
				db.insertWithOnConflict(Queries.NODES_TAGS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			}
		} finally{
		}
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addWay(il.yrtimid.osm.osmpoi.domain.Way)
	 */
	@Override
	public void addWay(Way way) throws SQLException {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", way.getId());
			values.put("timestamp", way.getTimestamp());

			db.insertWithOnConflict(Queries.WAYS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

			addWayTags(way);
			addWayNodes(way);
		} finally{
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addWayTag(long, il.yrtimid.osm.osmpoi.domain.Tag)
	 */
	@Override
	public void addWayTags(Way way) throws SQLException {
		try {
			SQLiteDatabase db = getWritableDatabase();
			
			for(Tag tag : way.getTags()){
				ContentValues values = new ContentValues();
				values.put("way_id", way.getId());
				values.put("k", tag.getKey());
				values.put("v", tag.getValue());

				db.insertWithOnConflict(Queries.WAY_TAGS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			}
		} finally{
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addWayNode(long, int, il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addWayNodes(Way way) throws SQLException {
		try {
			SQLiteDatabase db = getWritableDatabase();
			for(Node node : way.getWayNodes()){
				ContentValues values = new ContentValues();
				values.put("way_id", way.getId());
				values.put("node_id", node.getId());
	
				db.insertWithOnConflict(Queries.WAY_NODES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			}
		} finally{
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addRelation(il.yrtimid.osm.osmpoi.domain.Relation)
	 */
	@Override
	public void addRelation(Relation rel) throws SQLException {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", rel.getId());
			values.put("timestamp", rel.getTimestamp());

			db.insertWithOnConflict(Queries.RELATIONS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

			addRelationTags(rel);
			addRelationMembers(rel);
		} finally{
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addRelationTag(long, il.yrtimid.osm.osmpoi.domain.Tag)
	 */
	@Override
	public void addRelationTags(Relation rel) throws SQLException {
		try {
			SQLiteDatabase db = getWritableDatabase();
			for(Tag tag : rel.getTags()){
				ContentValues values = new ContentValues();
				values.put("relation_id", rel.getId());
				values.put("k", tag.getKey());
				values.put("v", tag.getValue());
	
				db.insertWithOnConflict(Queries.RELATION_TAGS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			}
		}finally{
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addRelationMember(long, int, il.yrtimid.osm.osmpoi.domain.RelationMember)
	 */
	@Override
	public void addRelationMembers(Relation rel) throws SQLException {
		try {
			SQLiteDatabase db = getWritableDatabase();
			for(RelationMember mem : rel.getMembers()){
				ContentValues values = new ContentValues();
				values.put("relation_id", rel.getId());
				values.put("type", mem.getMemberType().name());
				values.put("ref", mem.getMemberId());
				values.put("role", mem.getMemberRole());
	
				db.insertWithOnConflict(Queries.MEMBERS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			}
		}finally{
		}
	}
	
	/*
	public void updateNodesGrid(){
		SQLiteDatabase db = getWritableDatabase();
		Double gridStep = 0.1;
		db.delete(GRID_TABLE, null, null);
		String sql_generate_grid = "INSERT INTO grid (minLat, minLon, maxLat, maxLon)"
								+" SELECT minLat, minLon, minLat+? maxLat, minLon+? maxLon FROM ("
								+" SELECT DISTINCT CAST(lat/? as INT)*? minLat, CAST(lon/? as INT)*? minLon FROM nodes"
								+" )";	
		Log.d(sql_generate_grid);
		Log.d("gridStep="+gridStep);
		db.execSQL(sql_generate_grid, new Object[]{gridStep, gridStep, gridStep, gridStep, gridStep, gridStep});
		
		String updateNodesGrid = "UPDATE nodes SET grid_id = (SELECT g.id FROM grid g WHERE lat>=minLat AND lat<maxLat AND lon>=minLon AND lon<maxLon)";
		Log.d(updateNodesGrid);
		db.execSQL(updateNodesGrid);
	}
	*/
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#optimizeGrid(java.lang.Integer)
	 */
	@Override
	public void optimizeGrid(Integer maxItems) throws SQLException {
		Collection<Pair<Integer,Integer>> cells = null;
		do{
			cells = getBigCells(maxItems);
			Log.d("OptimizeGrid: "+cells.size()+" cells needs optimization for "+maxItems+" items");
			for(Pair<Integer,Integer> cell : cells){
				Log.d("OptimizeGrid: cell_id="+cell.getA()+", cell size="+cell.getB());
				splitGridCell(cell.getA());
			}
			
		}while(cells.size() > 0);
	}
	
	/**
	 * finds cells which have nodes count greater than minItems
	 * @param minItems
	 * @return
	 */
	private Collection<Pair<Integer,Integer>> getBigCells(Integer minItems) throws SQLException{
		SQLiteDatabase db = getReadableDatabase();
		Cursor cur = null;
		Collection<Pair<Integer,Integer>> gridIds = new ArrayList<Pair<Integer,Integer>>();
		try{
			
			cur = db.rawQuery("SELECT grid_id, count(id) [count] FROM "+Queries.NODES_TABLE+" GROUP BY grid_id HAVING count(id)>"+minItems.toString(), null);
			if (cur.moveToFirst()){
				do{
					Integer id = cur.getInt(cur.getColumnIndex("grid_id"));
					Integer count = cur.getInt(cur.getColumnIndex("count"));
					gridIds.add(new Pair<Integer, Integer>(id, count));
				}while(cur.moveToNext());
			}
		}finally{
			if (cur != null) cur.close(); 
		}
		return gridIds;
	}
	
	/**
	 * Splits cell into 4 pieces and updates theirs nodes with the new split
	 * @param id ID of the cell to split
	 */
	private void splitGridCell(Integer id){
		SQLiteDatabase db = getWritableDatabase();
		try{
			Log.d("splitGridCell id:"+id);
			//calc new cell size to be 1/2 of the old one
			Cursor cur = db.rawQuery("SELECT round((maxLat-minLat)/2,7) from "+Queries.GRID_TABLE+" WHERE id=?", new String[]{id.toString()});
			cur.moveToFirst();
			Double newCellSize = cur.getDouble(0);
			cur.close();
			
			//create new grid cells from all nodes in old cell
			String sql_generate_grid = "INSERT INTO grid (minLat, minLon, maxLat, maxLon)"
					+" SELECT minLat, minLon, minLat+? maxLat, minLon+? maxLon FROM ("
					+" SELECT DISTINCT CAST(lat/? as INT)*? minLat, CAST(lon/? as INT)*? minLon FROM nodes WHERE grid_id=?"
					+" );";	
			Log.d(sql_generate_grid);
			Log.d("newCellSize="+newCellSize);
			db.execSQL(sql_generate_grid, new Object[] {newCellSize,newCellSize,newCellSize,newCellSize,newCellSize,newCellSize, id});
			
			//delete old cell
			db.delete(Queries.GRID_TABLE, "id=?", new String[]{id.toString()});
			
			//update nodes to use new cells
			String update_nodes = "UPDATE nodes SET grid_id = (SELECT g.id FROM "+Queries.GRID_TABLE+" g WHERE lat>=minLat AND lat<maxLat AND lon>=minLon AND lon<maxLon) WHERE grid_id=?";
			Log.d(update_nodes);
			db.execSQL(update_nodes, new Object[]{id});
			
		}catch (Exception e) {
			Log.wtf("splitGridCell", e);
		}finally{
			//db.endTransaction();q
		}
	}
}
