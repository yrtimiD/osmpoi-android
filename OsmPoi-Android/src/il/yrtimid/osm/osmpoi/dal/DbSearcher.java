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

import il.yrtimid.osm.osmpoi.CancelFlag;
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.ItemPipe;
import il.yrtimid.osm.osmpoi.Point;
import il.yrtimid.osm.osmpoi.Util;
import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.tagmatchers.IdMatcher;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

/**
 * @author yrtimid
 * 
 */
public class DbSearcher extends DbOpenHelper {

	private static final int SEARCH_SIZE = 20;  
	
	/**
	 * @param context
	 */
	public DbSearcher(Context context, File dbLocation) {
		super(context, dbLocation);
	}

	private Integer[] getGrid(Point p, int count){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cur = null;
		try{
			cur = db.rawQuery("select id from grid order by (abs((minLat+maxLat)/2-?)+abs((minLon+maxLon)/2-?)) limit ?;", 
					new String[]{p.getLatitude().toString(),p.getLongitude().toString(), Integer.toString(count)});
			
			List<Integer> ids = new ArrayList<Integer>();
			if (cur.moveToFirst()){
				do{
					ids.add(cur.getInt(0));
				}while(cur.moveToNext());
			}
			
			return ids.toArray(new Integer[ids.size()]);
		}catch(Exception e){
			Log.wtf("getGrid", e);
			return null;
		}finally{
			if (cur != null) cur.close();
		}
	}
	
	public boolean findAroundPlace(Point point, int maxResults, ItemPipe<Entity> newItemNotifier, CancelFlag cancel) {
		return find(FindType.AROUND_PLACE, point, null, maxResults, newItemNotifier, cancel);
	}
	
	public boolean findAroundPlaceByTag(Point point, TagMatcher tagMatcher, int maxResults, ItemPipe<Entity> newItemNotifier, CancelFlag cancel) {
		if (tagMatcher instanceof IdMatcher){
			return getById(point, (IdMatcher)tagMatcher, newItemNotifier, cancel);
		}else{ 
			return find(FindType.BY_TAG, point, tagMatcher, maxResults, newItemNotifier, cancel);
		}
	}


	private boolean getById(Point point, IdMatcher idMatcher, ItemPipe<Entity> newItemNotifier, CancelFlag cancel) {
		if(cancel.isCancelled()) return true;
		Entity result = null;
		switch(idMatcher.getEntityType()){
		case Node:
			result = getNodeById(point, idMatcher);
			break;
		case Way:
			result = getWayById(point, idMatcher);
			break;
		case Relation:
			//TODO: implement relations
			break;
		}
		
		if (result != null)
			newItemNotifier.pushItem(result);
		
		return true;
	}

	private Node getNodeById(Point point, IdMatcher idMatcher){
		Cursor cur = null;
		try{
			
			String sql = "SELECT * FROM nodes WHERE id=?";
			SQLiteDatabase db = getReadableDatabase();
			cur = db.rawQuery(sql, new String[]{idMatcher.getId().toString()});
			if (cur.moveToFirst()){
				Long id = cur.getLong(cur.getColumnIndex("id"));
				Node node = constructNode(cur, id);
				fillTags(node);
				return node;
			}
			
			return null;
		} catch (Exception e) {
			Log.wtf("getNodeById", e);
			return null;
		} finally {
			if (cur!= null) cur.close();
		}
	}
	
	private Way getWayById(Point point, IdMatcher idMatcher){
		Cursor cur = null;
		try{
			String sql = "select way_nodes.id as id, nodes.id as node_id, ways.timestamp, nodes.lat, nodes.lon"
								+" from way_nodes"
								+" inner join nodes on way_nodes.node_id = nodes.id"
								+ " WHERE way_nodes.id=?"
								+ " order by (abs(lat-?)+abs(lon-?))"
								+ " limit 1";
			SQLiteDatabase db = getReadableDatabase();
			cur = db.rawQuery(sql, new String[]{idMatcher.getId().toString(), point.getLatitude().toString(), point.getLongitude().toString()});
			if (cur.moveToFirst()){
				Long nodeId = cur.getLong(cur.getColumnIndex("node_id"));
				Node node = constructNode(cur, nodeId);
				Way way = constructWay(cur, node);
				fillTags(way);
				return way;
			}			
			return null;
		} catch (Exception e) {
			Log.wtf("getById", e);
			return null;
		} finally {
			if (cur!= null) cur.close();
		}
	}
	
	private enum FindType{
		AROUND_PLACE,
		BY_TAG
	}
	
