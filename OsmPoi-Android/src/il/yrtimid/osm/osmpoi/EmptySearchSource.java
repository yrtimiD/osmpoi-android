/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import android.content.Context;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.searchparameters.BaseSearchParameter;

/**
 * @author Dmitry Gurovich (yrtimid at gmail.com)
 *
 */
public class EmptySearchSource implements ISearchSource {

	public static ISearchSource create(Context context) {
		return new EmptySearchSource();
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.ISearchSource#search(il.yrtimid.osm.osmpoi.searchparameters.BaseSearchParameter, il.yrtimid.osm.osmpoi.SearchPipe, il.yrtimid.osm.osmpoi.CancelFlag)
	 */
	@Override
	public void search(BaseSearchParameter search, SearchPipe<Entity> newItemNotifier, CancelFlag cancel) {
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.ISearchSource#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.ISearchSource#getName()
	 */
	@Override
	public String getName() {
		return "None";
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.ISearchSource#supportsInlineSearch()
	 */
	@Override
	public boolean supportsInlineSearch() {
		return false;
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.ISearchSource#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return false;
	}
}
