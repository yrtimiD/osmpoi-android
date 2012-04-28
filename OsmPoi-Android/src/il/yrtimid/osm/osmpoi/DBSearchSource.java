/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import il.yrtimid.osm.osmpoi.OsmPoiApplication.Config;
import il.yrtimid.osm.osmpoi.dal.DbSearcher;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.searchparameters.BaseSearchParameter;
import il.yrtimid.osm.osmpoi.searchparameters.SearchAround;
import il.yrtimid.osm.osmpoi.searchparameters.SearchById;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByKeyValue;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByParentId;

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

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.ISearchSource#search(il.yrtimid.osm.osmpoi.searchparameters.BaseSearchParameter, il.yrtimid.osm.osmpoi.SearchPipe, il.yrtimid.osm.osmpoi.CancelFlag)
	 */
	@Override
	public void search(BaseSearchParameter search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		if (search instanceof SearchByKeyValue){
			getByDistanceAndKeyValue((SearchByKeyValue)search, newItemNotifier, cancel);
		}else if (search instanceof SearchAround){
			getByDistance((SearchAround)search, newItemNotifier, cancel);
		}else if (search instanceof SearchByParentId){
			getByParentId((SearchByParentId)search, newItemNotifier, cancel);
		}else if (search instanceof SearchById){
			getById((SearchById)search, newItemNotifier, cancel);
		}
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
		poiDb.findAroundPlaceByTag(search, newItemNotifier, cancel);		
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
		poiDb.findAroundPlace(search, newItemNotifier, cancel);
	}

//	/* (non-Javadoc)
//	 * @see il.yrtimid.osm.osmpoi.SearchSource#getSupportsCancel()
//	 */
//	@Override
//	public boolean isSupportsCancel() {
//		return true;
//	}

	public String getName(){
		return "Offline search";
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.ISearchSource#getById(il.yrtimid.osm.osmpoi.searchparameters.SearchById, il.yrtimid.osm.osmpoi.SearchPipe, il.yrtimid.osm.osmpoi.CancelFlag)
	 */
	@Override
	public void getById(SearchById search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		poiDb.findById(search, newItemNotifier, cancel);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.ISearchSource#getByParentId(il.yrtimid.osm.osmpoi.searchparameters.SearchById, il.yrtimid.osm.osmpoi.SearchPipe, il.yrtimid.osm.osmpoi.CancelFlag)
	 */
	@Override
	public void getByParentId(SearchById search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		poiDb.findByParentId(search, newItemNotifier, cancel);
	}


}
