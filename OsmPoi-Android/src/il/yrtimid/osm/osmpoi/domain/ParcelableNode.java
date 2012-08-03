// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableNode extends ParcelableEntity implements Parcelable {

	protected Node node;

	public ParcelableNode(Node node) {
		super(node);
		this.node = node;
	}

	/**
	 * @param source
	 */
	public ParcelableNode(Parcel source) {
		this(new Node());
		super.readFromParcel(this.node, source);
		node.setLatitude(source.readDouble());
		node.setLongitude(source.readDouble());
	}

	/**
	 * @return the node
	 */
	public Node getNode() {
		return node;
	}

	public static final Parcelable.Creator<ParcelableNode> CREATOR = new Parcelable.Creator<ParcelableNode>() {

		@Override
		public ParcelableNode createFromParcel(Parcel source) {
			return new ParcelableNode(source);
		}

		@Override
		public ParcelableNode[] newArray(int size) {
			return new ParcelableNode[size];
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
		dest.writeDouble(node.getLatitude());
		dest.writeDouble(node.getLongitude());
	}
}
