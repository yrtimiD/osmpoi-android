// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Contains data common to all entity types. This is separated from the entity
 * class to allow it to be instantiated before all the data required for a full
 * entity is available.
 */
public class CommonEntityData implements Parcelable {

	private long id;
	private long timestamp;
	private TagCollection tags;

	public CommonEntityData(Parcel source) {
		this.id = source.readLong();
		this.timestamp = source.readLong();
		this.tags = new TagCollection();
		source.readTypedList((List<Tag>) this.tags, Tag.CREATOR);
	}

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

	public static final Parcelable.Creator<CommonEntityData> CREATOR = new Parcelable.Creator<CommonEntityData>() {

		@Override
		public CommonEntityData createFromParcel(Parcel source) {
			return new CommonEntityData(source);
		}

		@Override
		public CommonEntityData[] newArray(int size) {
			return new CommonEntityData[size];
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(timestamp);
		dest.writeTypedList(tags);
	}

}
