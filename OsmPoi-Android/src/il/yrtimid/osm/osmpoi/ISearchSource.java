/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.searchparameters.*;


/**
 * @author yrtimid
 *
 */
public interface ISearchSource {
	//public abstract boolean isSupportsCancel(); 
	public void search(BaseSearchParameter search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
//	public void getByDistance(SearchAround search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
//	public void getByDistanceAndKeyValue(SearchByKeyValue search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
//	public void getById(SearchById search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
//	public void getByParentId(SearchByParentId search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
	public abstract void close();
	public abstract String getName();
	public abstract boolean supportsInlineSearch();
	public abstract boolean isAvailable();
}



