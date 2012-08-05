/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.Util;
import il.yrtimid.osm.osmpoi.domain.*;

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
public class CachedDbOpenHelper extends DbFiller implements IDbCachedFiller {
	private static int MAX_QUEUE_SIZE = 1000;
	
	private Boolean flushing = false;
	
	private Collection<Node> addNodeIfBelongToWayQueue = new ArrayList<Node>();
	private Collection<Node> addNodeIfBelongToRelationQueue = new ArrayList<Node>();
	private Collection<Way> addWayIfBelongToRelationQueue = new ArrayList<Way>();
	
	/**
	 * @param context
	 */
	public CachedDbOpenHelper(Context context, File dbLocation) {
		super(context, dbLocation);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#beginAdd()
	 */
	@Override
	public void beginAdd(){
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#endAdd()
	 */
	@Override
	public void endAdd(){
		flushing = true;
		addNodeIfBelongsToWay(null);
		addNodeIfBelongsToRelation(null);
		addWayIfBelongsToRelation(null);
		flushing = false;
		SQLiteDatabase db = getWritableDatabase();
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addNodeIfBelongsToWay(il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addNodeIfBelongsToWay(Node node){
		if (node != null){
			addNodeIfBelongToWayQueue.add(node);
		}
		
		if (flushing || addNodeIfBelongToWayQueue.size()>=MAX_QUEUE_SIZE){
			ArrayList<Long> ids = new ArrayList<Long>(addNodeIfBelongToWayQueue.size());
			for(Node n:addNodeIfBelongToWayQueue){
				ids.add(n.getId());
			}
			
			HashSet<Long> neededIds = new HashSet<Long>();
			String inClause = Util.join(",", ids.toArray());

			SQLiteDatabase db = getWritableDatabase();
			Cursor cur = null;
			try{
				Log.d("Checking nodes in ways: "+inClause);
				cur = db.rawQuery("SELECT node_id FROM "+Queries.WAY_NODS_TABLE+" WHERE node_id in ("+inClause+")", null);
				if (cur.moveToFirst()){
					do{
						neededIds.add(cur.getLong(0));
					}while(cur.moveToNext());
				}
			}catch(Exception e){
				Log.wtf("addNodeIfBelongsToWay ids in "+inClause, e);
			}finally{
				if (cur!=null) cur.close();
			}
			
			for(Node n:addNodeIfBelongToWayQueue){
				if (neededIds.contains(n.getId())){
					super.addNode(n);
				}
			}
			
			addNodeIfBelongToWayQueue.clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addNodeIfBelongsToRelation(il.yrtimid.osm.osmpoi.domain.Node)
	 */
	@Override
	public void addNodeIfBelongsToRelation(Node node){
		if (node != null){
			addNodeIfBelongToRelationQueue.add(node);
		}
		
		if (flushing || addNodeIfBelongToRelationQueue.size()>=MAX_QUEUE_SIZE){
			ArrayList<Long> ids = new ArrayList<Long>(addNodeIfBelongToRelationQueue.size());
			for(Node n:addNodeIfBelongToRelationQueue){
				ids.add(n.getId());
			}
			
			HashSet<Long> neededIds = new HashSet<Long>();
			String inClause = Util.join(",", ids.toArray());

			SQLiteDatabase db = getWritableDatabase();
			Cursor cur = null;
			try{
				Log.d("Checking nodes in relations: "+inClause);
				cur = db.rawQuery("SELECT ref FROM "+Queries.MEMBERS_TABLE+" WHERE type='NODE' AND ref in ("+inClause+")", null);
				if (cur.moveToFirst()){
					do{
						neededIds.add(cur.getLong(0));
					}while(cur.moveToNext());
				}
			}catch(Exception e){
				Log.wtf("addNodeIfBelongsToRelation ids in "+inClause, e);
			}finally{
				if (cur!=null) cur.close();
			}
			
			for(Node n:addNodeIfBelongToRelationQueue){
				if (neededIds.contains(n.getId())){
					super.addNode(n);
				}
			}
			
			addNodeIfBelongToRelationQueue.clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dal.IDbCachedFiller#addWayIfBelongsToRelation(il.yrtimid.osm.osmpoi.domain.Way)
	 */
	@Override
	public void addWayIfBelongsToRelation(Way way){
		if (way != null){
			addWayIfBelongToRelationQueue.add(way);
		}
		
		if (flushing || addWayIfBelongToRelationQueue.size()>=MAX_QUEUE_SIZE){
			ArrayList<Long> ids = new ArrayList<Long>(addWayIfBelongToRelationQueue.size());
			for(Way w:addWayIfBelongToRelationQueue){
				ids.add(w.getId());
			}
			
			HashSet<Long> neededIds = new HashSet<Long>();
			String inClause = Util.join(",", ids.toArray());

			SQLiteDatabase db = getWritableDatabase();
			Cursor cur = null;
			try{
				Log.d("Checking ways in relations: "+inClause);
				cur = db.rawQuery("SELECT ref FROM "+Queries.MEMBERS_TABLE+" WHERE type='WAY' AND ref in ("+inClause+")", null);
				if (cur.moveToFirst()){
					do{
						neededIds.add(cur.getLong(0));
					}while(cur.moveToNext());
				}
			}catch(Exception e){
				Log.wtf("addWayIfBelongsToRelation ids in "+inClause, e);
			}finally{
				if (cur!=null) cur.close();
			}
			
			for(Way w:addWayIfBelongToRelationQueue){
				if (neededIds.contains(w.getId())){
					super.addWay(w);
				}
			}
			
			addWayIfBelongToRelationQueue.clear();
		}
	}
}
