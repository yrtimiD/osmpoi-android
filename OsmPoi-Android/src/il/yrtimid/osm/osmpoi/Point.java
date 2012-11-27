/**
 * 
 */
package il.yrtimid.osm.osmpoi;

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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Lat:%f Lon:%f", latitude, longitude);
	}

	/**
	 * Calculates distance between current point and specified point in meters
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public int getDistance(double latitude, double longitude) {
		return Util.getDistance(this.latitude, this.longitude, latitude, longitude);
	}

	/**
	 * Calculates distance between current point and specified point in meters
	 * @param anotherPoint
	 * @return
	 */
	public int getDistance(Point anotherPoint) {
		return Util.getDistance(this.latitude, this.longitude, anotherPoint.latitude, anotherPoint.longitude);
	}
}
