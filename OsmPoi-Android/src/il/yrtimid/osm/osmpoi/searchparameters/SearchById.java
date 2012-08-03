/**
 * 
 */
package il.yrtimid.osm.osmpoi.searchparameters;

import il.yrtimid.osm.osmpoi.domain.EntityType;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author yrtimid
 *
 */
public class SearchById extends BaseSearchParameter implements android.os.Parcelable {

	private long id;
	private EntityType type;
	
	public static final Parcelable.Creator<SearchById> CREATOR = new Parcelable.Creator<SearchById>() {
	
		@Override
		public SearchById createFromParcel(Parcel source) {
			return new SearchById(source);
		}
	
		@Override
		public SearchById[] newArray(int size) {
			return new SearchById[size];
		}
	};
	
	public SearchById(EntityType entityType, long id){
		this.maxResults = 0;
		this.id = id;
		this.type = entityType;
	}
	
	public SearchById(Parcel source){
		super(source);
		this.id = source.readLong();
		this.type = Enum.valueOf(EntityType.class, source.readString());
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setEntityType(EntityType type) {
		this.type = type;
	}
	
	public EntityType getEntityType() {
		return type;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(id);
		dest.writeString(type.name());
	}
	
}
