/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.domain.*;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import java.sql.SQLException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author yrtimiD
 * 
 */
public class DbCreator extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 3;
	protected static final Map<EntityType, String> entityTypeToTableName = new HashMap<EntityType, String>();
	protected static final Map<EntityType, String> entityTypeToTagsTableName = new HashMap<EntityType, String>();
	
	protected Context context;

	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DbCreator(Context context, File dbLocation) {
		super(context, dbLocation.getPath(), null, DATABASE_VERSION);
		this.context = context;
		
		entityTypeToTableName.put(EntityType.Node, Queries.NODES_TABLE);
		entityTypeToTableName.put(EntityType.Way, Queries.WAYS_TABLE);
		entityTypeToTableName.put(EntityType.Relation, Queries.RELATIONS_TABLE);
		
		entityTypeToTagsTableName.put(EntityType.Node, Queries.NODES_TAGS_TABLE);
		entityTypeToTagsTableName.put(EntityType.Way, Queries.WAY_TAGS_TABLE);
		entityTypeToTagsTableName.put(EntityType.Relation, Queries.RELATION_TAGS_TABLE);
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
		if (oldVersion == 1 && newVersion > 1){
			db.execSQL(Queries.SQL_CREATE_STARRED_TABLE);
		}
		
		if (oldVersion == 2 && newVersion > 2){
			db.execSQL(Queries.SQL_CREATE_BOUNDS_TABLE);
		}
	}
	
	protected void createAllTables(SQLiteDatabase db){
		db.execSQL(Queries.SQL_CREATE_BOUNDS_TABLE);
		
		db.execSQL(Queries.SQL_CREATE_NODE_TABLE);
		db.execSQL(Queries.SQL_CREATE_NODE_TAGS_TABLE);
		db.execSQL(Queries.SQL_NODE_TAGS_IDX);
		
		db.execSQL(Queries.SQL_CREATE_WAYS_TABLE);
		db.execSQL(Queries.SQL_CREATE_WAY_NODES_TABLE);
		db.execSQL(Queries.SQL_WAY_NODES_WAY_IDX);
		db.execSQL(Queries.SQL_WAY_NODES_NODE_IDX);
		db.execSQL(Queries.SQL_CREATE_WAY_TAGS_TABLE);
		db.execSQL(Queries.SQL_WAY_TAGS_IDX);
		
		db.execSQL(Queries.SQL_CREATE_RELATIONS_TABLE);
		db.execSQL(Queries.SQL_CREATE_RELATION_TAGS_TABLE);
		db.execSQL(Queries.SQL_RELATION_TAGS_IDX);
		db.execSQL(Queries.SQL_CREATE_MEMBERS_TABLE);
		db.execSQL(Queries.SQL_RELATION_MEMBERS_IDX);
		
		db.execSQL(Queries.SQL_CREATE_GRID_TABLE);
		
		db.execSQL(Queries.SQL_CREATE_INLINE_QUERIES_TABLE);
		db.execSQL(Queries.SQL_CREATE_INLINE_RESULTS_TABLE);
	}

	protected void dropAllTables(SQLiteDatabase db){
		db.beginTransaction();
		try{
			db.execSQL("DROP TABLE IF EXISTS "+Queries.MEMBERS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+Queries.RELATION_TAGS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+Queries.RELATIONS_TABLE);
	
			db.execSQL("DROP TABLE IF EXISTS "+Queries.WAY_NODS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+Queries.WAY_TAGS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+Queries.WAYS_TABLE);
	
			db.execSQL("DROP TABLE IF EXISTS "+Queries.NODES_TAGS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+Queries.NODES_TABLE);
			
			db.execSQL("DROP TABLE IF EXISTS "+Queries.GRID_TABLE);
			
			db.execSQL("DROP TABLE IF EXISTS "+Queries.INLINE_RESULTS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+Queries.INLINE_QUERIES_TABLE);
			
			db.execSQL("DROP TABLE IF EXISTS "+Queries.BOUNDS_TABLE);
			
			db.setTransactionSuccessful();
		}catch(Exception e){
			Log.wtf("dropAllTables", e);
		}finally{
			db.endTransaction();
		}

		db.execSQL("VACUUM");
	}

	//public void dropDB(){
	//	context.deleteDatabase(DATABASE_NAME);
	//}
	

}

	