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
public class SearchByParentId extends SearchById implements android.os.Parcelable {

	public static final Parcelable.Creator<SearchByParentId> CREATOR = new Parcelable.Creator<SearchByParentId>() {
	
		@Override
		public SearchByParentId createFromParcel(Parcel source) {
			return new SearchByParentId(source);
		}
	
		@Override
		public SearchByParentId[] newArray(int size) {
			return new SearchByParentId[size];
		}
	};
	
	public SearchByParentId(EntityType entityType, long id){
		super(entityType, id);
	}
	
	public SearchByParentId(Parcel source){
		super(source);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
	}
	
}
