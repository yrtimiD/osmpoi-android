/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.domain.Tag;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

/**
 * @author yrtimid
 * 
 */
public class ImportSettings {
	HashMap<EntityType, List<TagMatcher>> tagsInclude = new HashMap<EntityType, List<TagMatcher>>();// allowed tags by entity 
	HashMap<EntityType, List<TagMatcher>> tagsExclude = new HashMap<EntityType, List<TagMatcher>>();// forbidden tags by entity
	HashMap<EntityType, HashSet<TagMatcher>> addressTags = new HashMap<EntityType, HashSet<TagMatcher>>();// matchers collection to check if entity may be used for address search
	HashSet<String> excludedKeys = new HashSet<String>(); // these keys will not be imported
//	boolean onlyWithTags = false; // if entity have no tags - it will be excluded
	boolean isBuildGrid = true;
	boolean isClearBeforeImport = true;
	boolean importAddresses = false;
	int gridSize = 1;
	
	public ImportSettings() {
		excludedKeys.add("created_by");
		excludedKeys.add("source");
		
		tagsInclude.put(EntityType.Node, new ArrayList<TagMatcher>());
		tagsInclude.put(EntityType.Way, new ArrayList<TagMatcher>());
		tagsInclude.put(EntityType.Relation, new ArrayList<TagMatcher>());

		tagsExclude.put(EntityType.Node, new ArrayList<TagMatcher>());
		tagsExclude.put(EntityType.Way, new ArrayList<TagMatcher>());
		tagsExclude.put(EntityType.Relation, new ArrayList<TagMatcher>());

		
		addressTags.put(EntityType.Node, new HashSet<TagMatcher>());
		addressTags.put(EntityType.Way, new HashSet<TagMatcher>());
		addressTags.put(EntityType.Relation, new HashSet<TagMatcher>());
		
		addressTags.get(EntityType.Node).add(TagMatcher.parse("addr:*=*"));
		addressTags.get(EntityType.Node).add(TagMatcher.parse("place=*"));
		addressTags.get(EntityType.Way).add(TagMatcher.parse("addr:*=*"));
		addressTags.get(EntityType.Way).add(TagMatcher.parse("highway=*"));
		addressTags.get(EntityType.Way).add(TagMatcher.parse("place=*"));
		addressTags.get(EntityType.Relation).add(TagMatcher.parse("addr:*=*"));
		addressTags.get(EntityType.Relation).add(TagMatcher.parse("highway=*"));
		addressTags.get(EntityType.Relation).add(TagMatcher.parse("place=*"));
	}

	/**
	 * Creates settings instance with default configuration
	 * @return
	 */
	public static ImportSettings getDefault(){
		ImportSettings settings = new ImportSettings();
		
		settings.setBuildGrid(true);
		settings.setClearBeforeImport(true);
		
		settings.setKey(EntityType.Node, "name*", true);
		settings.setKey(EntityType.Node, "highway", false);
		settings.setKey(EntityType.Node, "building",false);
		settings.setKey(EntityType.Node, "barrier", false);
		settings.setKey(EntityType.Node, "*", true);
		
		settings.setKey(EntityType.Way, "name*", true);
		settings.setKey(EntityType.Way, "building",true);
		settings.setKey(EntityType.Way, "highway",false);
		settings.setKey(EntityType.Way, "*", false);
		
		
		//settings.setKey(EntityType.Relation, "name*", true);
		settings.setKeyValue(EntityType.Relation, "type","associatedStreet", false);
		settings.setKey(EntityType.Relation, "boundary",false); 
		settings.setKeyValue(EntityType.Relation, "type","bridge", true);
		settings.setKeyValue(EntityType.Relation, "type","destination_sign", false);
		settings.setKeyValue(EntityType.Relation, "type", "enforcement", false);
		settings.setKeyValue(EntityType.Relation, "type", "multipolygon", false);
		settings.setKeyValue(EntityType.Relation, "type", "public_transport", true);
		settings.setKeyValue(EntityType.Relation, "type", "relatedStreet", false);
		settings.setKeyValue(EntityType.Relation, "type", "restriction", false);
		
		settings.setKeyValue(EntityType.Relation, "type", "network", true);
		settings.setKeyValue(EntityType.Relation, "type", "operators", true);
		settings.setKeyValue(EntityType.Relation, "type", "health", true);
		
		settings.setKey(EntityType.Relation, "landuse",false); 
		settings.setKey(EntityType.Relation, "natural",false); 
		settings.setKey(EntityType.Relation, "leisure",false); 
		
		settings.setKey(EntityType.Relation, "area",false); 
		settings.setKey(EntityType.Relation, "waterway",false);
		settings.setKey(EntityType.Relation, "type", false);
		
		settings.setKey(EntityType.Relation, "*",false); 
 		 		
 		
 		settings.setImportAddresses(false);
 		
 		settings.setGridSize(1000);
 		
		return settings;
	}
	
	public boolean isImportNodes(){
		return !tagsInclude.get(EntityType.Node).isEmpty();
	}
	
	public boolean isImportWays(){
		return !tagsInclude.get(EntityType.Way).isEmpty();
	}
	
	public boolean isImportRelations(){
		return !tagsInclude.get(EntityType.Relation).isEmpty();
	}
	
	public void setBuildGrid(boolean isCreateGrid){
		this.isBuildGrid = isCreateGrid;
	}
	
	public boolean isBuildGrid(){
		return this.isBuildGrid;
	}
	
	public void setClearBeforeImport(boolean isClearBeforeImport) {
		this.isClearBeforeImport = isClearBeforeImport;
	}
	
	public boolean isClearBeforeImport() {
		return isClearBeforeImport;
	}
	
	public void reset(EntityType type){
		tagsInclude.get(type).clear();
		tagsExclude.get(type).clear();
	}
	
	public void setKey(EntityType type, String key, Boolean include){
		TagMatcher matcher = TagMatcher.parse(key+"=*");
		tagsInclude.get(type).remove(matcher);
		tagsExclude.get(type).remove(matcher);
		
		if (include){
			tagsInclude.get(type).add(matcher);
		}else{
			tagsExclude.get(type).add(matcher);
		}
	}

	public void setKeyValue(EntityType type, String key, String value, Boolean include){
		TagMatcher matcher = TagMatcher.parse(key+"="+value);
		if (include)
			tagsInclude.get(type).add(matcher);
		else
			tagsExclude.get(type).add(matcher);
	}
	
	public void setImportAddresses(boolean importAddresses) {
		this.importAddresses = importAddresses;
	}
	
	public int getGridSize() {
		return gridSize;
	}
	
	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
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
	
	public Boolean isPoi(Entity entity){
		Boolean include=false;
		Boolean exclude=false;

		for(TagMatcher matcher : tagsExclude.get(entity.getType())){
			Boolean match = matcher.isMatch(entity);
			if (match){
				exclude = true;
				break;
			}
		}
		if (exclude) 
			return false;
		
		
		for(TagMatcher matcher : tagsInclude.get(entity.getType())){
			Boolean match = matcher.isMatch(entity);
			if (match){
				include = true;
				break;
			}
		}
		if (include) 
			return true;
		
		
		return false;
	}
	
	public Boolean isAddress(Entity entity){
		if (importAddresses){
			for(TagMatcher check:addressTags.get(entity.getType())){
				if (check.isMatch(entity))
					return true;
			}
		}
		
		return false;
	}
}
