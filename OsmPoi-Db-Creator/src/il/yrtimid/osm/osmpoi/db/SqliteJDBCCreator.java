/**
 * 
 */
package il.yrtimid.osm.osmpoi.db;

import il.yrtimid.osm.osmpoi.dal.IDatabase;
import il.yrtimid.osm.osmpoi.dal.Queries;


import java.io.File;
import java.sql.*;

/**
 * @author yrtimid
 *
 */
public class SqliteJDBCCreator extends SqliteJDBCDatabase implements IDatabase {

	protected Connection conn;
	
	/**
	 * @throws Exception 
	 * 
	 */
	public SqliteJDBCCreator(String filePath) throws Exception {
		super(filePath);

		this.conn = getConnection();
	}
	
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.IDatabase#create(java.lang.String)
	 */
	@Override
	public void create() throws Exception {
		createAllTables();
	}
	
	protected void execSQL(String query) throws SQLException{
		Statement statement = null;
		try {
			statement = conn.createStatement();
			statement.executeUpdate(query);
		}finally{
			if (statement!=null)
				statement.close();
		}
	}
	
	
	private void createAllTables() throws SQLException{
		execSQL(Queries.SQL_CREATE_BOUNDS_TABLE);
		
		execSQL(Queries.SQL_CREATE_NODE_TABLE);
		execSQL(Queries.SQL_CREATE_NODE_TAGS_TABLE);
		execSQL(Queries.SQL_NODE_TAGS_IDX);
		
		execSQL(Queries.SQL_CREATE_WAYS_TABLE);
		execSQL(Queries.SQL_CREATE_WAY_TAGS_TABLE);
		execSQL(Queries.SQL_WAY_TAGS_IDX);
		execSQL(Queries.SQL_CREATE_WAY_NODES_TABLE);
		execSQL(Queries.SQL_WAY_NODES_WAY_NODE_IDX);
		execSQL(Queries.SQL_WAY_NODES_WAY_IDX);
		execSQL(Queries.SQL_WAY_NODES_NODE_IDX);
		
		execSQL(Queries.SQL_CREATE_RELATIONS_TABLE);
		execSQL(Queries.SQL_CREATE_RELATION_TAGS_TABLE);
		execSQL(Queries.SQL_RELATION_TAGS_IDX);
		execSQL(Queries.SQL_CREATE_MEMBERS_TABLE);
		execSQL(Queries.SQL_RELATION_MEMBERS_IDX);
		
		execSQL(Queries.SQL_CREATE_GRID_TABLE);
		
		execSQL(Queries.SQL_CREATE_INLINE_QUERIES_TABLE);
		execSQL(Queries.SQL_CREATE_INLINE_RESULTS_TABLE);
	}

	@Override
	public void drop() {
		new File(filePath).delete();
	}
}
