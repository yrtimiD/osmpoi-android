//package il.yrtimid.osm.osmpoi.tagmatchers;
//
//import java.util.Collection;
//
//import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
//import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
//
//public class SearchResult {
//	private Entity entity;
//	private int distance;
//
//	public SearchResult(Entity entity, int distance) {
//		this.entity = entity;
//		this.distance = distance;
//	}
//
//	public Entity getEntity() {
//		return entity;
//	}
//
//	public int getDistance() {
//		return distance;
//	}
//
//	public String getTitle() {
//		Collection<Tag> tags = entity.getTags();
//		String title = null;
//		for (Tag t : tags) {
//			if (t.getKey().equals("name")) {
//				title = t.getValue();
//				break;
//			}
//		}
//		if (title == null) {
//			title = "[noname]";
//		}
//
//		return title;
//	}
//
//	public String toString() {
//		return getDistance() + " " + getTitle();
//	}
//}
