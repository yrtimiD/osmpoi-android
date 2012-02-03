package il.yrtimid.osm.osmpoi.formatters;

import java.util.List;

import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

/**
 * @author yrtimid
 *
 */
public class EntityFormatter {
	TagMatcher matcher;
	TagSelector selector;
	
	public EntityFormatter(TagMatcher matcher, TagSelector selector) {
		this.matcher = matcher;
		this.selector = selector;
	}
	
	public static String format(List<EntityFormatter> formatters, Entity entity, CharSequence localPostfix){
		for(EntityFormatter formatter : formatters){
			if (formatter.matcher.isMatch(entity))
				return formatter.selector.select(entity, localPostfix);
		}
		
		return "ID: "+Long.toString(entity.getId());
	}
}
