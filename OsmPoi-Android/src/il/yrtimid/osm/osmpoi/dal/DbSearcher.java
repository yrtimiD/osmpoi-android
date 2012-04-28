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
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Relation;
import il.yrtimid.osm.osmpoi.domain.Tag;
import il.yrtimid.osm.osmpoi.domain.Way;
import il.yrtimid.osm.osmpoi.searchparameters.SearchAround;
import il.yrtimid.osm.osmpoi.searchparameters.SearchById;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByKeyValue;
import il.yrtimid.osm.osmpoi.tagmatchers.AssociatedMatcher;
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
	
	public boolean findAroundPlace(SearchAround search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		return find(FindType.AROUND_PLACE, search.getCenter(), null, search.getMaxResults(), newItemNotifier, cancel);
	}
	
	public boolean findAroundPlaceByTag(SearchByKeyValue search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		
		return find(FindType.BY_TAG, search.getCenter(), search.getMatcher(), search.getMaxResults(), newItemNotifier, cancel);
	}
	
	public boolean findById(SearchById search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel){
		return getById(search.getEntityType(), search.getId(), newItemNotifier, cancel);
	}
	
	public boolean findByParentId(SearchById search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel){
		return getByParentId(search.getEntityType(), search.getId(), newItemNotifier, cancel);
	}

	private boolean getById(EntityType entityType, Long id, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		if(cancel.isCancelled()) return true;
		Entity result = null;
		
		Cursor cur = null;
		try{
			String sql = "SELECT * FROM "+entityTypeToTableName.get(entityType)+" WHERE id=?";
			SQLiteDatabase db = getReadableDatabase();
			cur = db.rawQuery(sql, new String[]{id.toString()});
			if (cur.moveToFirst()){
				switch(entityType){
				case Node:
					Node node = constructNode(cur);
					fillTags(node);
					result = node;
					break;
				case Way:
					Way way = constructWay(cur);
					fillTags(way);
					result = way;
					break;
				case Relation:
					Relation rel = constructRelation(cur);
					fillTags(rel);
					result = rel;
					break;
				}
			}
		} catch (Exception e) {
			Log.wtf("getById", e);
			return false;
		} finally {
			if (cur!= null) cur.close();
		}
		
		if (result != null)
			newItemNotifier.pushItem(result);
		
		return true;
	}
	
	private enum FindType{
		AROUND_PLACE,
		BY_TAG
	}
	
	/**
	 * Used for cases where result count isn't known
	 */
	private boolean find(FindType findType, Point point, TagMatcher tagMatcher, int maxResults, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
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
				
				if (gridSize > 2){ //not first run, the whole grid may have only one cell
					if (gridSize*gridSize>gridIds.length){
						if (gridIds.length>((gridSize-1)*(gridSize-1)))//new grid bigger than previous
							lastRun = true;
						else
							return true;
					}
				}
				int nodesCount = 0;
				int waysCount = 0;
				int relationsCount = 0;
				switch(findType){
				case AROUND_PLACE:
					cur = getNodesAroundPlace(point, gridIds, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, nodesOffset);
					nodesCount = readNodes(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getWaysAroundPlace(point, gridIds, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, waysOffset);
					waysCount = readWays(cur, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getRelationsAroundPlace(point, gridIds, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, relationsOffset);
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
					cur = getRelationsAroundPlaceByTag(point, gridIds, tagMatcher, maxResults>SEARCH_SIZE?SEARCH_SIZE:maxResults, relationsOffset);
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
				
				if ((nodesCount+waysCount+relationsCount) == 0 && maxResults>0){//query returned no results - time to wider search
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
	
	/**
	 * @param maxResults negative number - no limit
	 */
	private int readNodes(Cursor cur, int maxResults, SearchPipe<Entity> notifier, CancelFlag cancel) {
		int count = 0;
		try {
			if (cur.moveToFirst()) {
				do {
					Node n = constructNode(cur);
					fillTags(n);
					notifier.pushItem(n);
						
					maxResults--;
					count++;
					if (maxResults == 0) break;
					
				} while (cur.moveToNext() && cancel.isNotCancelled());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return count;
	}

	/**
	 * @param maxResults negative number - no limit
	 */
	private int readWays(Cursor cur, int maxResults, SearchPipe<Entity> notifier, CancelFlag cancel) {
		int count = 0;
		try {
			if (cur.moveToFirst()) {
				do {
					Way w = constructWay(cur);
					fillTags(w);
					notifier.pushItem(w);
						
					maxResults--;
					count++;
					if (maxResults == 0) break;
					
				} while (cur.moveToNext() && cancel.isNotCancelled());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return count;
	}
	
	/**
	 * @param maxResults negative number - no limit
	 */
	private int readRelations(Cursor cur, int maxResults, SearchPipe<Entity> notifier, CancelFlag cancel) {
		int count = 0;
		try {
			if (cur.moveToFirst()) {
				do {
					Relation r = constructRelation(cur);
					fillTags(r);
					notifier.pushItem(r);
						
					maxResults--;
					count++;
					if (maxResults == 0) break;
					
				} while (cur.moveToNext() && cancel.isNotCancelled());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return count;
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

	private boolean getByParentId(EntityType entityType, Long id, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		if(cancel.isCancelled()) return true;

		switch(entityType){
		case Node:
			return true;
		case Way:
			return getNodesByWayId(id, newItemNotifier, cancel);
		case Relation:
			return getNodesAndWaysByRelationId(id, newItemNotifier, cancel);
		}
		
		return false;
	}
	
	private boolean getNodesByWayId(Long wayId, SearchPipe<Entity> notifier, CancelFlag cancel){
		Cursor cur = null;
		try{
			String sql = "SELECT n.* FROM "+NODES_TABLE+" n INNER JOIN "+WAY_NODS_TABLE+" w ON n.id=w.node_id WHERE w.way_id=?";
			SQLiteDatabase db = getReadableDatabase();
			cur = db.rawQuery(sql, new String[]{wayId.toString()});
			readNodes(cur, -1, notifier, cancel);
		} catch (Exception e) {
			Log.wtf("getNodesByWayId", e);
			return false;
		} finally {
			if (cur!= null) cur.close();
		}
		
		return true;
	}
	
	private boolean getNodesAndWaysByRelationId(Long relationId, SearchPipe<Entity> notifier, CancelFlag cancel){
		Cursor cur = null;
		try{
			SQLiteDatabase db = getReadableDatabase();
			
			String sql = "SELECT n.* FROM "+NODES_TABLE+" n INNER JOIN "+MEMBERS_TABLE+" m ON m.Type='"+EntityType.Node+"' AND n.id=m.ref WHERE m.relation_id=?";
			Log.d(sql);
			cur = db.rawQuery(sql, new String[]{relationId.toString()});
			readNodes(cur, -1, notifier, cancel);
			cur.close();
			
			sql = "SELECT w.* FROM "+WAYS_TABLE+" w INNER JOIN "+MEMBERS_TABLE+" m ON m.Type='"+EntityType.Way+"' AND w.id=m.ref WHERE m.relation_id=?";
			Log.d(sql);
			cur = db.rawQuery(sql, new String[]{relationId.toString()});
			readWays(cur, -1, notifier, cancel);
			cur.close();

			sql = "SELECT r.* FROM "+RELATIONS_TABLE+" r INNER JOIN "+MEMBERS_TABLE+" m ON m.Type='"+EntityType.Relation+"' AND r.id=m.ref WHERE m.relation_id=?";
			Log.d(sql);
			cur = db.rawQuery(sql, new String[]{relationId.toString()});
			readRelations(cur, -1, notifier, cancel);
			cur.close();

		} catch (Exception e) {
			Log.wtf("getNodesAndWaysByRelationId", e);
			return false;
		} finally {
			if (cur!= null) cur.close();
		}

		return true;
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
	
	private CommonEntityData constructEntity(Cursor cur) {
		long id = cur.getLong(cur.getColumnIndex("id"));
		long timestamp = cur.getLong(cur.getColumnIndex("timestamp"));
		CommonEntityData entityData = new CommonEntityData(id, timestamp);
		return entityData;
	}

	private Node constructNode(Cursor cur) {
		CommonEntityData entityData = constructEntity(cur);
		Double lat = cur.getDouble(cur.getColumnIndex("lat"));
		Double lon = cur.getDouble(cur.getColumnIndex("lon"));
		return new Node(entityData, lat, lon);
	}

	private Tag constructTag(Cursor cur) {
		String k = cur.getString(cur.getColumnIndex("k"));
		String v = cur.getString(cur.getColumnIndex("v"));
		return new Tag(k, v);
	}

	private Way constructWay(Cursor cur) {
		CommonEntityData entityData = constructEntity(cur);
		Way w = new Way(entityData);
		return w;
	}
	
	private Relation constructRelation(Cursor cur){
		CommonEntityData entityData = constructEntity(cur);
		Relation rel = new Relation(entityData);
		return rel;
	}
}
