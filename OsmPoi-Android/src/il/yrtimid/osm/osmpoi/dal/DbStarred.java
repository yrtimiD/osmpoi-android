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
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.categories.Category;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.searchparameters.SearchById;

/**
 * @author yrtimid
 *
 */
public class DbStarred extends DbOpenHelper {
	
	public DbStarred(Context context, File dbLocation) {
		super(context, dbLocation);
	}

	public void addStarred(Entity entity, String title){
		SQLiteDatabase db = null;
		try{
			db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("type", entity.getType().name());
			values.put("id", entity.getId());
			values.put("title", title);
			db.insertWithOnConflict(STARRED_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
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
			db.delete(STARRED_TABLE, "type=? AND id=?", new String[] {entity.getType().name(), Long.toString(entity.getId())});
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
			cur = db.rawQuery("SELECT 1 FROM "+STARRED_TABLE+" WHERE type=? AND id=?", new String[] {entity.getType().name(), Long.toString(entity.getId())});
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
			cur = db.rawQuery("SELECT * FROM "+STARRED_TABLE, null);
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
