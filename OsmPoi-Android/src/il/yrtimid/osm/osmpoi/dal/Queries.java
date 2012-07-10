/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

/**
 * @author yrtimid
 *
 */
public class Queries {

	public static final String sql_create_node_table="CREATE TABLE IF NOT EXISTS nodes ("
			+"		id INTEGER NOT NULL PRIMARY KEY,"
			+"		timestamp TEXT,"
			+"		lon REAL NOT NULL,"
			+"		lat REAL NOT NULL,"
			+"		grid_id INTEGER NOT NULL DEFAULT (0)"
			+")";
	
		
	public static final String sql_create_node_tags_table = "CREATE TABLE IF NOT EXISTS node_tags ("
			+"	    node_id INTEGER NOT NULL,"
			+"	    k TEXT,"
			+"	    v TEXT"
			+")";
	    
    public static final String sql_node_tags_idx = "CREATE INDEX IF NOT EXISTS node_tags_node_id_idx ON node_tags(node_id)";
    
    public static final String sql_create_ways_table = "CREATE TABLE IF NOT EXISTS ways ("
	    	+"	        id INTEGER NOT NULL PRIMARY KEY,"
	    	+"	        timestamp TEXT"
	    	+")";

    public static final String sql_create_way_tags_table = "CREATE TABLE IF NOT EXISTS way_tags ("
    		+"	way_id INTEGER NOT NULL,"
    		+"	k TEXT,"
    		+"	v TEXT"
    		+")";
    
	public static final String sql_way_tags_idx = "CREATE INDEX IF NOT EXISTS way_tags_way_id_idx ON way_tags(way_id)";
	
	public static final String sql_create_way_nodes_table = "CREATE TABLE IF NOT EXISTS way_nodes ("
			+"	        way_id INTEGER NOT NULL,"
			+"	        node_id INTEGER NOT NULL"
			+")";
	    
	public static final String sql_way_nodes_way_idx = "CREATE INDEX IF NOT EXISTS way_nodes_way_id_idx ON way_nodes(way_id ASC)";
	
	public static final String sql_way_nodes_node_idx = "CREATE INDEX IF NOT EXISTS way_nodes_node_id_idx ON way_nodes(node_id ASC)";
	
	public static final String sql_create_relations_table = "CREATE TABLE IF NOT EXISTS relations ("
		    +"	id INTEGER NOT NULL PRIMARY KEY,"
		    +"	timestamp TEXT"
		    +")";
	    
	public static final String sql_create_relation_tags_table = "CREATE TABLE IF NOT EXISTS relation_tags ("
	        +"	relation_id INTEGER NOT NULL,"
	        +"	k TEXT,"
	        +"	v TEXT"
	        +")";
	    
	    public static final String sql_relation_tags_idx = "CREATE INDEX IF NOT EXISTS relation_tags_rel_id_idx ON relation_tags(relation_id)";
	    
	    public static final String sql_create_members_table = "CREATE TABLE IF NOT EXISTS members ("
	        +"	relation_id INTEGER NOT NULL,"
	        +"	type TEXT,"
	        +"	ref INTEGER NOT NULL,"
	        +"	role TEXT"
	        +")";
	    
	    public static final String sql_relation_members_idx = "CREATE INDEX IF NOT EXISTS relation_members_rel_id_idx ON members(relation_id)";
	    
	    public static final String sql_create_grid_table = "CREATE TABLE IF NOT EXISTS grid ("
    		+"	id INTEGER PRIMARY KEY AUTOINCREMENT,"
    		+"	minLat REAL NOT NULL,"
    		+"	minLon REAL NOT NULL,"
    		+"	maxLat REAL NOT NULL,"
    		+"	maxLon REAL NOT NULL"
    		+")";
	    
	    public static final String sql_create_inline_queries_table = "CREATE TABLE IF NOT EXISTS inline_queries (id INTEGER PRIMARY KEY AUTOINCREMENT, query TEXT NOT NULL, [select] TEXT NOT NULL)";
	    
	    public static final String sql_create_inline_results_table = "CREATE TABLE IF NOT EXISTS inline_results (query_id INTEGER NOT NULL, value TEXT)";
	    
	    public static final String sql_create_starred_table = "CREATE TABLE IF NOT EXISTS starred (type TEXT NOT NULL, id INTEGER NOT NULL, title TEXT NOT NULL)";

}
