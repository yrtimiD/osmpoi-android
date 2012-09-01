/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

/**
 * @author yrtimid
 *
 */
public interface IDatabase {
	void create() throws Exception;
	void drop();
}
