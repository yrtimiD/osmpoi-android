package il.yrtimid.osm.osmpoi.searchparameters;

import il.yrtimid.osm.osmpoi.Point;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Search for anything around point
 * @author yrtimid
 *
 */
public class SearchAround extends BaseSearchParameter implements android.os.Parcelable{
	private Point center;
	
	public static final Parcelable.Creator<SearchAround> CREATOR = new Parcelable.Creator<SearchAround>() {

		@Override
		public SearchAround createFromParcel(Parcel source) {
			return new SearchAround(source);
		}

		@Override
		public SearchAround[] newArray(int size) {
			return new SearchAround[size];
		}
	};
	
	public SearchAround(){
		this.center = new Point(0,0);
		this.maxResults = 0;
	}
	
	public SearchAround(Parcel source){
		super(source);
		this.center = new Point(source.readDouble(), source.readDouble());
	}
	
	public void setCenter(Point center) {
		this.center = center;
	}
	
	public Point getCenter() {
		return center;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeDouble(center.getLatitude());
		dest.writeDouble(center.getLongitude());
	}
}
