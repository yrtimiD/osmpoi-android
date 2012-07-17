// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * A data class representing a single OSM entity. All top level data types
 * inherit from this class.
 * 
 * @author Brett Henderson
 */
public abstract class ParcelableEntity implements Parcelable {
	
	protected Entity entity;
	
	protected ParcelableEntity() {}
	
	public ParcelableEntity(Entity entity){
		this.entity = entity;
	}
	
	public void readFromParcel(Entity entity, Parcel source){
		this.entity = entity;
		ParcelableCommonEntityData parcelableCommonEntityData = source.readParcelable(ParcelableCommonEntityData.class.getClassLoader());
		this.entity.entityData = parcelableCommonEntityData.getCommonEntityData();
	}
	
	/**
	 * @return the entity
	 */
	public Entity getEntity() {
		return entity;
	}

	/* (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags){
		ParcelableCommonEntityData p = new ParcelableCommonEntityData(entity.entityData);
		
		dest.writeParcelable(p, flags);
	}
}
