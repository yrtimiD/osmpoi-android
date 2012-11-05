/**
 * 
 */
package il.yrtimid.osm.osmpoi.db;

import il.yrtimid.osm.osmpoi.dal.Queries;
import il.yrtimid.osm.osmpoi.domain.GridCell;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author yrtimid
 * 
 */
public class SqliteJDBCGridReader extends SqliteJDBCDatabase {

	/**
	 * @throws ClassNotFoundException 
	 * 
	 */
	public SqliteJDBCGridReader(String filePath) throws ClassNotFoundException {
		super(filePath);
	}
	
	public Collection<GridCell> getGrid() throws SQLException{
		Connection conn = getConnection();
		Statement statement = null;
		ResultSet cur = null;
		
		Collection<GridCell> cells = new ArrayList<GridCell>();
		
		try {
			statement = conn.createStatement();
			cur = statement.executeQuery("SELECT * FROM "+Queries.GRID_TABLE);
			while(cur.next()){
				Integer id = cur.getInt("id");
				double minLat = cur.getDouble("minLat");
				double maxLat = cur.getDouble("maxLat");
				double minLon = cur.getDouble("minLon");
				double maxLon = cur.getDouble("maxLon");
				
				cells.add(new GridCell(id, minLat, minLon, maxLat, maxLon));
			}
		}finally{
			if (cur != null) 
				cur.close();
			if (statement != null) 
				statement.close();
			if (conn != null)
				conn.close();
		}
		
		return cells;
	}
}
