package il.yrtimid.osm.osmpoi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import il.yrtimid.osm.osmpoi.CircleArea;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Relation;
import il.yrtimid.osm.osmpoi.domain.RelationMember;
import il.yrtimid.osm.osmpoi.domain.Way;

import android.location.Location;

public class Util {

	static final float RADIUS = 6372795;// радиус сферы (Земли)

	/**
	 * Calculates distance between two points in meters
	 */
	public static int getDistance(double lat1, double long1, double lat2, double long2) {
		// copied from http://gis-lab.info/qa/great-circles.html

		// в радианах
		double rlat1 = lat1 * Math.PI / 180.0;
		double rlat2 = lat2 * Math.PI / 180.0;
		double rlong1 = long1 * Math.PI / 180.0;
		double rlong2 = long2 * Math.PI / 180.0;

		// косинусы и синусы широт и разницы долгот
		double cl1 = Math.cos(rlat1);
		double cl2 = Math.cos(rlat2);
		double sl1 = Math.sin(rlat1);
		double sl2 = Math.sin(rlat2);
		double delta = rlong2 - rlong1;
		double cdelta = Math.cos(delta);
		double sdelta = Math.sin(delta);

		// вычисления длины большого круга
		double y = Math.sqrt(Math.pow(cl2 * sdelta, 2) + Math.pow(cl1 * sl2 - sl1 * cl2 * cdelta, 2));
		double x = sl1 * sl2 + cl1 * cl2 * cdelta;
		double ad = Math.atan2(y, x);
		int dist = (int) Math.round(ad * RADIUS);

		/*
		 * //вычисление начального азимута double x = (cl1*sl2) -
		 * (sl1*cl2*cdelta); double y = sdelta*cl2; double z =
		 * Math.degrees(Math.atan(-y/x));
		 * 
		 * if (x < 0) z = z+180.0;
		 * 
		 * double z2 = (z+180.) % 360. - 180.0; double z2 = - Math.radians(z2);
		 * double anglerad2 = z2 - ((2*Math.PI)*Math.floor((z2/(2*Math.PI))) );
		 * double angledeg = (anglerad2*180.)/Math.PI; //Initial bearing in
		 * degrees
		 */

		return dist;
	}

	/**
	 * Calculates bearing between two points in degrees
	 */
	public static int getBearing(double lat1, double long1, double lat2, double long2) {
		// copied from http://gis-lab.info/qa/great-circles.html

		// в радианах
		double rlat1 = lat1 * Math.PI / 180.0;
		double rlat2 = lat2 * Math.PI / 180.0;
		double rlong1 = long1 * Math.PI / 180.0;
		double rlong2 = long2 * Math.PI / 180.0;

		// косинусы и синусы широт и разницы долгот
		double cl1 = Math.cos(rlat1);
		double cl2 = Math.cos(rlat2);
		double sl1 = Math.sin(rlat1);
		double sl2 = Math.sin(rlat2);
		double delta = rlong2 - rlong1;
		double cdelta = Math.cos(delta);
		double sdelta = Math.sin(delta);

		// вычисление начального азимута
		double x = (cl1 * sl2) - (sl1 * cl2 * cdelta);
		double y = sdelta * cl2;
		double z = Math.toDegrees(Math.atan(-y / x));

		if (x < 0)
			z = z + 180.0;

		double z2 = (z + 180.) % 360. - 180.0;
		z2 = -Math.toRadians(z2);
		double anglerad2 = z2 - ((2 * Math.PI) * Math.floor((z2 / (2 * Math.PI))));
		double angledeg = (anglerad2 * 180.) / Math.PI; // Initial bearing in
														// degrees

		return (int) angledeg;
	}

	public static String getPrefixedName(String fileName, String prefix) {
		String result;

		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex != -1) {
			if (dotIndex < fileName.length() - 1) {
				result = fileName.substring(0, dotIndex) + "." + prefix + "." + fileName.substring(dotIndex + 1, fileName.length());
			} else {
				result = fileName + prefix;
			}
		} else {
			result = fileName + "." + prefix;
		}

		return result;
	}

	public static int getNodeDistance(Node node, CircleArea area) {
		return getDistance(node.getLatitude(), node.getLongitude(), area.getLatitude(), area.getLongitude());
	}

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	public static boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	public static String join(String delim, Object... data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			sb.append(data[i]);
			if (i >= data.length - 1) {
				break;
			}
			sb.append(delim);
		}
		return sb.toString();
	}

	public static Node getFirstNode(Entity entity){
		Node node = null;
		switch (entity.getType()){
		case Node:
			node = (Node) entity;
			break;
		case Way:
			List<Node> wayNodes = ((Way)entity).getNodes();
			if (wayNodes.size()>0) node = wayNodes.get(0);
			break;
		case Relation:
			//TODO: Implement returning nearest node from any relation level
			List<RelationMember> members = ((Relation)entity).getMembers();
			for(RelationMember rm : members){
				if (rm.getMemberType() == EntityType.Node){
					node = (Node)rm.getMember();
					break;
				}
			}
			break;
		default:
			break;
		}
		return node;
	}

	public static String readText(InputStream is, String charset) throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    byte[] bytes = new byte[4096];
	    for(int len;(len = is.read(bytes))>0;)
	        baos.write(bytes, 0, len);
	    return new String(baos.toByteArray(), charset);
	}
}
