/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import il.yrtimid.osm.osmpoi.CancelFlag;
import il.yrtimid.osm.osmpoi.CollectionPipe;
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.Point;
import il.yrtimid.osm.osmpoi.SearchPipe;
import il.yrtimid.osm.osmpoi.Util;
import il.yrtimid.osm.osmpoi.domain.CommonEntityData;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Relation;
import il.yrtimid.osm.osmpoi.domain.RelationMember;
import il.yrtimid.osm.osmpoi.domain.Tag;
import il.yrtimid.osm.osmpoi.domain.Way;
import il.yrtimid.osm.osmpoi.searchparameters.SearchAround;
import il.yrtimid.osm.osmpoi.searchparameters.SearchById;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByKeyValue;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByParentId;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author yrtimid
 * 
 */
public class DbSearcher extends DbCreator {

	private static final int SEARCH_SIZE = 20;

	/**
	 * @param context
	 */
	public DbSearcher(Context context, File dbLocation) {
		super(context, dbLocation);
	}

	/**
	 * Returns count grid cells around p point
	 * 
	 * @param p
	 *            point to get cells around
	 * @param count
	 *            how much cells to return
	 * */
	private List<Integer> getGrid(Point p, int count) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cur = null;
		try {
			cur = db.rawQuery("select id from grid order by (abs((minLat+maxLat)/2-?)+abs((minLon+maxLon)/2-?)) limit ?", new String[] { p.getLatitude().toString(), p.getLongitude().toString(), Integer.toString(count) });

			List<Integer> ids = new ArrayList<Integer>();
			if (cur.moveToFirst()) {
				do {
					ids.add(cur.getInt(0));
				} while (cur.moveToNext());
			}

			return ids;
		} catch (Exception e) {
			Log.wtf("getGrid", e);
			return null;
		} finally {
			if (cur != null)
				cur.close();
		}
	}

	/**
	 * Distance in meters up to most distant cell corner
	 * 
	 * @param from
	 * @param cellId
	 * @return
	 */
	private Integer getDistanceToCell(Point from, int cellId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cur = null;
		Integer distance = 0;
		try {
			cur = db.rawQuery("select * from grid where id=?", new String[] { Integer.toString(cellId) });
			if (cur.moveToFirst()) {
				double minLat, minLon, maxLat, maxLon;
				minLat = cur.getDouble(cur.getColumnIndex("minLat"));
				minLon = cur.getDouble(cur.getColumnIndex("minLon"));
				maxLat = cur.getDouble(cur.getColumnIndex("maxLat"));
				maxLon = cur.getDouble(cur.getColumnIndex("maxLon"));

				int d1 = from.getDistance(minLat, minLon);
				int d2 = from.getDistance(minLat, maxLon);
				int d3 = from.getDistance(maxLat, minLon);
				int d4 = from.getDistance(maxLat, maxLon);

				distance = Math.max(d1, Math.max(d2, Math.max(d3, d4)));
			}
			Log.d("Distance from "+from.toString()+" to cell "+cellId+" is "+distance);
			return distance;
		} catch (Exception e) {
			Log.wtf("getDistanceToCell", e);
			return null;
		} finally {
			if (cur != null)
				cur.close();
		}
	}

	public boolean findAroundPlace(SearchAround search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		return find(FindType.AROUND_PLACE, search.getCenter(), null, search.getMaxResults(), newItemNotifier, cancel);
	}

	public boolean findAroundPlaceByTag(SearchByKeyValue search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {

		return find(FindType.BY_TAG, search.getCenter(), search.getMatcher(), search.getMaxResults(), newItemNotifier, cancel);
	}

	public boolean findById(SearchById search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		return getById(search.getEntityType(), search.getId(), newItemNotifier, cancel);
	}

	public boolean findByParentId(SearchByParentId search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		return getByParentId(search, newItemNotifier, cancel);
	}

	private boolean getById(EntityType entityType, Long id, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		if (cancel.isCancelled())
			return true;
		Entity result = null;

		Cursor cur = null;
		try {
			String sql = "SELECT * FROM " + entityTypeToTableName.get(entityType) + " WHERE id=?";
			SQLiteDatabase db = getReadableDatabase();
			cur = db.rawQuery(sql, new String[] { id.toString() });
			if (cur.moveToFirst()) {
				result = constructEntity(cur, entityType);
				fillTags(result);
				fillSubItems(result, cancel);
			}
		} catch (Exception e) {
			Log.wtf("getById", e);
			return false;
		} finally {
			if (cur != null)
				cur.close();
		}

		if (result != null)
			newItemNotifier.pushItem(result);

		return true;
	}

	private enum FindType {
		AROUND_PLACE, BY_TAG
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
		List<Integer> lastGrid = new ArrayList<Integer>();
		try {
			Cursor cur = null;
			do {
				List<Integer> grid = getGrid(point, gridSize * gridSize);
				Log.d("Grid size: " + grid.size());
				int radius = getDistanceToCell(point, grid.get(grid.size() - 1));
				newItemNotifier.pushRadius(radius);

				if (gridSize > 2) { // not first run, the whole grid may have
									// only one cell
					if (gridSize * gridSize > grid.size()) {
						if (grid.size() > ((gridSize - 1) * (gridSize - 1)))// new grid bigger than previous
							lastRun = true;
						else
							return true;
					}
				}
				
				grid.removeAll(lastGrid);
				lastGrid.addAll(grid);
				Integer[] gridIds = grid.toArray(new Integer[grid.size()]);
				
				int nodesCount = 0;
				int waysCount = 0;
				int relationsCount = 0;
				switch (findType) {
				case AROUND_PLACE:
					cur = getNodesAroundPlace(point, gridIds, maxResults > SEARCH_SIZE ? SEARCH_SIZE : maxResults, nodesOffset);
					nodesCount = readEntities(cur, EntityType.Node, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getWaysAroundPlace(point, gridIds, maxResults > SEARCH_SIZE ? SEARCH_SIZE : maxResults, waysOffset);
					waysCount = readEntities(cur, EntityType.Way, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getRelationsAroundPlace(point, gridIds, maxResults > SEARCH_SIZE ? SEARCH_SIZE : maxResults, relationsOffset);
					relationsCount = readEntities(cur, EntityType.Relation, maxResults, newItemNotifier, cancel);
					cur.close();
					break;
				case BY_TAG:
					cur = getNodesAroundPlaceByTag(point, gridIds, tagMatcher, maxResults > SEARCH_SIZE ? SEARCH_SIZE : maxResults, nodesOffset);
					nodesCount = readEntities(cur, EntityType.Node, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getWaysAroundPlaceByTag(point, gridIds, tagMatcher, maxResults > SEARCH_SIZE ? SEARCH_SIZE : maxResults, waysOffset);
					waysCount = readEntities(cur, EntityType.Way, maxResults, newItemNotifier, cancel);
					cur.close();
					cur = getRelationsAroundPlaceByTag(point, gridIds, tagMatcher, maxResults > SEARCH_SIZE ? SEARCH_SIZE : maxResults, relationsOffset);
					relationsCount = readEntities(cur, EntityType.Relation, maxResults, newItemNotifier, cancel);
					cur.close();
					break;
				}

				Log.d((nodesCount + waysCount) + " results");
				nodesOffset += nodesCount;
				waysOffset += waysCount;
				relationsOffset += relationsCount;
				maxResults -= nodesCount;
				maxResults -= waysCount;
				maxResults -= relationsCount;

				if ((nodesCount + waysCount + relationsCount) == 0 && maxResults > 0) {// query returned no results - it's time to wider search
					gridSize++;
				}
			} while (maxResults > 0 && cancel.isNotCancelled() && !lastRun);
			return true;
		} catch (Exception e) {
			Log.wtf("findAroundPlace", e);
			return false;
		} finally {
		}
	}

	/**
	 * @param maxResults
	 *            negative number or zero - no limit
	 */
	private int readEntities(Cursor cur, EntityType entityType, int maxResults, SearchPipe<Entity> notifier, CancelFlag cancel) {
		int count = 0;
		try {
			if (cur.moveToFirst()) {
				do {
					Entity entity = constructEntity(cur, entityType);
					fillTags(entity);
					fillSubItems(entity, cancel);
					notifier.pushItem(entity);

					maxResults--;
					count++;
					if (maxResults == 0)
						break;

				} while (cur.moveToNext() && cancel.isNotCancelled());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	private int readMembers(Cursor cur, EntityType entityType, SearchPipe<RelationMember> pipe, CancelFlag cancel) {
		int count = 0;
		Entity entity;
		try {
			if (cur.moveToFirst()) {
				do {
					entity = constructEntity(cur, entityType);
					fillTags(entity);
					fillSubItems(entity, cancel);

					String role = cur.getString(cur.getColumnIndex("role"));
					if (entity != null){
						RelationMember rm = new RelationMember(entity, role);
						
						pipe.pushItem(rm);
						count++;
					}
				} while (cur.moveToNext() && cancel.isNotCancelled());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	
	private Cursor getNodesAroundPlace(Point point, Integer[] gridIds, Integer limit, Integer offset) {
		SQLiteDatabase db;
		try {
			db = getReadableDatabase();
			String inClause = "grid_id in (" + Util.join(",", (Object[]) gridIds) + ")";

			String query = "select id, timestamp, lat, lon" + " from nodes" + " where " + inClause + " order by (abs(lat-?)+abs(lon-?))" + " limit ? offset ?";
			String[] args = new String[] { point.getLatitude().toString(), point.getLongitude().toString(), limit.toString(), offset.toString() };
			Log.d(query);
			Log.d(Util.join(", ", (Object[]) args));
			Cursor cur = db.rawQuery(query, args);
			return cur;
		} catch (Exception e) {
			Log.wtf("getNodesAroundPlace", e);
			return null;
		}
	}

	private Cursor getWaysAroundPlace(Point point, Integer[] gridIds, Integer limit, Integer offset) {
		SQLiteDatabase db;
		try {
			db = getReadableDatabase();
			String inClause = "grid_id in (" + Util.join(",", (Object[]) gridIds) + ")";

			String query = "select ways.id as id, nodes.id as node_id, ways.timestamp, lat, lon" 
					+ " from ways" 
						+ " inner join way_nodes on ways.id=way_nodes.way_id" 
						+ " inner join nodes on way_nodes.node_id=nodes.id" 
					+ " where " + inClause 
					+ " order by (abs(lat-?)+abs(lon-?))" 
					+ " limit ? offset ?";
			String[] args = new String[] { point.getLatitude().toString(), point.getLongitude().toString(), limit.toString(), offset.toString() };
			Log.d(query);
			Log.d(Util.join(", ", (Object[]) args));
			Cursor cur = db.rawQuery(query, args);
			return cur;
		} catch (Exception e) {
			Log.wtf("getWaysAroundPlace", e);
			return null;
		}
	}

	// TODO: implement
	private Cursor getRelationsAroundPlace(Point point, Integer[] gridIds, Integer limit, Integer offset) {
		SQLiteDatabase db;
		try {
			db = getReadableDatabase();
			String inClause = "grid_id in (" + Util.join(",", (Object[]) gridIds) + ")";
			String query ="select relations.id as id, relations.timestamp"
					+ " from relations"
					+ " 	inner join members on relations.id = members.relation_id"
					+ " 	inner join nodes on members.ref = nodes.id and members.type = 'Node'"
					+ " where " + inClause 
					+ " order by (abs(lat-?) + abs(lon-?))" 
					
					+ " union all"
					
					+ " select relations.id as id, relations.timestamp"
					+ " from relations"
					+ " 	inner join members on relations.id = members.relation_id"
					+ " 	inner join ways on members.ref = ways.id and members.type = 'Way'"
					+ " 	inner join way_nodes on ways.id = way_nodes.way_id"
					+ " 	inner join nodes on way_nodes.node_id = nodes.id"
					+ " where " + inClause 
					+ " order by (abs(lat-?) + abs(lon-?))";
			
			String outerQuery = "select * from ("+query+")v"
					+ " limit ? offset ?";

			String[] args = new String[] { point.getLatitude().toString(), point.getLongitude().toString(),
					point.getLatitude().toString(), point.getLongitude().toString(),
					limit.toString(), offset.toString() };
			Log.d(outerQuery);
			Log.d(Util.join(", ", (Object[]) args));
			Cursor cur = db.rawQuery(outerQuery, args);
			return cur;
		} catch (Exception e) {
			Log.wtf("getRelationsAroundPlace", e);
			return null;
		}
	}

	private Cursor getNodesAroundPlaceByTag(Point point, Integer[] gridIds, TagMatcher matcher, Integer limit, Integer offset) {
		SQLiteDatabase db;
		try {

			String inClause = "grid_id in (" + Util.join(",", (Object[]) gridIds) + ")";
			TagMatcherFormatter.WhereClause where = TagMatcherFormatter.format(matcher, "EXISTS (SELECT 1 FROM node_tags WHERE (%s) AND node_tags.node_id=nodes.id)");

			String query = "select distinct nodes.id, timestamp, lat, lon from nodes" 
					+ " where " + inClause + " AND ( " + where.where + " )" 
					+ " order by (abs(lat-?) + abs(lon-?))" 
					+ " limit ? offset ?";
			String[] args = new String[] { point.getLatitude().toString(), point.getLongitude().toString(), limit.toString(), offset.toString() };
			Log.d(query);
			Log.d(Util.join(", ", (Object[]) args));
			db = getReadableDatabase();
			Cursor cur = db.rawQuery(query, args);

			return cur;
		} catch (Exception e) {
			Log.wtf("getNodesAroundPlaceByTag", e);
			return null;
		}
	}

	private Cursor getWaysAroundPlaceByTag(Point point, Integer[] gridIds, TagMatcher matcher, Integer limit, Integer offset) {
		SQLiteDatabase db;
		try {
			db = getReadableDatabase();
			String inClause = "grid_id in (" + Util.join(",", (Object[]) gridIds) + ")";
			TagMatcherFormatter.WhereClause where = TagMatcherFormatter.format(matcher, "EXISTS (SELECT 1 FROM way_tags WHERE (%s) AND way_tags.way_id=ways.id)");

			String query = "select distinct ways.id as id, nodes.id as node_id, ways.timestamp, nodes.lat, nodes.lon from ways" 
						+ " inner join way_nodes on ways.id = way_nodes.way_id" 
						+ " inner join nodes on way_nodes.node_id = nodes.id" 
					+ " where " + inClause + " AND (" + where.where + ")" 
					+ " group by ways.id" 
					+ " order by (abs(lat-?) + abs(lon-?))" 
					+ " limit ? offset ?";
			String[] args = new String[] { point.getLatitude().toString(), point.getLongitude().toString(), limit.toString(), offset.toString() };
			Log.d(query);
			Log.d(Util.join(", ", (Object[]) args));
			Cursor cur = db.rawQuery(query, args);

			return cur;
		} catch (Exception e) {
			Log.wtf("getWaysAroundPlaceByTag", e);
			return null;
		}
	}

	/**
	 * Ignores point, just searches for any relation accordingly to matcher
	 * 
	 * @param point
	 * @param gridIds
	 * @param matcher
	 * @param limit
	 * @param offset
	 * @return
	 */
	private Cursor getRelationsAroundPlaceByTag(Point point, Integer[] gridIds, TagMatcher matcher, Integer limit, Integer offset) {
		SQLiteDatabase db;
		try {
			db = getReadableDatabase();
			TagMatcherFormatter.WhereClause where = TagMatcherFormatter.format(matcher, "EXISTS (SELECT 1 FROM relation_tags WHERE (%s) AND relation_tags.relation_id=relations.id)");			
			String inClause = "grid_id in (" + Util.join(",", (Object[]) gridIds) + ")";
			String query ="select relations.id as id, relations.timestamp, nodes.lat, nodes.lon"
					+ " from relations"
					+ " 	inner join members on relations.id = members.relation_id"
					+ " 	inner join nodes on members.ref = nodes.id and members.type = 'Node'"
					+ " where " + inClause + " AND ("+where.where+")"

					+ " union all"
					
					+ " select relations.id as id, relations.timestamp, nodes.lat, nodes.lon"
					+ " from relations"
					+ " 	inner join members on relations.id = members.relation_id"
					+ " 	inner join ways on members.ref = ways.id and members.type = 'Way'"
					+ " 	inner join way_nodes on ways.id = way_nodes.way_id"
					+ " 	inner join nodes on way_nodes.node_id = nodes.id"
					+ " where " + inClause + " AND ("+where.where+")"; 
					
			
			String outerQuery = "select id, timestamp from ("+query+")v"
					+ " order by (abs(lat-?) + abs(lon-?))"
					+ " limit ? offset ?";

			String[] args = new String[] { point.getLatitude().toString(), point.getLongitude().toString(),
					limit.toString(), offset.toString() };
			
			Log.d(outerQuery);
			Log.d(Util.join(", ", (Object[]) args));
			Cursor cur = db.rawQuery(outerQuery, args);

			return cur;
		} catch (Exception e) {
			Log.wtf("getRelationsAroundPlaceByTag", e);
			return null;
		}
	}

	private boolean getByParentId(SearchByParentId search, final SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		if (cancel.isCancelled())
			return true;

		switch (search.getEntityType()) {
		case Node:
			return true;
		case Way:
			return getNodesByWayId(search, newItemNotifier, cancel);
		case Relation:
			SearchPipe<RelationMember> pipe = new SearchPipe<RelationMember>() {
				@Override
				public void pushItem(RelationMember item) {
					newItemNotifier.pushItem(item.getMember());
				}
				
				@Override
				public void pushRadius(int radius) {
					newItemNotifier.pushRadius(radius);
				}
			};
			return getMembersByRelationId(search, pipe, cancel);
		}

		return false;
	}

	/**
	 * 
	 * @param search
	 *            if search has result count limiting - the center point will be used to find N nearest objects
	 */
	private boolean getNodesByWayId(SearchByParentId search, SearchPipe<Entity> notifier, CancelFlag cancel) {
		Cursor cur = null;
		Long wayId = search.getId();

		try {
			SQLiteDatabase db = getReadableDatabase();

			String sql = "SELECT n.* FROM " + Queries.NODES_TABLE + " n INNER JOIN " + Queries.WAY_NODES_TABLE + " w ON n.id=w.node_id WHERE w.way_id=?";
			String[] args;
			if (search.getMaxResults() > 0) {
				sql += " order by (abs(lat-?)+abs(lon-?)) limit ?";
				args = new String[] { wayId.toString(), search.getCenter().getLatitude().toString(), search.getCenter().getLongitude().toString(), search.getMaxResults().toString() };
			} else {
				args = new String[] { wayId.toString() };
			}
			cur = db.rawQuery(sql, args);
			readEntities(cur, EntityType.Node, search.getMaxResults(), notifier, cancel);
		} catch (Exception e) {
			Log.wtf("getNodesByWayId", e);
			return false;
		} finally {
			if (cur != null)
				cur.close();
		}

		return true;
	}

	/*private boolean getNodesAndWaysByRelationId(SearchByParentId search, SearchPipe<Entity> notifier, CancelFlag cancel) {
		Cursor cur = null;
		Long relationId = search.getId();
		try {
			SQLiteDatabase db = getReadableDatabase();
			Integer maxResults = search.getMaxResults();

			String sql = "SELECT n.* FROM " + NODES_TABLE + " n INNER JOIN " + MEMBERS_TABLE + " m ON m.Type='" + EntityType.Node + "' AND n.id=m.ref WHERE m.relation_id=?";
			String[] args;
			if (maxResults > 0) {
				sql += " order by (abs(lat-?)+abs(lon-?)) limit ?";
				args = new String[] { relationId.toString(), search.getCenter().getLatitude().toString(), search.getCenter().getLongitude().toString(), maxResults.toString() };
			} else {
				args = new String[] { relationId.toString() };
			}
			Log.d(sql);
			cur = db.rawQuery(sql, args);
			maxResults -= readEntities(cur, EntityType.Node, maxResults, notifier, cancel);
			cur.close();

			if (maxResults > 0) {
				sql = "SELECT w.* FROM " + WAYS_TABLE + " w INNER JOIN " + MEMBERS_TABLE + " m ON m.Type='" + EntityType.Way + "' AND w.id=m.ref WHERE m.relation_id=?";
				Log.d(sql);
				cur = db.rawQuery(sql, new String[] { relationId.toString() });
				maxResults -= readEntities(cur, EntityType.Way, maxResults, notifier, cancel);
				cur.close();
			}

			if (maxResults > 0) {
				sql = "SELECT r.* FROM " + RELATIONS_TABLE + " r INNER JOIN " + MEMBERS_TABLE + " m ON m.Type='" + EntityType.Relation + "' AND r.id=m.ref WHERE m.relation_id=?";
				Log.d(sql);
				cur = db.rawQuery(sql, new String[] { relationId.toString() });
				maxResults -= readEntities(cur, EntityType.Relation, maxResults, notifier, cancel);
				cur.close();
			}

		} catch (Exception e) {
			Log.wtf("getNodesAndWaysByRelationId", e);
			return false;
		} finally {
			if (cur != null)
				cur.close();
		}

		return true;
	}*/

	private boolean getMembersByRelationId(SearchByParentId search, SearchPipe<RelationMember> pipe, CancelFlag cancel) {
		Cursor cur = null;
		Long relationId = search.getId();
		try {
			SQLiteDatabase db = getReadableDatabase();
			Integer maxResults = search.getMaxResults();

			String sql = "SELECT n.*, role FROM " + Queries.NODES_TABLE + " n INNER JOIN " + Queries.MEMBERS_TABLE + " m ON m.Type='" + EntityType.Node + "' AND n.id=m.ref WHERE m.relation_id=?";
			sql += " order by (abs(lat-?)+abs(lon-?))";
			String[] args;
			if (maxResults > 0) {
				sql += " limit ?";
				args = new String[] { relationId.toString(), search.getCenter().getLatitude().toString(), search.getCenter().getLongitude().toString(), maxResults.toString() };
			} else {
				args = new String[] { relationId.toString(), search.getCenter().getLatitude().toString(), search.getCenter().getLongitude().toString() };
			}
			Log.d(sql);
			cur = db.rawQuery(sql, args);
			maxResults -= readMembers(cur, EntityType.Node, pipe, cancel);
			cur.close();

			if (maxResults > 0) {
				sql = "SELECT w.*, role FROM " + Queries.WAYS_TABLE + " w INNER JOIN " + Queries.MEMBERS_TABLE + " m ON m.Type='" + EntityType.Way + "' AND w.id=m.ref WHERE m.relation_id=?";
				Log.d(sql);
				cur = db.rawQuery(sql, new String[] { relationId.toString() });
				maxResults -= readMembers(cur, EntityType.Way, pipe, cancel);
				cur.close();
			}

			if (maxResults > 0) {
				sql = "SELECT r.*, role FROM " + Queries.RELATIONS_TABLE + " r INNER JOIN " + Queries.MEMBERS_TABLE + " m ON m.Type='" + EntityType.Relation + "' AND r.id=m.ref WHERE m.relation_id=?";
				Log.d(sql);
				cur = db.rawQuery(sql, new String[] { relationId.toString() });
				maxResults -= readMembers(cur, EntityType.Relation, pipe, cancel);
				cur.close();
			}

		} catch (Exception e) {
			Log.wtf("getNodesAndWaysByRelationId", e);
			return false;
		} finally {
			if (cur != null)
				cur.close();
		}

		return true;
	}
	
	private void fillTags(Entity entity) {
		Collection<Tag> tags = entity.getTags();
		SQLiteDatabase db = null;
		Cursor cur = null;
		try {
			db = getReadableDatabase();
			String sql = null;
			switch(entity.getType()){
			case Node:
				sql = "select k,v from " + Queries.NODES_TAGS_TABLE + " where node_id = ?";
				break;
			case Way:
				sql = "select k,v from " + Queries.WAY_TAGS_TABLE + " where way_id = ?";
				break;
			case Relation:
				sql = "select k,v from " + Queries.RELATION_TAGS_TABLE + " where relation_id = ?";
				break;
			}
			
			cur = db.rawQuery(sql, new String[] { Long.toString(entity.getId()) });
			
			if (cur.moveToFirst()) {
				do {
					Tag t = constructTag(cur);
					tags.add(t);
				} while (cur.moveToNext());
			}
		} catch (Exception e) {
			Log.wtf("fillTags for "+entity.getType().name()+", with id "+entity.getId(), e);
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

	private Entity constructEntity(Cursor cur, EntityType entityType){
		switch (entityType) {
		case Node:
			return constructNode(cur);
		case Way:
			return constructWay(cur);
		case Relation:
			return constructRelation(cur);
		}
		return null;
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

	private Relation constructRelation(Cursor cur) {
		CommonEntityData entityData = constructEntity(cur);
		Relation rel = new Relation(entityData);
		return rel;
	}

	private void fillSubItems(Entity entity, CancelFlag cancel){
		SearchByParentId search = new SearchByParentId(entity.getType(), entity.getId());
		
		switch(entity.getType()){
		case Node:
			break;
		case Way:
			{
				CollectionPipe<Entity> pipe = new CollectionPipe<Entity>();
				getNodesByWayId(search, pipe, cancel);
				List<Node> list = ((Way)entity).getWayNodes();
				for(Entity e: pipe.getItems()){
					list.add((Node)e);
				}
			}
			break;
		case Relation:
			{
				CollectionPipe<RelationMember> pipe = new CollectionPipe<RelationMember>();
				getMembersByRelationId(search, pipe, cancel);
				((Relation)entity).getMembers().addAll(pipe.getItems());
			}
			break;
		}
	}
}
