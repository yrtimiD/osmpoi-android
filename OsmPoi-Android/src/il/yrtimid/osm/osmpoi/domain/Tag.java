// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A data class representing a single OSM tag.
 * 
 * @author Brett Henderson
 */
public class Tag implements Comparable<Tag>, Parcelable {

	/**
	 * The key identifying the tag.
	 */
	private String key;
	/**
	 * The value associated with the tag.
	 */
	private String value;

	/**
	 * Creates a new instance.
	 * 
	 * @param key
	 *            The key identifying the tag.
	 * @param value
	 *            The value associated with the tag.
	 */
	public Tag(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @param source
	 */
	public Tag(Parcel source) {
		this.key = source.readString();
		this.value = source.readString();
	}

	/**
	 * Compares this tag to the specified tag. The tag comparison is based on a
	 * comparison of key and value in that order.
	 * 
	 * @param tag
	 *            The tag to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	public int compareTo(Tag tag) {
		int keyResult;

		keyResult = this.key.compareTo(tag.key);

		if (keyResult != 0) {
			return keyResult;
		}

		return this.value.compareTo(tag.value);
	}

	/**
	 * @return The key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return The value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * ${@inheritDoc}.
	 */
	@Override
	public String toString() {
		return "Tag('" + getKey() + "'='" + getValue() + "')";
	}

	public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {

		@Override
		public Tag createFromParcel(Parcel source) {
			return new Tag(source);
		}

		@Override
		public Tag[] newArray(int size) {
			return new Tag[size];
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
		dest.writeString(key);
		dest.writeString(value);
	}
}
