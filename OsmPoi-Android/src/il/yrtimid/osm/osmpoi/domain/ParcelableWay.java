// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A data class representing a single OSM way.
 * 
 * @author Brett Henderson
 */
public class ParcelableWay extends ParcelableEntity implements Parcelable {

	protected Way way;

	/**
 * 
 */
	public ParcelableWay(Way way) {
		super(way);
		this.way = way;
	}

	/**
	 * @param source
	 */
	public ParcelableWay(Parcel source) {
		this(new Way());
		super.readFromParcel(way, source);
		List<ParcelableNode> pWayNodes = new ArrayList<ParcelableNode>();
		source.readTypedList(pWayNodes, ParcelableNode.CREATOR);
		List<Node> wayNodes = way.getNodes();
		for(ParcelableNode pn : pWayNodes){
			wayNodes.add(pn.getNode());
		}
	}

	public static final Parcelable.Creator<ParcelableWay> CREATOR = new Parcelable.Creator<ParcelableWay>() {

		@Override
		public ParcelableWay createFromParcel(Parcel source) {
			return new ParcelableWay(source);
		}

		@Override
		public ParcelableWay[] newArray(int size) {
			return new ParcelableWay[size];
		}

	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		
		List<ParcelableNode> pWayNodes = new ArrayList<ParcelableNode>();
		List<Node> wayNodes = way.getNodes();
		for(Node n : wayNodes){
			pWayNodes.add(new ParcelableNode(n));
		}
		
		dest.writeTypedList(pWayNodes);
	}
}
