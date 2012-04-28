/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.searchparameters.BaseSearchParameter;
import il.yrtimid.osm.osmpoi.searchparameters.SearchAround;
import il.yrtimid.osm.osmpoi.searchparameters.SearchById;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByKeyValue;


/**
 * @author yrtimid
 *
 */
public interface ISearchSource {
	//public abstract boolean isSupportsCancel(); 
	public void search(BaseSearchParameter search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
	public void getByDistance(SearchAround search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
	public void getByDistanceAndKeyValue(SearchByKeyValue search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
	public void getById(SearchById search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
	public void getByParentId(SearchById search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
	public abstract void close();
	public abstract String getName();
}



