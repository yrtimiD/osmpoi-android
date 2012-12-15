package il.yrtimid.osm.osmpoi;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import il.yrtimid.osm.osmpoi.dal.OsmOverpassAPI;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.searchparameters.BaseSearchParameter;
import il.yrtimid.osm.osmpoi.searchparameters.SearchAround;
import il.yrtimid.osm.osmpoi.searchparameters.SearchById;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByKeyValue;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByParentId;
import il.yrtimid.osm.osmpoi.tagmatchers.KeyValueMatcher;

public class OverpassAPISearchSource implements ISearchSource {

	public static ISearchSource create(Context context) {
		return new OverpassAPISearchSource();
	}

	protected OverpassAPISearchSource() {
	}

	@Override
	public void search(BaseSearchParameter search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		if (search instanceof SearchByKeyValue) {
			getByDistanceAndKeyValue((SearchByKeyValue) search, newItemNotifier, cancel);
		} else if (search instanceof SearchAround) {
			//getByDistance((SearchAround) search, newItemNotifier, cancel);
		} else if (search instanceof SearchByParentId) {
			//getByParentId((SearchByParentId) search, newItemNotifier, cancel);
		} else if (search instanceof SearchById) {
			//getById((SearchById) search, newItemNotifier, cancel);
		}
	}

	public void getByDistanceAndKeyValue(SearchByKeyValue search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
		Point c = search.getCenter();
		Double radius = 0.0d;
		int results = 0;
		while (results < search.getMaxResults()){
			if (cancel.isCancelled()) break;
			
			radius += 0.01d;
			int radiusInMeters = c.getDistance(c.latitude+radius, c.longitude+radius);
			newItemNotifier.pushRadius(radiusInMeters);
			
			List<Entity> result;
			try {
				result = OsmOverpassAPI.Search(c.latitude, c.longitude, radius, search.getMatcher());
			} catch (IOException e1) {
				e1.printStackTrace();
				break;
			}
			for(Entity e : result){
				newItemNotifier.pushItem(e);
			}
			results+=result.size();
		}
	}

	
//	public void getByDistance(SearchAround search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
//		// TODO Auto-generated method stub
//
//	}
//
//	public void getById(SearchById search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
//		// TODO Auto-generated method stub
//
//	}
//
//	public void getByParentId(SearchByParentId search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
//		// TODO Auto-generated method stub
//
	// }

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "OverpassAPI search";
	}

}
