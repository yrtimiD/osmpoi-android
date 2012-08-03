/**
 * 
 */
package il.yrtimid.osm.osmpoi.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yrtimid
 *
 */
public class TagCollection extends ArrayList<Tag> {
	private static final long serialVersionUID = -3930512868581794286L;

	/**
	 * Creates a new instance.
	 */
	public TagCollection() {
		super();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param tags
	 *            The initial tags.
	 */
	public TagCollection(Collection<? extends Tag> tags) {
		super(tags);
	}
	
	
	public Map<String, String> buildMap() {
		Map<String, String> tagMap;
		
		tagMap = new HashMap<String, String>(size());
		for (Tag tag : this) {
			tagMap.put(tag.getKey(), tag.getValue());
		}
		
		return tagMap;
	}
	
	static final Comparator<Tag> COMPARATOR = new Comparator<Tag>(){

		@Override
		public int compare(Tag object1, Tag object2) {
			return object1.compareTo(object2);
		}
		
	};
	
	public Collection<Tag> getSorted(){
		ArrayList<Tag> copy = new ArrayList<Tag>(this);
		Collections.sort(copy, COMPARATOR);
		return copy;
	}
}
