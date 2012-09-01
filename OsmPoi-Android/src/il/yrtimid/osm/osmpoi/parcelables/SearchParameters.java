package il.yrtimid.osm.osmpoi.parcelables;

import il.yrtimid.osm.osmpoi.Point;
import android.os.Parcel;
import android.os.Parcelable;

public class SearchParameters implements android.os.Parcelable{
	private Point center;
	private String expression;
	private int maxResults;
	
	public static final Parcelable.Creator<SearchParameters> CREATOR = new Parcelable.Creator<SearchParameters>() {

		@Override
		public SearchParameters createFromParcel(Parcel source) {
			return new SearchParameters(source);
		}

		@Override
		public SearchParameters[] newArray(int size) {
			return new SearchParameters[size];
		}
	};
	
	public SearchParameters(){
		this.center = new Point(0,0);
		this.maxResults = 0;
	}
	
	public SearchParameters(Parcel source){
		expression = source.readString();
		this.center = new Point(source.readDouble(), source.readDouble());
		maxResults = source.readInt();
	}
	
	public void setCenter(Point center) {
		this.center = center;
	}
	
	public Point getCenter() {
		return center;
	}
	
	public int getMaxResults() {
		return maxResults;
	}
	
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	public String getExpression(){
		return this.expression;
	}
	
	public boolean hasExpression(){
		return this.expression != null && this.expression.length()>0;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(expression);
		dest.writeDouble(center.getLatitude());
		dest.writeDouble(center.getLongitude());
		dest.writeInt(maxResults);
	}
}
