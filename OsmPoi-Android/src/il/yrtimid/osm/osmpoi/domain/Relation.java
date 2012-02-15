// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * A data class representing a single OSM relation.
 * 
 * @author Brett Henderson
 */
public class Relation extends Entity implements Comparable<Relation>, Parcelable {
	private List<RelationMember> members;
	

	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 */
	public Relation(CommonEntityData entityData) {
		super(entityData);
		
		this.members = new ArrayList<RelationMember>();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 * @param members
	 *            The members to apply to the object.
	 */
	public Relation(CommonEntityData entityData, List<RelationMember> members) {
		super(entityData);
		
		this.members = new ArrayList<RelationMember>(members);
	}
	
	public Relation(long relationId){
		this(new CommonEntityData(relationId, -1));
	}
	
	
	/**
	 * @param source
	 */
	public Relation(Parcel source) {
		super(source);
		members = new ArrayList<RelationMember>();
		source.readTypedList(members, RelationMember.CREATOR);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Relation;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Relation) {
			return compareTo((Relation) o) == 0;
		} else {
			return false;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		/*
		 * As per the hashCode definition, this doesn't have to be unique it
		 * just has to return the same value for any two objects that compare
		 * equal. Using both id and version will provide a good distribution of
		 * values but is simple to calculate.
		 */
		return (int) getId();
	}
	
	
	/**
	 * Compares this member list to the specified member list. The bigger list
	 * is considered bigger, if that is equal then each relation member is
	 * compared.
	 * 
	 * @param comparisonMemberList
	 *            The member list to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	protected int compareMemberList(Collection<RelationMember> comparisonMemberList) {
		Iterator<RelationMember> i;
		Iterator<RelationMember> j;
		
		// The list with the most entities is considered bigger.
		if (members.size() != comparisonMemberList.size()) {
			return members.size() - comparisonMemberList.size();
		}
		
		// Check the individual node references.
		i = members.iterator();
		j = comparisonMemberList.iterator();
		while (i.hasNext()) {
			int result = i.next().compareTo(j.next());
			
			if (result != 0) {
				return result;
			}
		}
		
		// There are no differences.
		return 0;
	}


	/**
	 * Compares this relation to the specified relation. The relation comparison
	 * is based on a comparison of id, version, timestamp, and tags in that order.
	 * 
	 * @param comparisonRelation
	 *            The relation to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	public int compareTo(Relation comparisonRelation) {
		int memberListResult;
		
		if (this.getId() < comparisonRelation.getId()) {
			return -1;
		}
		if (this.getId() > comparisonRelation.getId()) {
			return 1;
		}


		
		if (this.getTimestamp()<comparisonRelation.getTimestamp()) {
			return -1;
		}
		if (this.getTimestamp()>comparisonRelation.getTimestamp()) {
			return 1;
		}
		
		memberListResult = compareMemberList(
			comparisonRelation.members
		);
		
		if (memberListResult != 0) {
			return memberListResult;
		}
		
		return compareTags(comparisonRelation.getTags());
	}
	
	
	public List<RelationMember> getMembers() {
		return members;
	}

    /** 
     * ${@inheritDoc}.
     */
    @Override
    public String toString() {
        String type = null;
        Collection<Tag> tags = getTags();
        for (Tag tag : tags) {
            if (tag.getKey() != null && tag.getKey().equalsIgnoreCase("type")) {
                type = tag.getValue();
                break;
            }
        }
        if (type != null) {
            return "Relation(id=" + getId() + ", #tags=" +  getTags().size() + ", type='" + type + "')";
        }
        return "Relation(id=" + getId() + ", #tags=" +  getTags().size() + ")";
    }

    
	public static final Parcelable.Creator<Relation> CREATOR = new Parcelable.Creator<Relation>() {

		@Override
		public Relation createFromParcel(Parcel source) {
			return new Relation(source);
		}

		@Override
		public Relation[] newArray(int size) {
			return new Relation[size];
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
		dest.writeTypedList(members);
	}
}
