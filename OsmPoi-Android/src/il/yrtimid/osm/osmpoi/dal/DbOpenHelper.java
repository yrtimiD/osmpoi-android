/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.domain.*;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author yrtimiD
 * 
 */
public class DbOpenHelper extends SQLiteOpenHelper {
	protected static final String NODES_TABLE = "nodes";
	protected static final String NODES_TAGS_TABLE = "node_tags";

	protected static final String WAYS_TABLE = "ways";
	protected static final String WAY_TAGS_TABLE = "way_tags";
	protected static final String WAY_NODS_TABLE = "way_nodes";

	protected static final String RELATIONS_TABLE = "relations";
	protected static final String RELATION_TAGS_TABLE = "relation_tags";
	protected static final String MEMBERS_TABLE = "members";

	protected static final String GRID_TABLE = "grid";
	
	protected static final String INLINE_QUERIES_TABLE = "inline_queries";
	protected static final String INLINE_RESULTS_TABLE = "inline_results";
	
	protected static final String STARRED_TABLE = "starred";
	
	
	private static final int DATABASE_VERSION = 2;

	Context context;

	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DbOpenHelper(Context context, File dbLocation) {
		super(context, dbLocation.getPath(), null, DATABASE_VERSION);
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		createAllTables(db);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2){
			db.execSQL(context.getString(R.string.sql_create_starred_table));
		}
	}

	private void createAllTables(SQLiteDatabase db){
		db.execSQL(context.getString(R.string.sql_create_node_table));
		db.execSQL(context.getString(R.string.sql_create_node_tags_table));
		db.execSQL(context.getString(R.string.sql_node_tags_idx));
		
		db.execSQL(context.getString(R.string.sql_create_ways_table));
		db.execSQL(context.getString(R.string.sql_create_way_nodes_table));
		db.execSQL(context.getString(R.string.sql_way_nodes_way_idx));
		db.execSQL(context.getString(R.string.sql_way_nodes_node_idx));
		db.execSQL(context.getString(R.string.sql_create_way_tags_table));
		db.execSQL(context.getString(R.string.sql_way_tags_idx));
		
		db.execSQL(context.getString(R.string.sql_create_relations_table));
		db.execSQL(context.getString(R.string.sql_create_relation_tags_table));
		db.execSQL(context.getString(R.string.sql_relation_tags_idx));
		db.execSQL(context.getString(R.string.sql_create_members_table));
		db.execSQL(context.getString(R.string.sql_relation_members_idx));
		
		db.execSQL(context.getString(R.string.sql_create_grid_table));
		
		db.execSQL(context.getString(R.string.sql_create_inline_queries_table));
		db.execSQL(context.getString(R.string.sql_create_inline_results_table));
		
		db.execSQL(context.getString(R.string.sql_create_starred_table));
	}

	private void dropAllTables(SQLiteDatabase db){
		db.execSQL("DROP TABLE IF EXISTS "+MEMBERS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+RELATION_TAGS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+RELATIONS_TABLE);

		db.execSQL("DROP TABLE IF EXISTS "+WAY_NODS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+WAY_TAGS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+WAYS_TABLE);

		db.execSQL("DROP TABLE IF EXISTS "+NODES_TAGS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+NODES_TABLE);
		
		db.execSQL("DROP TABLE IF EXISTS "+GRID_TABLE);
		
		db.execSQL("DROP TABLE IF EXISTS "+INLINE_RESULTS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+INLINE_QUERIES_TABLE);
		
		db.execSQL("VACUUM");
	}

	//public void dropDB(){
	//	context.deleteDatabase(DATABASE_NAME);
	//}
	
	public void clearAll() {
		SQLiteDatabase db = getWritableDatabase();
		dropAllTables(db);
		createAllTables(db);
	}
	
	public void addEntity(Entity entity) {
		if (entity instanceof Node)
			addNode((Node) entity);
		else if (entity instanceof Way)
			addWay((Way) entity);
		else if (entity instanceof Relation)
			addRelation((Relation) entity);
	}
	
	public void addNode(Node node) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", node.getId());
			values.put("timestamp", node.getTimestamp());
			values.put("lat", node.getLatitude());
			values.put("lon", node.getLongitude());

			long id = db.insert(NODES_TABLE, null, values);
			if (id == -1)
				throw new SQLiteException("Node was not inserted");

			Collection<Tag> tags = node.getTags();
			long nodeId = node.getId();
			for (Tag tag : tags) {
				addNodeTag(nodeId, tag);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addNodes(Collection<Node> nodes) {
		SQLiteDatabase db = getWritableDatabase();
		db.setLockingEnabled(false);
		db.beginTransaction();

		try {
			InsertHelper insert = new InsertHelper(db, NODES_TABLE);
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
					throw new SQLiteException("Node was not inserted");
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

	
	public void addNodeTag(long nodeId, Tag tag) {
		SQLiteDatabase db = getWritableDatabase();
		try {
			
			ContentValues values = new ContentValues();
			values.put("node_id", nodeId);
			values.put("k", tag.getKey());
			values.put("v", tag.getValue());

			long id = db.insert(NODES_TAGS_TABLE, null, values);
			if (id == -1)
				throw new SQLiteException("Node tag was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addNodesTags(Collection<Node> nodes) {
		SQLiteDatabase db = getWritableDatabase();		
		db.setLockingEnabled(false);
		db.beginTransaction();

		try {
			InsertHelper insert = new InsertHelper(db, NODES_TAGS_TABLE);
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
						throw new SQLiteException("Node tag was not inserted");
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

	
	public void addWay(Way way) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", way.getId());
			values.put("timestamp", way.getTimestamp());

			long id = db.insert(WAYS_TABLE, null, values);
			if (id == -1)
				throw new SQLiteException("Way was not inserted");

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

	public void addWayTag(long wayId, Tag tag) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("way_id", wayId);
			values.put("k", tag.getKey());
			values.put("v", tag.getValue());

			long id = db.insert(WAY_TAGS_TABLE, null, values);
			if (id == -1)
				throw new SQLiteException("Node tag was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addWayNode(long wayId, int index, Node wayNode) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("way_id", wayId);
			values.put("node_id", wayNode.getId());

			long id = db.insert(WAY_NODS_TABLE, null, values);
			if (id == -1)
				throw new SQLiteException("Node tag was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addRelation(Relation rel) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", rel.getId());
			values.put("timestamp", rel.getTimestamp());

			long id = db.insert(RELATIONS_TABLE, null, values);
			if (id == -1)
				throw new SQLiteException("Relation was not inserted");

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

	public void addRelationTag(long relId, Tag tag) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("relation_id", relId);
			values.put("k", tag.getKey());
			values.put("v", tag.getValue());

			long id = db.insert(RELATION_TAGS_TABLE, null, values);
			if (id == -1)
				throw new SQLiteException("Relation tag was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addRelationMember(long relId, int index, RelationMember mem) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("relation_id", relId);
			values.put("type", mem.getMemberType().name());
			values.put("ref", mem.getMemberId());
			values.put("role", mem.getMemberRole());

			long id = db.insert(MEMBERS_TABLE, null, values);
			if (id == -1)
				throw new SQLiteException("Relation member was not inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateNodesGrid(){
		SQLiteDatabase db = getWritableDatabase();
		Double gridStep = 0.1;
		db.delete(GRID_TABLE, null, null);
		String sql_generate_grid = "INSERT INTO grid (minLat, minLon, maxLat, maxLon)"
								+" SELECT minLat, minLon, minLat+? maxLat, minLon+? maxLon FROM ("
								+" SELECT DISTINCT CAST(lat/? as INT)*? minLat, CAST(lon/? as INT)*? minLon FROM nodes"
								+" )";	
		db.execSQL(sql_generate_grid, new Object[]{gridStep, gridStep, gridStep, gridStep, gridStep, gridStep});
		db.execSQL("UPDATE nodes SET grid_id = (SELECT g.id FROM grid g WHERE lat>=minLat AND lat<maxLat AND lon>=minLon AND lon<maxLon)");
	}
	
	public void optimizeGrid(Integer maxItems){
		Collection<Integer> cells = null;
		do{
			cells = getBigCells(maxItems);
			if (cells.size() == 0) break;
			for(Integer cellId : cells){
				splitGridCell(cellId);
			}
			
		}while(true);
	}
	
	private Collection<Integer> getBigCells(Integer minItems){
		SQLiteDatabase db = getReadableDatabase();
		Collection<Integer> gridIds = new ArrayList<Integer>();
		Cursor cur = db.rawQuery("SELECT grid_id FROM "+NODES_TABLE+" GROUP BY grid_id HAVING count(id)>"+minItems.toString(), null);
		if (cur.moveToFirst()){
			do{
				gridIds.add(cur.getInt(0));
			}while(cur.moveToNext());
		}
		
		return gridIds;
	}
	
	private void splitGridCell(Integer id){
		SQLiteDatabase db = getWritableDatabase();
		//db.beginTransaction();
		try{
			//calc new cell size to be 1/2 of the old one
			Cursor cur = db.rawQuery("SELECT round((maxLat-minLat)/2,7)  from "+GRID_TABLE+" WHERE id=?", new String[]{id.toString()});
			cur.moveToFirst();
			Double newCellSize = cur.getDouble(0);
			
			//create new grid cells from all nodes in old cell
			String sql_generate_grid = "INSERT INTO grid (minLat, minLon, maxLat, maxLon)"
					+" SELECT minLat, minLon, minLat+? maxLat, minLon+? maxLon FROM ("
					+" SELECT DISTINCT CAST(lat/? as INT)*? minLat, CAST(lon/? as INT)*? minLon FROM nodes WHERE grid_id=?"
					+" );";	
			db.execSQL(sql_generate_grid, new Object[] {newCellSize,newCellSize,newCellSize,newCellSize,newCellSize,newCellSize, id});
			
			//delete old cell
			db.delete(GRID_TABLE, "id=?", new String[]{id.toString()});
			
			//update nodes to use new cells
			db.execSQL("UPDATE nodes SET grid_id = (SELECT g.id FROM grid g WHERE lat>=minLat AND lat<maxLat AND lon>=minLon AND lon<maxLon) WHERE grid_id=?", new Object[]{id});
			
			//db.setTransactionSuccessful();
		}catch (Exception e) {
			Log.wtf("splitGridCell", e);
		}finally{
			//db.endTransaction();
		}
	}
	
	public boolean isNodeBelongsToWay(Node node){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cur = null;
		try{
			cur = db.rawQuery("SELECT 1 as _id FROM "+WAY_NODS_TABLE+" WHERE node_id=? LIMIT 1", new String[]{Long.toString(node.getId())});
			if (cur.moveToFirst())
				return true;
			else 
				return false;
		}catch(Exception e){
			Log.wtf("isNodeBelongsToWay id="+node.getId(), e);
			return false;
		}finally{
			if (cur!=null) cur.close();
		}
	}

}

	