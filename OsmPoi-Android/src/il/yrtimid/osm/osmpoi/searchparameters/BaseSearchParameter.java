package il.yrtimid.osm.osmpoi.searchparameters;

import android.os.Parcel;

public class BaseSearchParameter{
	protected int maxResults;
	
	public BaseSearchParameter(){
		this.maxResults = 0;
	}
	
	public BaseSearchParameter(Parcel source){
		maxResults = source.readInt();
	}
	
	public int getMaxResults() {
		return maxResults;
	}
	
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(maxResults);
	}
}
