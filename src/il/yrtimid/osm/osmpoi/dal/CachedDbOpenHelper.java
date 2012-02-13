/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.Util;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author yrtimid
 *
 */
public class CachedDbOpenHelper extends DbOpenHelper {

	private Collection<Entity> addEntitiesQueue = new ArrayList<Entity>();
	private Collection<Node> addNodeQueue = new ArrayList<Node>();
	private Collection<Node> addIfBelongToWayQueue = new ArrayList<Node>();
	/**
	 * @param context
	 */
	public CachedDbOpenHelper(Context context, File dbLocation) {
		super(context, dbLocation);
	}

	private boolean flushing = false;
	public void flush() {
		flushing = true;
		addNode(null);
		addNodeIfBelongsToWay(null);
		addEntity(null);
		flushing = false;
	}

	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.DAL.DBHelper#addEntity(org.openstreetmap.osmosis.core.domain.v0_6.Entity)
	 */
	@Override
	public void addEntity(Entity entity) {
		if (entity != null){
			addEntitiesQueue.add(entity);
			//Log.v(TAG, "Adding entity to queue. Count="+entitiesQueue.size());
		}
		
		if (flushing || addEntitiesQueue.size() >= 1000) {
			try {
				Log.v("Flushing queue to DB. Count="+addEntitiesQueue.size());
				SQLiteDatabase db = getWritableDatabase();
				db.beginTransaction();
				
				try {
					for (Entity e : addEntitiesQueue) {
						super.addEntity(e);
					}
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			addEntitiesQueue.clear();
		}
	}
	
	public void addNode(Node node) {
		if (node != null){
			addNodeQueue.add(node);
		}
		
		if (flushing || addNodeQueue.size() >= 1000) {
			try {
				Log.v("Flushing nodes queue to DB. Count="+addNodeQueue.size());
				super.addNodes(addNodeQueue);
			} catch (Exception e) {
				e.printStackTrace();
			}
			addNodeQueue.clear();
		}
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

			SQLiteDatabase db = getReadableDatabase();
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
			
			db = getWritableDatabase();
			db.beginTransaction();
			
			try {
				for(Node n:addIfBelongToWayQueue){
					if (neededIds.contains(n.getId())){
						super.addNode(n);
					}
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			
			addIfBelongToWayQueue.clear();
		}
	}
}
