/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import il.yrtimid.osm.osmpoi.Log;
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
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
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
	public void clearAll() {
		dropDB();
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
	public void initGrid(){
		SQLiteDatabase db = getWritableDatabase();
		//db.execSQL("UPDATE "+NODES_TABLE+" SET grid_id=1");
		db.execSQL("DROP TABLE IF EXISTS "+Queries.GRID_TABLE);
		db.execSQL(Queries.SQL_CREATE_GRID_TABLE);
	
		String sql_generate_grid = "INSERT INTO grid (minLat, minLon, maxLat, maxLon)"
								+" SELECT min(lat) minLat, min(lon) minLon, max(lat) maxLat, max(lon) maxLon"
								+" FROM nodes";	
		Log.d(sql_generate_grid);
		db.execSQL(sql_generate_grid);
		
		String updateNodesGrid = "UPDATE nodes SET grid_id = 1";
		Log.d(updateNodesGrid);
		db.execSQL(updateNodesGrid);
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
	
	@Override
	public void addBound(Bound bound) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addNode(il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addNode(Node node) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", node.getId());
			values.put("timestamp", node.getTimestamp());
			values.put("lat", node.getLatitude());
			values.put("lon", node.getLongitude());

			long id = db.insert(Queries.NODES_TABLE, null, values);
			if (id == -1)
				throw new SQLException("Node was not inserted");

			Collection<Tag> tags = node.getTags();
			long nodeId = node.getId();
			for (Tag tag : tags) {
				addNodeTag(nodeId, tag);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addNodes(java.util.Collection)
	 */
	@Override
	public void addNodes(Collection<Node> nodes) {
		SQLiteDatabase db = getWritableDatabase();
		db.setLockingEnabled(false);
		db.beginTransaction();

		try {
			InsertHelper insert = new InsertHelper(db, Queries.NODES_TABLE);
			final int idCol = insert.getColumnIndex("id");
			final int timestampCol = insert.getColumnIndex("timestamp");
			final int latCol = insert.getColumnIndex("lat");
			final int lonCol = insert.getColumnIndex("lon");
			
			for(Node node : nodes){
				insert.prepareForInsert();
				insert.bind(idCol, node.getId());
				insert.bind(timestampCol, node.getTimestamp());
				insert.bind(latCol, node.getLatitude());
				insert.bind(lonCol, node.getLongitude());
				long id = insert.execute();
				if (id == -1)
					throw new SQLException("Node was not inserted");
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			db.endTransaction();
			db.setLockingEnabled(true);
		}
		
		addNodesTags(nodes);
	}

	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addNodeTag(long, il.yrtimid.osm.osmpoi.domain.Tag)
	 */
	@Override
	public void addNodeTag(long nodeId, Tag tag) {
		SQLiteDatabase db = getWritableDatabase();
		try {
			
			ContentValues values = new ContentValues();
			values.put("node_id", nodeId);
			values.put("k", tag.getKey());
			values.put("v", tag.getValue());

			long id = db.insert(Queries.NODES_TAGS_TABLE, null, values);
			if (id == -1)
				throw new SQLException("Node tag was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addNodesTags(java.util.Collection)
	 */
	@Override
	public void addNodesTags(Collection<Node> nodes) {
		SQLiteDatabase db = getWritableDatabase();		
		db.setLockingEnabled(false);
		db.beginTransaction();

		try {
			InsertHelper insert = new InsertHelper(db, Queries.NODES_TAGS_TABLE);
			final int nodeIdCol = insert.getColumnIndex("node_id");
			final int kCol = insert.getColumnIndex("k");
			final int vCol = insert.getColumnIndex("v");
			for(Node node:nodes){
				final long nodeId = node.getId();
				for(Tag tag:node.getTags()){
					insert.prepareForInsert();
					insert.bind(nodeIdCol, nodeId);
					insert.bind(kCol, tag.getKey());
					insert.bind(vCol, tag.getValue());
					long id = insert.execute();
					if (id == -1)
						throw new SQLException("Node tag was not inserted");
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			db.endTransaction();
			db.setLockingEnabled(true);
		}
	}

	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addWay(il.yrtimid.osm.osmpoi.domain.Way)
	 */
	@Override
	public void addWay(Way way) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", way.getId());
			values.put("timestamp", way.getTimestamp());

			long id = db.insert(Queries.WAYS_TABLE, null, values);
			if (id == -1)
				throw new SQLException("Way was not inserted");

			Collection<Tag> tags = way.getTags();
			long wayId = way.getId();
			for (Tag tag : tags) {
				addWayTag(wayId, tag);
			}

			List<Node> nodes = way.getWayNodes();
			for (int i = 0; i < nodes.size(); i++) {
				addWayNode(wayId, i, nodes.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addWayTag(long, il.yrtimid.osm.osmpoi.domain.Tag)
	 */
	@Override
	public void addWayTag(long wayId, Tag tag) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("way_id", wayId);
			values.put("k", tag.getKey());
			values.put("v", tag.getValue());

			long id = db.insert(Queries.WAY_TAGS_TABLE, null, values);
			if (id == -1)
				throw new SQLException("Node tag was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addWayNode(long, int, il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addWayNode(long wayId, int index, Node wayNode) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("way_id", wayId);
			values.put("node_id", wayNode.getId());

			long id = db.insert(Queries.WAY_NODS_TABLE, null, values);
			if (id == -1)
				throw new SQLException("Node tag was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addRelation(il.yrtimid.osm.osmpoi.domain.Relation)
	 */
	@Override
	public void addRelation(Relation rel) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", rel.getId());
			values.put("timestamp", rel.getTimestamp());

			long id = db.insert(Queries.RELATIONS_TABLE, null, values);
			if (id == -1)
				throw new SQLException("Relation was not inserted");

			Collection<Tag> tags = rel.getTags();
			long relId = rel.getId();
			for (Tag tag : tags) {
				addRelationTag(relId, tag);
			}

			List<RelationMember> members = rel.getMembers();
			for (int i = 0; i < members.size(); i++) {
				addRelationMember(relId, i, members.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addRelationTag(long, il.yrtimid.osm.osmpoi.domain.Tag)
	 */
	@Override
	public void addRelationTag(long relId, Tag tag) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("relation_id", relId);
			values.put("k", tag.getKey());
			values.put("v", tag.getValue());

			long id = db.insert(Queries.RELATION_TAGS_TABLE, null, values);
			if (id == -1)
				throw new SQLException("Relation tag was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbFiller#addRelationMember(long, int, il.yrtimid.osm.osmpoi.domain.RelationMember)
	 */
	@Override
	public void addRelationMember(long relId, int index, RelationMember mem) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("relation_id", relId);
			values.put("type", mem.getMemberType().name());
			values.put("ref", mem.getMemberId());
			values.put("role", mem.getMemberRole());

			long id = db.insert(Queries.MEMBERS_TABLE, null, values);
			if (id == -1)
				throw new SQLException("Relation member was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
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
	public void optimizeGrid(Integer maxItems){
		Collection<Integer> cells = null;
		do{
			cells = getBigCells(maxItems);
			Log.d("OptimizeGrid: "+cells.size()+" cells needs optimization for "+maxItems+" items");
			if (cells.size() == 0) break;
			for(Integer cellId : cells){
				splitGridCell(cellId);
			}
			
		}while(true);
	}
	
	/**
	 * finds cells which have nodes count greater than minItems
	 * @param minItems
	 * @return
	 */
	private Collection<Integer> getBigCells(Integer minItems){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cur = null;
		Collection<Integer> gridIds = new ArrayList<Integer>();
		try{
			
			cur = db.rawQuery("SELECT grid_id FROM "+Queries.NODES_TABLE+" GROUP BY grid_id HAVING count(id)>"+minItems.toString(), null);
			if (cur.moveToFirst()){
				do{
					gridIds.add(cur.getInt(0));
				}while(cur.moveToNext());
			}
		}catch (Exception e) {
			Log.wtf("getBigCells", e);
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
		//db.beginTransaction();
		try{
			Log.d("splitGridCell id:"+id);
			//calc new cell size to be 1/2 of the old one
			Cursor cur = db.rawQuery("SELECT round((maxLat-minLat)/2,7) from "+Queries.GRID_TABLE+" WHERE id=?", new String[]{id.toString()});
			cur.moveToFirst();
			Double newCellSize = cur.getDouble(0);
			
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
			
			//db.setTransactionSuccessful();
		}catch (Exception e) {
			Log.wtf("splitGridCell", e);
		}finally{
			//db.endTransaction();q
		}
	}
}
