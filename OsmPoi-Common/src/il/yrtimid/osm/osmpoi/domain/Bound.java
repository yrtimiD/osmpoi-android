// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.domain;

import java.util.Date;


/**
 * A data class representing an OSM data bound element.
 * 
 * @author Karl Newman
 */
public class Bound extends Entity {
	
	private static final double MIN_LATITUDE = -90.0;
	private static final double MAX_LATITUDE = 90.0;
	private static final double MIN_LONGITUDE = -180.0;
	private static final double MAX_LONGITUDE = 180.0;
	
	private double right;
	private double left;
	private double top;
	private double bottom;
	private String origin;
	
	
	/**
	 * Creates a new instance which covers the entire planet.
	 * 
	 * @param origin
	 *            The origin (source) of the data, typically a URI
	 * 
	 */
	public Bound(String origin) {
		this(MAX_LONGITUDE, MIN_LONGITUDE, MAX_LATITUDE, MIN_LATITUDE, origin);
	}


	/**
	 * Creates a new instance with the specified boundaries.
	 * 
	 * @param right
	 *            The longitude coordinate of the right (East) edge of the bound
	 * @param left
	 *            The longitude coordinate of the left (West) edge of the bound
	 * @param top
	 *            The latitude coordinate of the top (North) edge of the bound
	 * @param bottom
	 *            The latitude coordinate of the bottom (South) edge of the bound
	 * @param origin
	 *            The origin (source) of the data, typically a URI
	 */
	public Bound(double right, double left, double top, double bottom, String origin) {
		super(new CommonEntityData(0, new Date())); // minimal underlying entity
		
		// Check if any coordinates are out of bounds
		if (Double.compare(right, MAX_LONGITUDE + 1.0d) > 0
		        || Double.compare(right, MIN_LONGITUDE - 1.0d) < 0
		        || Double.compare(left, MAX_LONGITUDE + 1.0d) > 0
		        || Double.compare(left, MIN_LONGITUDE - 1.0d) < 0
		        || Double.compare(top, MAX_LATITUDE + 1.0d) > 0
		        || Double.compare(top, MIN_LATITUDE - 1.0d) < 0
		        || Double.compare(bottom, MAX_LATITUDE + 1.0d) > 0
		        || Double.compare(bottom, MIN_LATITUDE - 1.0d) < 0) {
			throw new IllegalArgumentException("Bound coordinates outside of valid range");
		}
		if (Double.compare(top, bottom) < 0) {
			throw new IllegalArgumentException("Bound top < bottom");
		}
		if (origin == null) {
			throw new IllegalArgumentException("Bound origin is null");
		}
		this.right = right;
		this.left = left;
		this.top = top;
		this.bottom = bottom;
		this.origin = origin;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Bound;
	}


	/**
	 * @return The right (East) bound longitude
	 */
	public double getRight() {
		return right;
	}


	/**
	 * @return The left (West) bound longitude
	 */
	public double getLeft() {
		return left;
	}


	/**
	 * @return The top (North) bound latitude
	 */
	public double getTop() {
		return top;
	}


	/**
	 * @return The bottom (South) bound latitude
	 */
	public double getBottom() {
		return bottom;
	}


	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		/*
		 * As per the hashCode definition, this doesn't have to be unique it
		 * just has to return the same value for any two objects that compare
		 * equal. Using both id and version will provide a good distribution of
		 * values but is simple to calculate.
		 */
		return (int) getId();
	}

    /** 
     * ${@inheritDoc}.
     */
    @Override
    public String toString() {
        return "Bound(top=" + getTop() + ", bottom=" + getBottom() + ", left=" + getLeft() + ", right=" + getRight()
				+ ")";
    }

}
