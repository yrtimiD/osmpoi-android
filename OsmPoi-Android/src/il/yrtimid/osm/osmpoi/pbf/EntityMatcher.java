/**
 * 
 */
package il.yrtimid.osm.osmpoi.pbf;

import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.Tag;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

/**
 * @author yrtimid
 *
 */
public class EntityMatcher {
	
	TagMatcher tagMatcher;

	public EntityMatcher(TagMatcher tagMatcher) {
		this.tagMatcher = tagMatcher;
	}
	
	public boolean isMatch(Entity entity){
		for(Tag t:entity.getTags()){
			if (tagMatcher.isMatch(t.getKey(), t.getValue()))
				return true;
		}
		
		return false;
	}
}
