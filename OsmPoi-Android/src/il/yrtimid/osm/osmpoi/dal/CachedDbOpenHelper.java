/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.Util;
import il.yrtimid.osm.osmpoi.domain.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author yrtimid
 *
 */
public class CachedDbOpenHelper extends DbOpenHelper {

	private Boolean flushing = false;
	private Collection<Node> addIfBelongToWayQueue = new ArrayList<Node>();
	/**
	 * @param context
	 */
	public CachedDbOpenHelper(Context context, File dbLocation) {
		super(context, dbLocation);
	}

	public void beginAdd(){
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
	}
	
	public void endAdd(){
		flushing = true;
		addNodeIfBelongsToWay(null);
		flushing = false;
		SQLiteDatabase db = getWritableDatabase();
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	

	public void addNodeIfBelongsToWay(Node node){
		if (node != null){
			addIfBelongToWayQueue.add(node);
		}
		
		if (flushing || addIfBelongToWayQueue.size()>=1000){
			ArrayList<Long> ids = new ArrayList<Long>(addIfBelongToWayQueue.size());
			for(Node n:addIfBelongToWayQueue){
				ids.add(n.getId());
			}
			
			HashSet<Long> neededIds = new HashSet<Long>();
			String inClause = Util.join(",", ids.toArray());

			SQLiteDatabase db = getWritableDatabase();
			Cursor cur = null;
			try{
				Log.d("Checking nodes in ways: "+inClause);
				cur = db.rawQuery("SELECT node_id as _id FROM "+WAY_NODS_TABLE+" WHERE node_id in ("+inClause+")", null);
				if (cur.moveToFirst()){
					do{
						neededIds.add(cur.getLong(0));
					}while(cur.moveToNext());
				}
			}catch(Exception e){
				Log.wtf("addNodeIfBelongsToWay ids in"+inClause, e);
			}finally{
				if (cur!=null) cur.close();
			}
			
			for(Node n:addIfBelongToWayQueue){
				if (neededIds.contains(n.getId())){
					super.addNode(n);
				}
			}
			
			addIfBelongToWayQueue.clear();
		}
	}
}
