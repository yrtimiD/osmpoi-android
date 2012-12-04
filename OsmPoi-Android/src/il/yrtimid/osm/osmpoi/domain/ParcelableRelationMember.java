// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A data class representing a single member within a relation entity.
 *
 * @author Brett Henderson
 */
public class ParcelableRelationMember implements Parcelable {
	
	protected RelationMember member;

	public ParcelableRelationMember(RelationMember member){
		this.member = member;
	}
	
	
	/**
	 * @param source
	 */
	public ParcelableRelationMember(Parcel source) {
		member = new RelationMember();
		member.setMemberRole(source.readString());
		EntityType memberType = Enum.valueOf(EntityType.class, source.readString());
		//TODO: check creating from parcel
		ParcelableEntity pEntity = source.readParcelable(Entity.class.getClassLoader());
		this.member.setMember(pEntity.getEntity());
		
	}
	
	/**
	 * @return the member
	 */
	public RelationMember getMember() {
		return member;
	}
	

	public static final Parcelable.Creator<ParcelableRelationMember> CREATOR = new Parcelable.Creator<ParcelableRelationMember>() {

		@Override
		public ParcelableRelationMember createFromParcel(Parcel source) {
			return new ParcelableRelationMember(source);
		}

		@Override
		public ParcelableRelationMember[] newArray(int size) {
			return new ParcelableRelationMember[size];
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
		dest.writeString(member.getMemberRole());
		dest.writeString(member.getMemberType().name());
		switch(member.getMemberType()){
		case Node:
			dest.writeParcelable(new ParcelableNode((Node)member.getMember()), flags);
			break;
		case Way:
			dest.writeParcelable(new ParcelableWay((Way)member.getMember()), flags);
			break;
		case Relation:
			dest.writeParcelable(new ParcelableRelation((Relation)member.getMember()), flags);
			break;
		default:
			break;
		}

	}
}
