/**
 * 
 */
package il.yrtimid.osm.osmpoi.formatters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.Tag;

/**
 * @author yrtimid
 *
 */
public class TagSelector {

	private String pattern;
	private static Pattern tagNamePattern = Pattern.compile("\\{([^\\}]+)\\}");
	private Collection<String> tagsInPattern = new ArrayList<String>();
	
	public TagSelector(String pattern) {
		this.pattern = pattern;
		Matcher tagMatcher = tagNamePattern.matcher(this.pattern);
		while(tagMatcher.find()){
			String name = tagMatcher.group(1);
			tagsInPattern.add(name);
		}
	}
	
	public String select(Entity entity, CharSequence localPostfix){
		if (tagsInPattern.size()>0){
			String result = pattern;

			Map<String,String> tags = entity.getTags().buildMap();
			for(String tagName:tagsInPattern){
				if (tags.containsKey(tagName+":"+localPostfix)) //trying local tag
					result = result.replace("{"+tagName+"}", tags.get(tagName+":"+localPostfix));
				if (tags.containsKey(tagName)) //trying normal tag
					result = result.replace("{"+tagName+"}", tags.get(tagName));
				if (tags.containsKey(tagName+":en")) //fall back to english version
					result = result.replace("{"+tagName+"}", tags.get(tagName+":en"));
				else //nothing found, remove place holder
					result = result.replace("{"+tagName+"}", "");
			}
			return result;
		}else {
			return pattern;
		}
	}
}
