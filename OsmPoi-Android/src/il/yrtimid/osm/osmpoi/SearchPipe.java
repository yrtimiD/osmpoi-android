/**
 * 
 */
package il.yrtimid.osm.osmpoi;

/**
 * @author yrtimid
 *
 */
public interface SearchPipe<T> extends ItemPipe<T> {
	public void pushRadius(int radius);
}
