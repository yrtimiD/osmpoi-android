/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import il.yrtimid.osm.osmpoi.domain.Entity;

/**
 * @author yrtimid
 *
 */
public class ResultItem {
	public Entity entity;
	public Integer radius;
	
	public ResultItem(Entity entity, Integer radius) {
		this.entity = entity;
		this.radius = radius;
	}
}