	public boolean find(FindType findType, Point point, TagMatcher tagMatcher, int maxResults, ItemPipe<Entity> newItemNotifier, CancelFlag cancel) {
		int nodesOffset = 0;
		int waysOffset = 0;
		int nodesCount;
		int waysCount;
		int gridSize = 2;
		boolean lastRun = false;
		try {
			Cursor cur = null;
			do {
				Integer[] gridIds = getGrid(point, gridSize*gridSize);
				Log.d("Grid size: "+gridIds.length);
				if (gridSize*gridSize>gridIds.length){
					if (gridIds.length>((gridSize-1)*(gridSize-1)))//new grid bigger than previous
						lastRun = true;
					else
						return true;
				}
				
				nodesCount = 0;
				waysCount = 0;
				switch(findType){
				case AROUND_PLACE:
					cur = getNodesAroundPlace(point, gridIds, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, nodesOffset);
					nodesCount = readNodes(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getWaysAroundPlace(point, gridIds, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, nodesOffset);
					waysCount = readWays(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					break;
				case BY_TAG:
					cur = getNodesAroundPlaceByTag(point, gridIds, tagMatcher, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, nodesOffset);
					nodesCount = readNodes(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getWaysAroundPlaceByTag(point, gridIds, tagMatcher, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, waysOffset);
					waysCount = readWays(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					break;
				}
				
				Log.d((nodesCount+waysCount)+" results");
				nodesOffset += nodesCount;
				waysOffset += waysCount;
				maxResults-=nodesCount;
				maxResults-=waysCount;
				
				if ((nodesCount+waysCount) == 0 && maxResults>0){//query returned no results - time to wider search
					gridSize++;
				}
			} while (maxResults>0 && cancel.isNotCancelled() && !lastRun);
			return true;
		} catch (Exception e) {
			Log.wtf("findAroundPlace", e);
			return false;
		} finally {
		}
	}
	
	private int readNodes(Cursor cur, int maxResults, ItemPipe<Entity> notifier, CancelFlag cancel) {
		Map<Long, Node> nodes = new HashMap<Long, Node>();

		try {
			if (cur.moveToFirst()) {
				do {
					long id = cur.getLong(cur.getColumnIndex("id"));
					if (nodes.containsKey(id)) {
						continue;
					}

					Node n = constructNode(cur, id);
					nodes.put(id, n);
					fillTags(n);
					if (n.hasTags()) {
						notifier.pushItem(n);
						
						maxResults--;
						if (maxResults == 0) break;
					}
					
				} while (cur.moveToNext() && maxResults>0 && cancel.isNotCancelled());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return nodes.keySet().size();
	}

	private int readWays(Cursor cur, int maxResults, ItemPipe<Entity> notifier, CancelFlag cancel) {
		Map<Long, Way> ways = new HashMap<Long, Way>();

		try {
			if (cur.moveToFirst()) {
				do {
					long wayId = cur.getLong(cur.getColumnIndex("id"));
					long nodeId = cur.getLong(cur.getColumnIndex("node_id"));
					
					if (ways.containsKey(wayId)) continue;

					Node n = constructNode(cur, nodeId);
					Way w = constructWay(cur, n);
					fillTags(w);

					ways.put(w.getId(), w);
					notifier.pushItem(w);
						
					maxResults--;
					if (maxResults == 0) break;
					
					//Collection<Relation> relationsWithNode = getRelations(n);
					//TODO: implement relations getting
				} while (cur.moveToNext() && maxResults>0 && cancel.isNotCancelled());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ways.keySet().size();
	}
	
	private Cursor getNodesAroundPlace(Point point, Integer[] gridIds, Integer limit, Integer offset) {
		SQLiteDatabase db;
		try{
			db = getReadableDatabase();
			String inClause = "grid_id in ("+Util.join(",", (Object[])gridIds) +")";
			
			String query = "select id, timestamp, lat, lon"
					+ " from nodes"
					+ " where "+inClause
					+ " order by (abs(lat-?)+abs(lon-?))"
					+ " limit ? offset ?";
			String[] args = new String[] {point.getLatitude().toString(), point.getLongitude().toString(), limit.toString(), offset.toString()};
			Log.d(query);
			Log.d(Util.join(", ", (Object[])args));
			Cursor cur = db.rawQuery(query,args);
			return cur;
		}catch(Exception e){
			Log.wtf("getNodesAroundPlace", e);
			return null;
		}
	}

	private Cursor getWaysAroundPlace(Point point, Integer[] gridIds, Integer limit, Integer offset) {
		SQLiteDatabase db;
		try{
			db = getReadableDatabase();
			String inClause = "grid_id in ("+Util.join(",", (Object[])gridIds) +")";
			
			String query = "select ways.id as id, nodes.id as node_id, ways.timestamp, lat, lon"
					+ " from ways"
					+ " inner join way_nodes on ways.id=way_nodes.way_id"
					+ " inner join nodes on way_nodes.node_id=nodes.id"
					+ " where "+inClause
					+ " order by (abs(lat-?)+abs(lon-?))"
					+ " limit ? offset ?";
			String[] args = new String[] {point.getLatitude().toString(), point.getLongitude().toString(), limit.toString(), offset.toString()};
			Log.d(query);
			Log.d(Util.join(", ", (Object[])args));
			Cursor cur = db.rawQuery(query,args);
			return cur;
		}catch(Exception e){
			Log.wtf("getWaysAroundPlace", e);
			return null;
		}
	}
	
	private Cursor getNodesAroundPlaceByTag(Point point, Integer[] gridIds, TagMatcher matcher, Integer limit, Integer offset){
		SQLiteDatabase db;
		try {

			String inClause = "grid_id in ("+Util.join(",", (Object[])gridIds) +")";
			TagMatcherFormatter.WhereClause where = TagMatcherFormatter.format(matcher, "EXISTS (SELECT 1 FROM node_tags WHERE (%s) AND node_tags.node_id=nodes.id)");

			String query = "select distinct nodes.id as id, timestamp, lat, lon from nodes"
					+ " where "+inClause+" AND ( "+ where.where + " )"
					+ " order by (abs(lat-?) + abs(lon-?))"
					+ " limit ? offset ?";
			String[] args = new String[] { point.getLatitude().toString(), point.getLongitude().toString(), limit.toString(), offset.toString() };
			Log.d(query);
			Log.d(Util.join(", ", (Object[])args));
			db = getReadableDatabase();
			Cursor cur = db.rawQuery(query, args);
			
			return cur;
		} catch (Exception e) {
			Log.wtf("getNodesAroundPlaceByTag", e);
			return null;
		}
	}
	
	private Cursor getWaysAroundPlaceByTag(Point point, Integer[] gridIds, TagMatcher matcher, Integer limit, Integer offset){
		SQLiteDatabase db;
		try {
			db = getReadableDatabase(); 
			String inClause = "grid_id in ("+Util.join(",", (Object[])gridIds) +")";
			TagMatcherFormatter.WhereClause where = TagMatcherFormatter.format(matcher, "EXISTS (SELECT 1 FROM way_tags WHERE (%s) AND way_tags.way_id=ways.id)");

			String query = "select distinct ways.id as id, nodes.id as node_id, ways.timestamp, nodes.lat, nodes.lon from ways"
					+" inner join way_nodes on ways.id = way_nodes.way_id"
					+" inner join nodes on way_nodes.node_id = nodes.id"
					+ " where "+inClause+" AND ("+ where.where +")"
					+ " group by ways.id"
					+ " order by (abs(lat-?) + abs(lon-?))"
					+ " limit ? offset ?";
			String[] args = new String[] { point.getLatitude().toString(), point.getLongitude().toString(), limit.toString(), offset.toString() };
			Log.d(query);
			Log.d(Util.join(", ", (Object[])args));
			Cursor cur = db.rawQuery(query, args);
			
			return cur;
		} catch (Exception e) {
			Log.wtf("getWaysAroundPlaceByTag", e);
			return null;
		}
	}

	private void fillTags(Node node) {
		Collection<Tag> tags = node.getTags();
		SQLiteDatabase db = null;
		Cursor cur = null;
		try {
			db = getReadableDatabase();
			cur = db.rawQuery("select k,v from node_tags where node_id = ?", new String[] { Long.toString(node.getId()) });
			if (cur.moveToFirst()) {
				do {
					Tag t = constructTag(cur);
					tags.add(t);
				} while (cur.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cur != null)
				cur.close();
		}
	}

	private void fillTags(Way way) {
		Collection<Tag> tags = way.getTags();
		SQLiteDatabase db = null;
		Cursor cur = null;
		try {
			db = getReadableDatabase();
			cur = db.rawQuery("select k,v from way_tags where way_id = ?", new String[] { Long.toString(way.getId()) });
			if (cur.moveToFirst()) {
				do {
					Tag t = constructTag(cur);
					tags.add(t);
				} while (cur.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cur != null)
				cur.close();
		}
	}

	private CommonEntityData constructEntity(Cursor cur, long id) {
		long timestamp = cur.getLong(cur.getColumnIndex("timestamp"));
		CommonEntityData entityData = new CommonEntityData(id, timestamp);
		return entityData;
	}

	private Node constructNode(Cursor cur, long id) {
		CommonEntityData entityData = constructEntity(cur, id);
		Double lat = cur.getDouble(cur.getColumnIndex("lat"));
		Double lon = cur.getDouble(cur.getColumnIndex("lon"));
		return new Node(entityData, lat, lon);
	}

	private Tag constructTag(Cursor cur) {
		String k = cur.getString(cur.getColumnIndex("k"));
		String v = cur.getString(cur.getColumnIndex("v"));
		return new Tag(k, v);
	}

	private Way constructWay(Cursor cur, Node node) {
		long id = cur.getLong(cur.getColumnIndex("id"));
		CommonEntityData entityData = constructEntity(cur, id);
		Way w = new Way(entityData);
		if (node != null)
			w.getWayNodes().add(node);
		return w;

	}
}
