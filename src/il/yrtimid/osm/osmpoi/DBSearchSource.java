/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import il.yrtimid.osm.osmpoi.dal.DbSearcher;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.parcelables.SearchParameters;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

import android.content.Context;

/**
 * @author yrtimid
 * 
 */
public class DBSearchSource implements ISearchSource {
	//private Context context;
	private DbSearcher db;
	
	public DBSearchSource(Context context) throws Exception {
		//this.context = context;
		db = new DbSearcher(context);
	}

	@Override
	public void close(){
		if (db != null)
			db.close();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * il.yrtimid.osm.osmpoi.SearchSource#getByKeyValue(il.yrtimid.osm.osmpoi
	 * .nodematchers.EntityMatcher, il.yrtimid.osm.osmpoi.NewItemNotifier)
	 */
	@Override
	public void getByDistanceAndKeyValue(SearchParameters search, TagMatcher matcher, ItemPipe<Entity> newItemNotifier, CancelFlag cancel) {
		db.findAroundPlaceByTag(search.getCenter(), matcher, search.getMaxResults(), newItemNotifier, cancel);		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * il.yrtimid.osm.osmpoi.SearchSource#getByDistance(il.yrtimid.osm.osmpoi
	 * .CircleArea, int, il.yrtimid.osm.osmpoi.NewItemNotifier)
	 */
	@Override
	public void getByDistance(SearchParameters search, ItemPipe<Entity> newItemNotifier, CancelFlag cancel) {
		db.findAroundPlace(search.getCenter(), search.getMaxResults(), newItemNotifier, cancel);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.SearchSource#getSupportsCancel()
	 */
	@Override
	public boolean isSupportsCancel() {
		return true;
	}

	public String getName(){
		return "DB search";
	}
}
