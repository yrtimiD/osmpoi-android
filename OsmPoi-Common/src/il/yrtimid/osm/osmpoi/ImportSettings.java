/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.domain.Tag;
import il.yrtimid.osm.osmpoi.tagmatchers.KeyValueMatcher;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

/**
 * @author yrtimid
 * 
 */
public class ImportSettings {
	HashMap<EntityType, List<KeyValueMatcher>> tagsInclude = new HashMap<EntityType, List<KeyValueMatcher>>();// allowed tags by entity 
	HashMap<EntityType, List<KeyValueMatcher>> tagsExclude = new HashMap<EntityType, List<KeyValueMatcher>>();// forbidden tags by entity
	HashMap<EntityType, HashSet<KeyValueMatcher>> addressTags = new HashMap<EntityType, HashSet<KeyValueMatcher>>();// matchers collection to check if entity may be used for address search
	HashSet<String> excludedKeys = new HashSet<String>(); // these keys will not be imported
//	boolean onlyWithTags = false; // if entity have no tags - it will be excluded
	Boolean isBuildGrid = true;
	Boolean isClearBeforeImport = true;
	Boolean importAddresses = false;
	Integer gridSize = Integer.MAX_VALUE;
	
	public ImportSettings() {
		excludedKeys.add("created_by");
		excludedKeys.add("source");
		
		tagsInclude.put(EntityType.Node, new ArrayList<KeyValueMatcher>());
		tagsInclude.put(EntityType.Way, new ArrayList<KeyValueMatcher>());
		tagsInclude.put(EntityType.Relation, new ArrayList<KeyValueMatcher>());

		tagsExclude.put(EntityType.Node, new ArrayList<KeyValueMatcher>());
		tagsExclude.put(EntityType.Way, new ArrayList<KeyValueMatcher>());
		tagsExclude.put(EntityType.Relation, new ArrayList<KeyValueMatcher>());

		
		addressTags.put(EntityType.Node, new HashSet<KeyValueMatcher>());
		addressTags.put(EntityType.Way, new HashSet<KeyValueMatcher>());
		addressTags.put(EntityType.Relation, new HashSet<KeyValueMatcher>());
		
		addressTags.get(EntityType.Node).add(new KeyValueMatcher("addr:*","*"));
		addressTags.get(EntityType.Node).add(new KeyValueMatcher("place","*"));
		addressTags.get(EntityType.Way).add(new KeyValueMatcher("addr:*","*"));
		addressTags.get(EntityType.Way).add(new KeyValueMatcher("highway","*"));
		addressTags.get(EntityType.Way).add(new KeyValueMatcher("place","*"));
		addressTags.get(EntityType.Relation).add(new KeyValueMatcher("addr:*","*"));
		addressTags.get(EntityType.Relation).add(new KeyValueMatcher("highway","*"));
		addressTags.get(EntityType.Relation).add(new KeyValueMatcher("place","*"));
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
		settings.setKey(EntityType.Node, "*", false);
		
		settings.setKey(EntityType.Way, "name*", true);
		settings.setKey(EntityType.Way, "building",true);
		settings.setKey(EntityType.Way, "highway",false);
		settings.setKey(EntityType.Way, "*", false);
		
		
		//settings.setKey(EntityType.Relation, "name*", true);
		settings.setKeyValue(EntityType.Relation, "type","associatedStreet", false);
		settings.setKey(EntityType.Relation, "boundary",false); 
		settings.setKeyValue(EntityType.Relation, "type", "bridge", true);
		settings.setKeyValue(EntityType.Relation, "type", "destination_sign", false);
		settings.setKeyValue(EntityType.Relation, "type", "enforcement", false);
		settings.setKeyValue(EntityType.Relation, "type", "multipolygon", false);
		settings.setKeyValue(EntityType.Relation, "type", "public_transport", true);
		settings.setKeyValue(EntityType.Relation, "type", "relatedStreet", false);
		settings.setKeyValue(EntityType.Relation, "type", "associatedStreet", false);
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
 		
 		settings.setGridSize(1000000);
 		
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
	
	public Boolean isImportAddresses() {
		return importAddresses;
	}
	
	public void reset(EntityType type){
		tagsInclude.get(type).clear();
		tagsExclude.get(type).clear();
	}
	
	public void setKey(EntityType type, String key, Boolean include){
		KeyValueMatcher matcher = new KeyValueMatcher(key,"*");
		tagsInclude.get(type).remove(matcher);
		tagsExclude.get(type).remove(matcher);
		
		if (include){
			tagsInclude.get(type).add(matcher);
		}else{
			tagsExclude.get(type).add(matcher);
		}
	}

	public void setKeyValue(EntityType type, String key, String value, Boolean include){
		KeyValueMatcher matcher = new KeyValueMatcher(key, value);
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
		for(TagMatcher check:addressTags.get(entity.getType())){
			if (check.isMatch(entity))
				return true;
		}
		
		return false;
	}

	public void writeToProperties(Properties props){
		props.setProperty("import.isBuildGrid", isBuildGrid.toString());
		props.setProperty("import.gridSize", gridSize.toString());
		props.setProperty("import.importAddresses", importAddresses.toString());
		
		for(EntityType entityType : tagsInclude.keySet()){
			List<KeyValueMatcher> tags = tagsInclude.get(entityType);
			for(KeyValueMatcher tm : tags){
				props.setProperty("import.include."+entityType+"."+tm.getKey(), tm.getValue());
			}
		}
		
		for(EntityType entityType : tagsExclude.keySet()){
			List<KeyValueMatcher> tags = tagsExclude.get(entityType);
			for(KeyValueMatcher tm : tags){
				props.setProperty("import.exclude."+entityType+"."+tm.getKey(), tm.getValue());
			}
		}
	}
	
	public static ImportSettings createFromProperties(Properties props){
		ImportSettings settings = new ImportSettings();
		
		settings.setBuildGrid(Boolean.parseBoolean(props.getProperty("import.isBuildGrid", "true")));
		settings.setGridSize(Integer.parseInt(props.getProperty("import.gridSize", new Integer(Integer.MAX_VALUE).toString())));
		settings.setImportAddresses(Boolean.parseBoolean(props.getProperty("import.importAddresses", "false")));
		
		for(Object key : props.keySet()){
			String[] tokens = key.toString().split("\\.");
			if (tokens.length != 4 || !tokens[0].equals("import")) 
				continue;
			
			EntityType entityType = EntityType.valueOf(tokens[2]);
			String k = tokens[3];
			
			if (tokens[1].equals("include")){
				settings.setKeyValue(entityType, k, props.getProperty(key.toString()), true);
			}else if (tokens[1].equals("exclude")){
				settings.setKeyValue(entityType, k, props.getProperty(key.toString()), false);
			}
		}
		
		return settings;
	}
}
