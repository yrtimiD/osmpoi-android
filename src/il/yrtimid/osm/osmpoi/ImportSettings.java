/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.domain.Tag;

/**
 * @author yrtimid
 * 
 */
public class ImportSettings {
	HashMap<EntityType, HashMap<String, Boolean>> tags = new HashMap<EntityType, HashMap<String,Boolean>>();
	
	HashSet<String> excludedKeys = new HashSet<String>(); // these keys will not be imported
//	boolean onlyWithTags = false; // if entity have no tags - it will be excluded
	boolean isBuildGrid = true;
	boolean isClearBeforeImport = true;
	
	public ImportSettings() {
		excludedKeys.add("created_by");
		excludedKeys.add("source");
		tags.put(EntityType.Bound, new HashMap<String, Boolean>());
		tags.put(EntityType.Node, new HashMap<String, Boolean>());
		tags.put(EntityType.Way, new HashMap<String, Boolean>());
		tags.put(EntityType.Relation, new HashMap<String, Boolean>());
	}

	public boolean isImportNodes(){
		return tags.get(EntityType.Node).containsValue(true);
	}
	
	public boolean isImportWays(){
		return tags.get(EntityType.Way).containsValue(true);
	}
	
	public boolean isImportRelations(){
		return tags.get(EntityType.Relation).containsValue(true);
	}
	
	public void setBuildGrid(boolean isCreateGrid){
		this.isBuildGrid = isCreateGrid;
	}
	
	public boolean isBuildGrid(){
		return this.isBuildGrid;
	}
	
	/**
	 * @param isClearBeforeImport the isClearBeforeImport to set
	 */
	public void setClearBeforeImport(boolean isClearBeforeImport) {
		this.isClearBeforeImport = isClearBeforeImport;
	}
	
	/**
	 * @return the isClearBeforeImport
	 */
	public boolean isClearBeforeImport() {
		return isClearBeforeImport;
	}
	
	public void setNodeKey(String key, Boolean include){
		tags.get(EntityType.Node).put(key, include);
	}
	
	public void setWayKey(String key, Boolean include){
		tags.get(EntityType.Way).put(key, include);
	}

	public void setRelationKey(String key, Boolean include){
		tags.get(EntityType.Relation).put(key, include);
	}

	public void cleanTags(Entity entity){
		Collection<Tag> tagsToRemove = new ArrayList<Tag>();
			
		for (Tag t : entity.getTags()) {
			if (excludedKeys.contains(t.getKey())) {
				tagsToRemove.add(t);
			}
		}
		
		for(Tag t:tagsToRemove){
			entity.getTags().remove(t);
		}
	}
	
	public Boolean isEntityValid(Entity entity) {

		for (Tag t : entity.getTags()) {
			Boolean include = tags.get(entity.getType()).get(t.getKey());
			if (include != null){
				return include;
			}
		}
		
		if (entity.getTags().size() == 0)
			return false;

		return (tags.get(entity.getType()).get("*") == true);
	}
}
