/**
 * 
 */
package il.yrtimid.osm.osmpoi.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author yrtimid
 *
 */
public class SqliteJDBCDatabase {
	String filePath;

	/**
	 * @throws ClassNotFoundException 
	 * 
	 */
	public SqliteJDBCDatabase(String filePath) throws ClassNotFoundException {
		this.filePath = filePath;
		Class.forName("org.sqlite.JDBC");
	}
	
	protected Connection getConnection() throws SQLException{
		return DriverManager.getConnection("jdbc:sqlite:" + filePath);
	}
}
