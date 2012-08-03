package il.yrtimid.osm.osmpoi.searchparameters;

import il.yrtimid.osm.osmpoi.Point;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Search around point and filtering by key-value
 * @author yrtimid
 *
 */
public class SearchByKeyValue extends SearchAround implements android.os.Parcelable{
	protected Point center;
	protected String expression;
	protected TagMatcher matcher = null;
	
	public static final Parcelable.Creator<SearchByKeyValue> CREATOR = new Parcelable.Creator<SearchByKeyValue>() {

		@Override
		public SearchByKeyValue createFromParcel(Parcel source) {
			return new SearchByKeyValue(source);
		}

		@Override
		public SearchByKeyValue[] newArray(int size) {
			return new SearchByKeyValue[size];
		}
	};
	
	public SearchByKeyValue(String expression){
		this.center = new Point(0,0);
		this.maxResults = 0;
		this.expression = expression;
	}
	
	public SearchByKeyValue(Parcel source){
		super(source);
		expression = source.readString();
		this.matcher = null;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
		this.matcher = null;
	}
	
	public String getExpression(){
		return this.expression;
	}
	
	public boolean hasExpression(){
		return this.expression != null && this.expression.length()>0;
	}
	
	public TagMatcher getMatcher() {
		if (matcher == null){
			matcher = TagMatcher.parse(expression);
		}
		return matcher;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(expression);
	}
}
