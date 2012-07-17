/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Way;

/**
 * @author yrtimid
 *
 */
public interface IDbCachedFiller extends IDbFiller{

	public abstract void beginAdd();

	public abstract void endAdd();

	public abstract void addNodeIfBelongsToWay(Node node);

	public abstract void addNodeIfBelongsToRelation(Node node);

	public abstract void addWayIfBelongsToRelation(Way way);

}