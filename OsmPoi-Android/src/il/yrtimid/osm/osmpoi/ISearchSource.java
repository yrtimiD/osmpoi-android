/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.searchparameters.SearchAround;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByKeyValue;


/**
 * @author yrtimid
 *
 */
public interface ISearchSource {
	public abstract boolean isSupportsCancel(); 
	public void getByDistance(SearchAround search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
	public void getByDistanceAndKeyValue(SearchByKeyValue search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
	public abstract void close();
	public abstract String getName();
}



