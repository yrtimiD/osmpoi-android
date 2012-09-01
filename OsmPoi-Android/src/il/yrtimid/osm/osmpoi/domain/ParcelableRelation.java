// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * A data class representing a single OSM relation.
 * 
 * @author Brett Henderson
 */
public class ParcelableRelation extends ParcelableEntity implements Parcelable {
	
	protected Relation rel;

	public ParcelableRelation(Relation rel){
		super(rel);
		this.rel = rel;
	}
	
	/**
	 * @param source
	 */
	public ParcelableRelation(Parcel source) {
		this(new Relation());
		super.readFromParcel(rel, source);
		List<ParcelableRelationMember> pMembers = new ArrayList<ParcelableRelationMember>();
		source.readTypedList(pMembers, ParcelableRelationMember.CREATOR);
		for(ParcelableRelationMember pMember : pMembers){
			this.rel.getMembers().add(pMember.getMember());
		}
	}


	public static final Parcelable.Creator<ParcelableRelation> CREATOR = new Parcelable.Creator<ParcelableRelation>() {

		@Override
		public ParcelableRelation createFromParcel(Parcel source) {
			return new ParcelableRelation(source);
		}

		@Override
		public ParcelableRelation[] newArray(int size) {
			return new ParcelableRelation[size];
		}
		
	};


	/* (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		List<ParcelableRelationMember> pMembers = new ArrayList<ParcelableRelationMember>();
		for(RelationMember member : rel.getMembers()){
			pMembers.add(new ParcelableRelationMember(member));
		}
		dest.writeTypedList(pMembers);
	}
}
