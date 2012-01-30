package il.yrtimid.osm.osmpoi;

public class CircleArea {
	Point point;
	int radius;
	
	public CircleArea(CircleArea area) {
		point = new Point(area.point);
		this.radius = area.radius;
	}
	
	public CircleArea(double latitude, double longitude, int radius) {
		point = new Point(latitude, longitude);
		this.radius = radius;
	}

	public Double getLatitude() {
		return point.getLatitude();
	}

	public Double getLongitude() {
		return point.getLongitude();
	}
	
	public Point getCenter(){
		return point;
	}

	public void setLocation(double latitude, double longitude){
		this.point.setLatitude(latitude);
		this.point.setLongitude(longitude);
	}
	
	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius){
		this.radius = radius;
	}
	
	public Boolean isInArea(double latitude, double longitude) {
		return point.getDistance(latitude, longitude) <= radius;
	}
	
	/*public Boolean isInArea(Node node){
		return isInArea(node.getLatitude(), node.getLongitude());
	}*/
	
	public int getDistance(double latitude, double longitude) {
		return point.getDistance(latitude, longitude);
	}
	
	/*public int getDistance(Node node) {
		return getDistance(node.getLatitude(), node.getLongitude());
	}*/
}
