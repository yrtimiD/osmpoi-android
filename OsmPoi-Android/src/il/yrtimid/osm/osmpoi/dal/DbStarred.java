/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import il.yrtimid.osm.osmpoi.categories.Category;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.logging.Log;
import il.yrtimid.osm.osmpoi.searchparameters.SearchById;

/**
 * @author yrtimid
 *
 */
public class DbStarred extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 1;
	
	protected Context context;

	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DbStarred(Context context, File dbLocation) {
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
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {	}
	
	protected void createAllTables(SQLiteDatabase db){
		db.execSQL(Queries.SQL_CREATE_STARRED_TABLE);
	}

	protected void dropAllTables(SQLiteDatabase db){
		db.beginTransaction();
		try{
			db.execSQL("DROP TABLE IF EXISTS "+Queries.STARRED_TABLE);
			
			db.setTransactionSuccessful();
		}catch(Exception e){
			Log.wtf("dropAllTables", e);
		}finally{
			db.endTransaction();
		}

		db.execSQL("VACUUM");
	}
	
	public void addStarred(Entity entity, String title){
		SQLiteDatabase db = null;
		try{
			db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("type", entity.getType().name());
			values.put("id", entity.getId());
			values.put("title", title);
			db.insertWithOnConflict(Queries.STARRED_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		}catch (Exception e) {
			Log.wtf("addStarred",e);
		}finally{
			if (db != null) db.close();
		}
	}
	
	public void removeStarred(Entity entity){
		SQLiteDatabase db = null;
		try{
			db = getWritableDatabase();
			db.delete(Queries.STARRED_TABLE, "type=? AND id=?", new String[] {entity.getType().name(), Long.toString(entity.getId())});
		}catch(Exception e){
			Log.wtf("removeStarred",e);
		}finally{
			if (db != null) db.close();
		}
	}
	
	public Boolean isStarred(Entity entity){
		SQLiteDatabase db = null;
		Cursor cur = null;
		try{
			db = getReadableDatabase();
			cur = db.rawQuery("SELECT 1 FROM "+Queries.STARRED_TABLE+" WHERE type=? AND id=?", new String[] {entity.getType().name(), Long.toString(entity.getId())});
			boolean hasResults = (cur.moveToFirst());
			return hasResults;
		}catch(Exception e){
			Log.wtf("isStarred",e);
			return false;
		}finally{
			if (cur != null) cur.close();
			if (db != null) db.close();
		}
	}
	
	public Collection<Category> getAllStarred(){
		SQLiteDatabase db = null;
		Cursor cur = null;
		Collection<Category> results = new ArrayList<Category>();
		try{
			db = getReadableDatabase();
			cur = db.rawQuery("SELECT * FROM "+Queries.STARRED_TABLE, null);
			if (cur.moveToFirst()){
				do{
					String title = cur.getString(cur.getColumnIndex("title"));
					String type = cur.getString(cur.getColumnIndex("type"));
					Long id = cur.getLong(cur.getColumnIndex("id"));
					
					Category cat = new Category(Category.Type.SEARCH);
					cat.setLocalizable(false);
					cat.setName(title);
					EntityType entityType = Enum.valueOf(EntityType.class, type);
					cat.setSearchParameter(new SearchById(entityType, id));
					
					results.add(cat);
				}while(cur.moveToNext());
			}
		}catch(Exception e){
			Log.wtf("getAllStarred",e);
		}finally{
			if (cur != null) cur.close();
			if (db != null) db.close();
		}
		
		return results;
	}
}
