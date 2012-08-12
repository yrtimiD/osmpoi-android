/**
 * 
 */
package il.yrtimid.osm.osmpoi.db;

import il.yrtimid.osm.osmpoi.dal.IDatabase;
import il.yrtimid.osm.osmpoi.dal.Queries;


import java.sql.*;

/**
 * @author yrtimid
 *
 */
public class SqliteJDBCCreator implements IDatabase {

	protected Connection conn;
	String filePath;
	
	/**
	 * @throws Exception 
	 * 
	 */
	public SqliteJDBCCreator(String filePath) throws Exception {
		this.filePath = filePath;
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.IDatabase#create(java.lang.String)
	 */
	@Override
	public void create() throws Exception {
		Class.forName("org.sqlite.JDBC");
		this.conn = DriverManager.getConnection("jdbc:sqlite:"+filePath);
		
		createAllTables();
	}
	
	private void execSQL(String query){
		Statement statement;
		try {
			statement = conn.createStatement();
			statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private void createAllTables(){
		execSQL(Queries.SQL_CREATE_BOUNDS_TABLE);
		
		execSQL(Queries.SQL_CREATE_NODE_TABLE);
		execSQL(Queries.SQL_CREATE_NODE_TAGS_TABLE);
		execSQL(Queries.SQL_NODE_TAGS_IDX);
		
		execSQL(Queries.SQL_CREATE_WAYS_TABLE);
		execSQL(Queries.SQL_CREATE_WAY_NODES_TABLE);
		execSQL(Queries.SQL_WAY_NODES_WAY_IDX);
		execSQL(Queries.SQL_WAY_NODES_NODE_IDX);
		execSQL(Queries.SQL_CREATE_WAY_TAGS_TABLE);
		execSQL(Queries.SQL_WAY_TAGS_IDX);
		
		execSQL(Queries.SQL_CREATE_RELATIONS_TABLE);
		execSQL(Queries.SQL_CREATE_RELATION_TAGS_TABLE);
		execSQL(Queries.SQL_RELATION_TAGS_IDX);
		execSQL(Queries.SQL_CREATE_MEMBERS_TABLE);
		execSQL(Queries.SQL_RELATION_MEMBERS_IDX);
		
		execSQL(Queries.SQL_CREATE_GRID_TABLE);
		
		execSQL(Queries.SQL_CREATE_INLINE_QUERIES_TABLE);
		execSQL(Queries.SQL_CREATE_INLINE_RESULTS_TABLE);
	}

}
