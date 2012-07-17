// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A data class representing a single OSM tag.
 * 
 * @author Brett Henderson
 */
public class ParcelableTag implements Parcelable {

	private Tag tag;

	public ParcelableTag(Tag tag){
		this.tag = tag;
	}
	
	/**
	 * @param source
	 */
	public ParcelableTag(Parcel source) {
		String key = source.readString();
		String value = source.readString();
		this.tag = new Tag(key, value);
	}

	public Tag getTag(){
		return tag;
	}
	
	public static final Parcelable.Creator<ParcelableTag> CREATOR = new Parcelable.Creator<ParcelableTag>() {

		@Override
		public ParcelableTag createFromParcel(Parcel source) {
			return new ParcelableTag(source);
		}

		@Override
		public ParcelableTag[] newArray(int size) {
			return new ParcelableTag[size];
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
		dest.writeString(tag.getKey());
		dest.writeString(tag.getValue());
	}
}
