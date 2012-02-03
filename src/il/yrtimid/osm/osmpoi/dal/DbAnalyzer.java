/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import java.util.ArrayList;
import java.util.Collection;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.Point;
import il.yrtimid.osm.osmpoi.Util;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author yrtimid
 * 
 */
public class DbAnalyzer extends DbOpenHelper {

	/**
	 * @param context
	 */
	public DbAnalyzer(Context context) {
		super(context);
	}

	public long getNodesCount() {
		return getRowsCount(NODES_TABLE);
	}

	public long getWaysCount() {
		return getRowsCount(WAYS_TABLE);
	}

	public long getRelationsCount() {
		return getRowsCount(RELATIONS_TABLE);
	}

	
	private long getRowsCount(String tableName) {
		SQLiteDatabase db = null;
		Cursor cur = null;
		try {
			db = getReadableDatabase();
			
			cur = db.rawQuery("select count(*) from " + tableName, null);
			if (cur.moveToFirst()) {
				return cur.getLong(0);
			}
			return 0;
		}catch(Exception e){
			Log.wtf("getRowsCount for"+tableName, e);
			return -1;
		} finally {
			if (cur != null) cur.close();
			//if (db != null) db.close();
		}
	}
	
	public Long getInlineResultsId(String query, String select){
		SQLiteDatabase db;
		Cursor cur = null;
		try {
			db = getReadableDatabase();
			cur = db.rawQuery("select id from "+INLINE_QUERIES_TABLE+" where query=? and [select]=?", new String[]{query, select});
			if (cur.moveToFirst()){
				long id = cur.getLong(0);
				if (id>0) return id;
			}
			return 0L;
		}catch(Exception e){
			Log.wtf("isInlineResultsExists", e);
			return 0L;
		}finally{
			if (cur != null) cur.close();
		}
	}
	
	public Long createInlineResults(String query, String select){
		//query="shop=*"
		TagMatcher matcher = TagMatcher.parse(query);
		SQLiteDatabase db;
		try {
			db = getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put("query", query);
			cv.put("[select]", select);
			Long id = db.insert(INLINE_QUERIES_TABLE, null, cv);
			
			TagMatcherFormatter.WhereClause where = TagMatcherFormatter.format(matcher);
//TODO: add ways and relations
			String baseQuery = "INSERT INTO inline_results (query_id, value) SELECT DISTINCT ?, node_tags.v FROM node_tags";
			StringBuilder sb = new StringBuilder(baseQuery);
			for (int i = 1; i <= where.count; i++) {
				sb.append(String.format(" INNER JOIN node_tags t%s ON node_tags.node_id=t%s.node_id", i, i));
			}

			String sql = sb.toString()
					+ " WHERE "+ where.where +" AND node_tags.k like '"+select.replace('*', '%')+"'"
					+ " ORDER BY node_tags.v"
					+ " LIMIT 1000";
			Log.d(sql);
			db.execSQL(sql, new Object[]{id});
			
			return id;
		} catch (Exception e) {
			Log.wtf("createInlineResults", e);
			return 0L;
		}
	}
	
	public Collection<String> getInlineResults(Long id){
		SQLiteDatabase db;
		Cursor cur = null;
		Collection<String> result = new ArrayList<String>();
		try {
			db = getReadableDatabase();
			cur = db.rawQuery("select value from "+INLINE_RESULTS_TABLE+" where query_id=?", new String[]{id.toString()});
			if (cur.moveToFirst()){
				do{
					result.add(cur.getString(0));
				}while(cur.moveToNext());
			}
		}catch(Exception e){
			Log.wtf("getInlineResults", e);
		}finally{
			if (cur != null) cur.close();
		}
		
		return result;
	}
	
}
	

