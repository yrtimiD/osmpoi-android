package il.yrtimid.osm.osmpoi.searchparameters;

import android.os.Parcel;

public abstract class BaseSearchParameter implements android.os.Parcelable{
	protected Integer maxResults;
	
	public BaseSearchParameter(){
		this.maxResults = 0;
	}
	
	public BaseSearchParameter(Parcel source){
		maxResults = source.readInt();
	}
	
	/**
	 * Limiting results count. Negative or zero values equal to no limit.
	 * @return
	 */
	public Integer getMaxResults() {
		return maxResults;
	}
	
	/**
	 * Limiting results count. Negative or zero values equal to no limit.
	 * @param maxResults
	 */
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(maxResults);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
