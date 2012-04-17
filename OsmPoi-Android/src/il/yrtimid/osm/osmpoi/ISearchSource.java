/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.parcelables.SearchParameters;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;



/**
 * @author yrtimid
 *
 */
public interface ISearchSource {
	public abstract boolean isSupportsCancel(); 
	public void getByDistance(SearchParameters search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
	public void getByDistanceAndKeyValue(SearchParameters search, TagMatcher matcher, SearchPipe<Entity> newItemNotifier, CancelFlag cancel);
	public abstract void close();
	public abstract String getName();
}



