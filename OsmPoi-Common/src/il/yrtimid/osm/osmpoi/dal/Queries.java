/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

/**
 * @author yrtimid
 *
 */
public class Queries {
	
	public static final String BOUNDS_TABLE = "bounds";

	public static final String NODES_TABLE = "nodes";
	public static final String NODES_TAGS_TABLE = "node_tags";

	public static final String WAYS_TABLE = "ways";
	public static final String WAY_TAGS_TABLE = "way_tags";
	public static final String WAY_NODS_TABLE = "way_nodes";

	public static final String RELATIONS_TABLE = "relations";
	public static final String RELATION_TAGS_TABLE = "relation_tags";
	public static final String MEMBERS_TABLE = "members";

	public static final String GRID_TABLE = "grid";
	
	public static final String INLINE_QUERIES_TABLE = "inline_queries";
	public static final String INLINE_RESULTS_TABLE = "inline_results";
	
	public static final String STARRED_TABLE = "starred";
	
	public static final String SQL_CREATE_BOUNDS_TABLE = "CREATE TABLE IF NOT EXISTS bounds ("+
			"top REAL NOT NULL," +
			"bottom REAL NOT NULL," +
			"left REAL NOT NULL," +
			"right REAL NOT NULL" +
			")";
	
	public static final String SQL_CREATE_NODE_TABLE = "CREATE TABLE IF NOT EXISTS nodes ("
			+"		id INTEGER NOT NULL PRIMARY KEY,"
			+"		timestamp TEXT,"
			+"		lon REAL NOT NULL,"
			+"		lat REAL NOT NULL,"
			+"		grid_id INTEGER NOT NULL DEFAULT (0)"
			+")";
	
		
	public static final String SQL_CREATE_NODE_TAGS_TABLE = "CREATE TABLE IF NOT EXISTS node_tags ("
			+"	    node_id INTEGER NOT NULL,"
			+"	    k TEXT,"
			+"	    v TEXT"
			+")";
	    
    public static final String SQL_NODE_TAGS_IDX = "CREATE INDEX IF NOT EXISTS node_tags_node_id_idx ON node_tags(node_id)";
    
    public static final String SQL_CREATE_WAYS_TABLE = "CREATE TABLE IF NOT EXISTS ways ("
	    	+"	        id INTEGER NOT NULL PRIMARY KEY,"
	    	+"	        timestamp TEXT"
	    	+")";

    public static final String SQL_CREATE_WAY_TAGS_TABLE = "CREATE TABLE IF NOT EXISTS way_tags ("
    		+"	way_id INTEGER NOT NULL,"
    		+"	k TEXT,"
    		+"	v TEXT"
    		+")";
    
	public static final String SQL_WAY_TAGS_IDX = "CREATE INDEX IF NOT EXISTS way_tags_way_id_idx ON way_tags(way_id)";
	
	public static final String SQL_CREATE_WAY_NODES_TABLE = "CREATE TABLE IF NOT EXISTS way_nodes ("
			+"	        way_id INTEGER NOT NULL,"
			+"	        node_id INTEGER NOT NULL"
			+")";
	    
	public static final String SQL_WAY_NODES_WAY_IDX = "CREATE INDEX IF NOT EXISTS way_nodes_way_id_idx ON way_nodes(way_id ASC)";
	
	public static final String SQL_WAY_NODES_NODE_IDX = "CREATE INDEX IF NOT EXISTS way_nodes_node_id_idx ON way_nodes(node_id ASC)";
	
	public static final String SQL_CREATE_RELATIONS_TABLE = "CREATE TABLE IF NOT EXISTS relations ("
		    +"	id INTEGER NOT NULL PRIMARY KEY,"
		    +"	timestamp TEXT"
		    +")";
	    
	public static final String SQL_CREATE_RELATION_TAGS_TABLE = "CREATE TABLE IF NOT EXISTS relation_tags ("
	        +"	relation_id INTEGER NOT NULL,"
	        +"	k TEXT,"
	        +"	v TEXT"
	        +")";
	    
	    public static final String SQL_RELATION_TAGS_IDX = "CREATE INDEX IF NOT EXISTS relation_tags_rel_id_idx ON relation_tags(relation_id)";
	    
	    public static final String SQL_CREATE_MEMBERS_TABLE = "CREATE TABLE IF NOT EXISTS members ("
	        +"	relation_id INTEGER NOT NULL,"
	        +"	type TEXT,"
	        +"	ref INTEGER NOT NULL,"
	        +"	role TEXT"
	        +")";
	    
	    public static final String SQL_RELATION_MEMBERS_IDX = "CREATE INDEX IF NOT EXISTS relation_members_rel_id_idx ON members(relation_id)";
	    
	    public static final String SQL_CREATE_GRID_TABLE = "CREATE TABLE IF NOT EXISTS grid ("
    		+"	id INTEGER PRIMARY KEY AUTOINCREMENT,"
    		+"	minLat REAL NOT NULL,"
    		+"	minLon REAL NOT NULL,"
    		+"	maxLat REAL NOT NULL,"
    		+"	maxLon REAL NOT NULL"
    		+")";
	    
	    public static final String SQL_CREATE_INLINE_QUERIES_TABLE = "CREATE TABLE IF NOT EXISTS inline_queries (id INTEGER PRIMARY KEY AUTOINCREMENT, query TEXT NOT NULL, [select] TEXT NOT NULL)";
	    
	    public static final String SQL_CREATE_INLINE_RESULTS_TABLE = "CREATE TABLE IF NOT EXISTS inline_results (query_id INTEGER NOT NULL, value TEXT)";
	    
	    public static final String SQL_CREATE_STARRED_TABLE = "CREATE TABLE IF NOT EXISTS starred (type TEXT NOT NULL, id INTEGER NOT NULL, title TEXT NOT NULL)";

}
