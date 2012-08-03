// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Contains data common to all entity types. This is separated from the entity
 * class to allow it to be instantiated before all the data required for a full
 * entity is available.
 */
public class ParcelableCommonEntityData implements Parcelable {

	protected CommonEntityData data;
	
	public ParcelableCommonEntityData(Parcel source) {
		data = new CommonEntityData();
		data.id = source.readLong();
		data.timestamp = source.readLong();
		data.tags = new TagCollection();
		List<ParcelableTag> pTags = new ArrayList<ParcelableTag>();
		source.readTypedList((List<ParcelableTag>) pTags, ParcelableTag.CREATOR);
		for(ParcelableTag pTag : pTags){
			data.tags.add(pTag.getTag());
		}
	}
	
	public ParcelableCommonEntityData(CommonEntityData data){
		this.data = data;
	}

	/**
	 * @return the data
	 */
	public CommonEntityData getCommonEntityData() {
		return data;
	}

	public static final Parcelable.Creator<ParcelableCommonEntityData> CREATOR = new Parcelable.Creator<ParcelableCommonEntityData>() {

		@Override
		public ParcelableCommonEntityData createFromParcel(Parcel source) {
			return new ParcelableCommonEntityData(source);
		}

		@Override
		public ParcelableCommonEntityData[] newArray(int size) {
			return new ParcelableCommonEntityData[size];
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
		dest.writeLong(data.id);
		dest.writeLong(data.timestamp);
		List<ParcelableTag> pTags = new ArrayList<ParcelableTag>();
		for(Tag tag : data.tags){
			pTags.add(new ParcelableTag(tag));
		}
		dest.writeTypedList(pTags);
	}

}
