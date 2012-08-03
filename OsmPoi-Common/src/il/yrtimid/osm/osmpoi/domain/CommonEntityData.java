// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Contains data common to all entity types. This is separated from the entity
 * class to allow it to be instantiated before all the data required for a full
 * entity is available.
 */
public class CommonEntityData {

	protected long id;
	protected long timestamp;
	protected TagCollection tags;

	protected CommonEntityData(){}
	
	public CommonEntityData(long id, Date date) {
		this(id, date.getTime(), new ArrayList<Tag>());
	}

	public CommonEntityData(long id, Date date, Collection<Tag> tags) {
		this(id, date.getTime(), tags);
	}
	
	public CommonEntityData(long id, long timestamp) {
		this(id, timestamp, new ArrayList<Tag>());
	}

	public CommonEntityData(long id, long timestamp, Collection<Tag> tags) {
		this.id = id;
		this.timestamp = timestamp;
		this.tags = new TagCollection(tags);
	}

	/**
	 * Gets the identifier.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the identifier.
	 * 
	 * @param id
	 *            The identifier.
	 */
	public void setId(long id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Returns the attached tags. If the class is read-only, the collection will
	 * be read-only.
	 * 
	 * @return The tags.
	 */
	public TagCollection getTags() {
		return tags;
	}

	/**
	 * Compares the tags on this entity to the specified tags. The tag
	 * comparison is based on a comparison of key and value in that order.
	 * 
	 * @param comparisonTags
	 *            The tags to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	protected int compareTags(Collection<Tag> comparisonTags) {
		List<Tag> tags1;
		List<Tag> tags2;

		tags1 = new ArrayList<Tag>(tags);
		tags2 = new ArrayList<Tag>(comparisonTags);

		Collections.sort(tags1);
		Collections.sort(tags2);

		// The list with the most tags is considered bigger.
		if (tags1.size() != tags2.size()) {
			return tags1.size() - tags2.size();
		}

		// Check the individual tags.
		for (int i = 0; i < tags1.size(); i++) {
			int result = tags1.get(i).compareTo(tags2.get(i));

			if (result != 0) {
				return result;
			}
		}

		// There are no differences.
		return 0;
	}
}
