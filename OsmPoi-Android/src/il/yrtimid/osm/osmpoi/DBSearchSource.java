/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import il.yrtimid.osm.osmpoi.OsmPoiApplication.Config;
import il.yrtimid.osm.osmpoi.dal.DbSearcher;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.searchparameters.SearchAround;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByKeyValue;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

import android.content.Context;

/**
 * @author yrtimid
 * 
 */
public class DBSearchSource implements ISearchSource {
	//private Context context;
	private DbSearcher poiDb;
	private DbSearcher addressDb;
	
	public static ISearchSource create(Context context){
		if (Config.getPoiDbLocation() != null && Config.getAddressDbLocation() != null)
			return new DBSearchSource(context);
		else
			return null;
	}
	
	protected DBSearchSource(Context context){
		//this.context = context;
		poiDb = OsmPoiApplication.databases.getPoiSearcherDb();
		addressDb = OsmPoiApplication.databases.getAddressSearcherDb();
	}

	@Override
	public void close(){
		/*
		if (poiDb != null)
			poiDb.close();

		if (addressDb != null)
			addressDb.close();
			*/
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * il.yrtimid.osm.osmpoi.SearchSource#getByKeyValue(il.yrtimid.osm.osmpoi
	 * .nodematchers.EntityMatcher, il.yrtimid.osm.osmpoi.NewItemNotifier)
	 */
	@Override
	public void getByDistanceAndKeyValue(SearchByKeyValue search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		poiDb.findAroundPlaceByTag(search.getCenter(), search.getMatcher(), search.getMaxResults(), newItemNotifier, cancel);		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * il.yrtimid.osm.osmpoi.SearchSource#getByDistance(il.yrtimid.osm.osmpoi
	 * .CircleArea, int, il.yrtimid.osm.osmpoi.NewItemNotifier)
	 */
	@Override
	public void getByDistance(SearchAround search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		poiDb.findAroundPlace(search.getCenter(), search.getMaxResults(), newItemNotifier, cancel);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.SearchSource#getSupportsCancel()
	 */
	@Override
	public boolean isSupportsCancel() {
		return true;
	}

	public String getName(){
		return "Offline search";
	}
}
