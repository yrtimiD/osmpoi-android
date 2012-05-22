/**
 * 
 */
package il.yrtimid.osm.osmpoi.searchparameters;

import il.yrtimid.osm.osmpoi.Point;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author yrtimid
 *
 */
public class SearchByParentId extends SearchById implements android.os.Parcelable {
	private Point center;
	
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
		this.center = new Point(0,0);
	}
	
	public SearchByParentId(Parcel source){
		super(source);
		this.center = new Point(source.readDouble(), source.readDouble());
	}

	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeDouble(center.getLatitude());
		dest.writeDouble(center.getLongitude());
	}
	
}
