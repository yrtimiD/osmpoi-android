package il.yrtimid.osm.osmpoi.pbf;

import il.yrtimid.osm.osmpoi.ItemPipe;
import il.yrtimid.osm.osmpoi.domain.*;


public class EntityHandler implements Sink {

	ItemPipe<Entity> itemPipe;
	int maximumEntries = Integer.MAX_VALUE;

	public EntityHandler(ItemPipe<Entity> itemPipe) {
		this.itemPipe = itemPipe;
	}

	public void setMaximumEntries(int max) {
		this.maximumEntries = max;
	}

	@Override
	public void complete() {
		// TODO Auto-generated method stub
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
	}

	@Override
	public void process(Entity entity) {
		if (maximumEntries > 0) {
			this.itemPipe.pushItem(entity);
			maximumEntries--;
		}
	}
}
