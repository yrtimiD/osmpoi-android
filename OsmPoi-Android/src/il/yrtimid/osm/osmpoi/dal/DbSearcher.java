/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import il.yrtimid.osm.osmpoi.CancelFlag;
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.Point;
import il.yrtimid.osm.osmpoi.SearchPipe;
import il.yrtimid.osm.osmpoi.Util;
import il.yrtimid.osm.osmpoi.domain.CommonEntityData;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Relation;
import il.yrtimid.osm.osmpoi.domain.Tag;
import il.yrtimid.osm.osmpoi.domain.Way;
import il.yrtimid.osm.osmpoi.tagmatchers.IdMatcher;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

	/** 
	 * Returns count grid cells around p point 
	 * @param p point to get cells around
	 * @param count how much cells to return
	 * */
	private Integer[] getGrid(Point p, int count){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cur = null;
		try{
			cur = db.rawQuery("select id from grid order by (abs((minLat+maxLat)/2-?)+abs((minLon+maxLon)/2-?)) limit ?", 
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
	
	/**
	 * Distance in meters up to most distant cell corner
	 * @param from
	 * @param cellId
	 * @return
	 */
	private Integer getDistanceToCell(Point from, int cellId){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cur = null;
		Integer distance = 0;
		try{
			cur = db.rawQuery("select * from grid where id=?", new String[]{Integer.toString(cellId)});
			if (cur.moveToFirst()){
				double  minLat, minLon, maxLat, maxLon;
				minLat = cur.getInt(cur.getColumnIndex("minLat"));
				minLon = cur.getInt(cur.getColumnIndex("minLon"));
				maxLat = cur.getInt(cur.getColumnIndex("maxLat"));
				maxLon = cur.getInt(cur.getColumnIndex("maxLon"));
				
				int d1 = from.getDistance(minLat, minLon);
				int d2 = from.getDistance(minLat, maxLon);
				int d3 = from.getDistance(maxLat, minLon);
				int d4 = from.getDistance(maxLat, maxLon);
				
				distance = Math.max(d1, Math.max(d2, Math.max(d3, d4)));
			}
			
			return distance;
		}catch(Exception e){
			Log.wtf("getDistanceToCell", e);
			return null;
		}finally{
			if (cur != null) cur.close();
		}
	}
	
	public boolean findAroundPlace(Point point, int maxResults, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		return find(FindType.AROUND_PLACE, point, null, maxResults, newItemNotifier, cancel);
	}
	
	public boolean findAroundPlaceByTag(Point point, TagMatcher tagMatcher, int maxResults, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		if (tagMatcher instanceof IdMatcher){
			return getById(point, (IdMatcher)tagMatcher, newItemNotifier, cancel);
		}else{ 
			return find(FindType.BY_TAG, point, tagMatcher, maxResults, newItemNotifier, cancel);
		}
	}


	private boolean getById(Point point, IdMatcher idMatcher, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
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
			result = getRelationById(point, idMatcher);
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
	
	/**
	 * Returns way populated with single nearest to point node 
	 * @param point
	 * @param idMatcher
	 * @return
	 */
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
			Log.wtf("getWayById", e);
			return null;
		} finally {
			if (cur!= null) cur.close();
		}
	}
	
	private Relation getRelationById(Point point, IdMatcher idMatcher){
		Cursor cur = null;
		try{
			String sql = "SELECT id, timestamp FROM "+RELATIONS_TABLE+" WHERE id=?";
			SQLiteDatabase db = getReadableDatabase();
			cur = db.rawQuery(sql, new String[]{idMatcher.getId().toString()});
			if (cur.moveToFirst()){
				Relation rel = constructRelation(cur);
				fillTags(rel);
				return rel;
			}			
			return null;
		} catch (Exception e) {
			Log.wtf("getRelationById", e);
			return null;
		} finally {
			if (cur!= null) cur.close();
		}
	}
	
	private enum FindType{
		AROUND_PLACE,
		BY_TAG
	}
	
	public boolean find(FindType findType, Point point, TagMatcher tagMatcher, int maxResults, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		int nodesOffset = 0;
		int waysOffset = 0;
		int relationsOffset = 0;
		
		int gridSize = 2;
		boolean lastRun = false;
		try {
			Cursor cur = null;
			do {
				Integer[] gridIds = getGrid(point, gridSize*gridSize);
				Log.d("Grid size: "+gridIds.length);
				int radius = getDistanceToCell(point, gridIds[gridIds.length-1]);
				newItemNotifier.pushRadius(radius);
				
				if (gridSize*gridSize>gridIds.length){
					if (gridIds.length>((gridSize-1)*(gridSize-1)))//new grid bigger than previous
						lastRun = true;
					else
						return true;
				}
				
				int nodesCount = 0;
				int waysCount = 0;
				int relationsCount = 0;
				switch(findType){
				case AROUND_PLACE:
					cur = getNodesAroundPlace(point, gridIds, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, nodesOffset);
					nodesCount = readNodes(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getWaysAroundPlace(point, gridIds, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, nodesOffset);
					waysCount = readWays(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getRelationsAroundPlace(point, gridIds, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, nodesOffset);
					relationsCount = readRelations(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					break;
				case BY_TAG:
					cur = getNodesAroundPlaceByTag(point, gridIds, tagMatcher, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, nodesOffset);
					nodesCount = readNodes(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getWaysAroundPlaceByTag(point, gridIds, tagMatcher, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, waysOffset);
					waysCount = readWays(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getRelationsAroundPlaceByTag(point, gridIds, tagMatcher, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, waysOffset);
					relationsCount = readRelations(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					break;
				}
				
				Log.d((nodesCount+waysCount)+" results");
				nodesOffset += nodesCount;
				waysOffset += waysCount;
				relationsOffset += relationsCount;
				maxResults-=nodesCount;
				maxResults-=waysCount;
				maxResults-=relationsCount;
				
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
	
	private int readNodes(Cursor cur, int maxResults, SearchPipe<Entity> notifier, CancelFlag cancel) {
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

	private int readWays(Cursor cur, int maxResults, SearchPipe<Entity> notifier, CancelFlag cancel) {
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
					
				} while (cur.moveToNext() && maxResults>0 && cancel.isNotCancelled());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ways.keySet().size();
	}
	
	private int readRelations(Cursor cur, int maxResults, SearchPipe<Entity> notifier, CancelFlag cancel) {
		Map<Long, Relation> rels = new HashMap<Long, Relation>();

		try {
			if (cur.moveToFirst()) {
				do {
					long relId = cur.getLong(cur.getColumnIndex("id"));
					if (rels.containsKey(relId)) continue;

					Relation r = constructRelation(cur);
					fillTags(r);

					rels.put(r.getId(), r);
					notifier.pushItem(r);
						
					maxResults--;
					if (maxResults == 0) break;
					
				} while (cur.moveToNext() && maxResults>0 && cancel.isNotCancelled());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rels.keySet().size();
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
	
	//TODO: implement
	private Cursor getRelationsAroundPlace(Point point, Integer[] gridIds, Integer limit, Integer offset) {
		SQLiteDatabase db;
		try{
			db = getReadableDatabase();
			String inClause = "grid_id in ("+Util.join(",", (Object[])gridIds) +")";
		/*	
			String queryRelByWays = "select relations.id as id, relations.timestamp"
					+ " from relations"
					+ " inner join members on relations.id=members.relation_id"
					+ " inner join way_nodes on members.type='WAY' AND members.ref=way_nodes.way_id"
					+ " inner join nodes on way_nodes.node_id=nodes.id"
					+ " where "+inClause
					+ " order by (abs(lat-?)+abs(lon-?))"
					+ " limit ? offset ?";
			
			String queryRelByNodes = "select relations.id as id, relations.timestamp"
					+ " from relations"
					+ " inner join members on relations.id=members.relation_id"
					+ " inner join way_nodes on members.type='WAY' AND members.ref=way_nodes.way_id"
					+ " inner join nodes on way_nodes.node_id=nodes.id"
					+ " where "+inClause
					+ " order by (abs(lat-?)+abs(lon-?))"
					+ " limit ? offset ?";
*/
			String query = "select relations.id as id, relations.timestamp from relations where 1=2";
			
			String[] args = new String[] {point.getLatitude().toString(), point.getLongitude().toString(), limit.toString(), offset.toString()};
			Log.d(query);
			Log.d(Util.join(", ", (Object[])args));
			Cursor cur = db.rawQuery(query,args);
			return cur;
		}catch(Exception e){
			Log.wtf("getRelationsAroundPlace", e);
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
	
	/**
	 * Ignores point, just searches for any relation accordingly to matcher
	 * @param point
	 * @param gridIds
	 * @param matcher
	 * @param limit
	 * @param offset
	 * @return
	 */
	private Cursor getRelationsAroundPlaceByTag(Point point, Integer[] gridIds, TagMatcher matcher, Integer limit, Integer offset){
		SQLiteDatabase db;
		try {
			db = getReadableDatabase(); 
			//String inClause = "grid_id in ("+Util.join(",", (Object[])gridIds) +")";
			TagMatcherFormatter.WhereClause where = TagMatcherFormatter.format(matcher, "EXISTS (SELECT 1 FROM relation_tags WHERE (%s) AND relation_tags.relation_id=relations.id)");

			String query = "select distinct relations.id, relations.timestamp from relations"
					+ " where ("+ where.where +")"
					+ " group by relations.id"
					+ " limit ? offset ?";
			String[] args = new String[] { limit.toString(), offset.toString() };
			Log.d(query);
			Log.d(Util.join(", ", (Object[])args));
			Cursor cur = db.rawQuery(query, args);
			
			return cur;
		} catch (Exception e) {
			Log.wtf("getRelationsAroundPlaceByTag", e);
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
			Log.wtf("fillTags node", e);
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
			Log.wtf("fillTags way", e);
		} finally {
			if (cur != null)
				cur.close();
		}
	}

	private void fillTags(Relation rel) {
		Collection<Tag> tags = rel.getTags();
		SQLiteDatabase db = null;
		Cursor cur = null;
		try {
			db = getReadableDatabase();
			cur = db.rawQuery("select k,v from "+RELATION_TAGS_TABLE+" where relation_id = ?", new String[] { Long.toString(rel.getId()) });
			if (cur.moveToFirst()) {
				do {
					Tag t = constructTag(cur);
					tags.add(t);
				} while (cur.moveToNext());
			}
		} catch (Exception e) {
			Log.wtf("fillTags relation", e);
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
	
	private Relation constructRelation(Cursor cur){
		long id = cur.getLong(cur.getColumnIndex("id"));
		CommonEntityData entityData = constructEntity(cur, id);
		Relation rel = new Relation(entityData);
		return rel;
	}
}
