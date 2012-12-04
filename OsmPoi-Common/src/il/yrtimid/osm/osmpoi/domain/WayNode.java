package il.yrtimid.osm.osmpoi.domain;

public class WayNode extends Entity {

	protected WayNode(){
		this(new CommonEntityData());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 */
	public WayNode(CommonEntityData entityData) {
		super(entityData);
	}

	public WayNode(long nodeId) {
		super(new CommonEntityData(nodeId));
	}
	

	@Override
	public EntityType getType() {
		return EntityType.NodeRef;
	}
}
