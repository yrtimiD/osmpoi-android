/**
 * 
 */
package il.yrtimid.osm.osmpoi.domain;

/**
 * @author yrtimid
 *
 */
public class GridCell {
	int id;
	double minLat, minLon, maxLat, maxLon;
	
	/**
	 * @param id
	 * @param minLat
	 * @param minLon
	 * @param maxLat
	 * @param maxLon
	 */
	public GridCell(int id, double minLat, double minLon, double maxLat, double maxLon) {
		super();
		this.id = id;
		this.minLat = minLat;
		this.minLon = minLon;
		this.maxLat = maxLat;
		this.maxLon = maxLon;
	}
	
	/**
	 * 
	 * @param 0-based vertex index [0..3]
	 * @return
	 * @throws Exception 
	 */
	public Point getVertex(int index) throws Exception{
		switch(index){
		case 0:
			return new Point(minLat, minLon);
		case 1:
			return new Point(minLat, maxLon);
		case 2:
			return new Point(maxLat, maxLon);
		case 3:
			return new Point(maxLat, minLon);
		default:
			throw new Exception("Only index from 0 to 3 inclusive is supported");
		}
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
