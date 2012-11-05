package il.yrtimid.osm.osmpoi.domain;

/**
 * @author yrtimid
 *
 */
public class Point {
	Double latitude;
	Double longitude;
	int radius;
	
	public Point(Point point) {
		this.latitude = point.latitude;
		this.longitude = point.longitude;
	}
	
	public Point(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}
	
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
}
