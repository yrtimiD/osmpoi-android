package il.yrtimid.osm.osmpoi.xml;

import java.util.ArrayList;
import java.util.List;

import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.osmosis.core.task.v0_6.Sink;

public class EntityListSink implements Sink {

	List<Entity> entities = new ArrayList<Entity>();
	
	@Override
	public void complete() {
	}

	@Override
	public void release() {
	}

	@Override
	public void process(Entity entity) {
		entities.add(entity);
	}

	public List<Entity> getEntities() {
		return entities;
	}
}
