// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.security.InvalidParameterException;

/**
 * A data class representing a single member within a relation entity.
 *
 * @author Brett Henderson
 */
public class RelationMember implements Comparable<RelationMember>{
	
	private String memberRole;
	private Entity member = null;
	
	protected RelationMember(){}
	
	public RelationMember(Entity member, String memberRole) {
		this.memberRole = memberRole;
		this.member = member;
	}
	
	public RelationMember(long memberId, EntityType memberType, String memberRole) {
		this.memberRole = memberRole;
		
		switch(memberType){
		case Node:
			this.member = new Node(memberId);
			break;
		case Way:
			this.member = new Way(memberId);
			break;
		case Relation:
			this.member = new Relation(memberId);
			break;
		default:
			throw new InvalidParameterException("Unsupported member type: "+memberType.name());
		}
	}
	
	
	/**
	 * Compares this relation member to the specified relation member. The
	 * relation member comparison is based on a comparison of member type, then
	 * member id, then role.
	 * 
	 * @param relationMember
	 *            The relation member to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	public int compareTo(RelationMember relationMember) {
		long result;
		
		// Compare the member type.
		result = this.getMemberType().compareTo(relationMember.getMemberType());
		if (result > 0) {
			return 1;
		} else if (result < 0) {
			return -1;
		}
		
		// Compare the member id.
		result = this.getMemberId() - relationMember.getMemberId();
		if (result > 0) {
			return 1;
		} else if (result < 0) {
			return -1;
		}
		
		// Compare the member role.
		result = this.memberRole.compareTo(relationMember.memberRole);
		if (result > 0) {
			return 1;
		} else if (result < 0) {
			return -1;
		}
		
		// No differences detected.
		return 0;
	}
	
	
	/**
	 * Returns the id of the member entity.
	 * 
	 * @return The member id.
	 */
	public long getMemberId() {
		return member.getId();
	}
	
	
	/**
	 * Returns the type of the member entity.
	 * 
	 * @return The member type.
	 */
	public EntityType getMemberType() {
		return member.getType();
	}
	

	/**
	 * Returns the role that this member forms within the relation.
	 * 
	 * @return The role.
	 */
	public String getMemberRole() {
		return memberRole;
	}

	/**
	 * @param memberRole the memberRole to set
	 */
	public void setMemberRole(String memberRole) {
		this.memberRole = memberRole;
	}

	/**
	 * @return the member
	 */
	public Entity getMember() {
		return member;
	}
	
	/**
	 * @param member the member to set
	 */
	public void setMember(Entity member) {
		this.member = member;
	}
	
    /** 
     * ${@inheritDoc}.
     */
    @Override
    public String toString() {
        return "RelationMember(" + getMemberType() + " with id " + getMemberId() + " in the role '" + getMemberRole()
				+ "')";
    }

}
