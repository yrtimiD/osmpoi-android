package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.domain.*;

import java.util.Comparator;

import android.location.Location;

public class DistanceComparator implements Comparator<Entity> {
	private Location loc;

	public DistanceComparator(Location loc) {
		this.loc = loc;
	}

	@Override
	public int compare(Entity lhs, Entity rhs) {
		
		Node ln = il.yrtimid.osm.osmpoi.Util.getFirstNode(lhs);
		Node rn = il.yrtimid.osm.osmpoi.Util.getFirstNode(rhs);
	
		if (ln != null && rn != null){
			Location ll = new Location(this.loc);
			ll.setLatitude(ln.getLatitude());
			ll.setLongitude(ln.getLongitude());

			Location rl = new Location(this.loc);
			rl.setLatitude(rn.getLatitude());
			rl.setLongitude(rn.getLongitude());

			return Float.compare(loc.distanceTo(ll), loc.distanceTo(rl));
		}
		else if (ln != null){
			return 1;
		}
		else if (rn != null){
			return -1;
		}
		else {
			return 0;
		}
	}

}
