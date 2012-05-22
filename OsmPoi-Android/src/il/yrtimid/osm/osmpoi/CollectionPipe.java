/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author yrtimid
 *
 */
public class CollectionPipe<T> implements SearchPipe<T> {

	private Collection<T> items = new ArrayList<T>();
	
	/**
	 * @return the items
	 */
	public Collection<T> getItems() {
		return items;
	}

	@Override
	public void pushItem(T item) {
		items.add(item);
	}

	@Override
	public void pushRadius(int radius) {
	}
}
