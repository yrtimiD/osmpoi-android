package il.yrtimid.osm.osmpoi.dbcreator;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Collection;

import il.yrtimid.osm.osmpoi.ImportSettings;
import il.yrtimid.osm.osmpoi.SortedProperties;
import il.yrtimid.osm.osmpoi.db.SqliteJDBCCachedFiller;
import il.yrtimid.osm.osmpoi.db.SqliteJDBCGridReader;
import il.yrtimid.osm.osmpoi.dbcreator.common.*;
import il.yrtimid.osm.osmpoi.domain.GridCell;
import il.yrtimid.osm.osmpoi.domain.Point;

/**
 * 
 */

/**
 * @author yrtimid
 * 
 */
public class DbCreatorConsole {
	private static final String PROPERTIES_FILE_NAME = "dbcreator.properties";
	private static ImportSettings settings = null;
	private static final String ARGUMENT_CREATE = "--create";
	private static final String ARGUMENT_SAVE_GRID = "--save-grid";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		createSettings();

		// args = new String[0]; ////////////// DEBUG /////////////////////

		if (args.length == 2) {
			if (ARGUMENT_CREATE.equals(args[0])) {
				create(args[1]);
				return;
			} else if (ARGUMENT_SAVE_GRID.equals(args[0])) {
				saveGrid(args[1]);
				return;
			}
		}

		printHelp();
	}

	/**
	 * @param string
	 */
	private static void saveGrid(String dbFilePath) {
		PrintStream stream = System.out;
		try {
			File dbFile = new File(dbFilePath);
			if (dbFile.canRead()) {
				stream.println(dbFile.getName().replace('.', '_'));
				SqliteJDBCGridReader gridReader = new SqliteJDBCGridReader(dbFilePath);

				Collection<GridCell> cells = gridReader.getGrid();
				for(GridCell c : cells){
					stream.println(c.getId());
					for(int i=0;i<4;i++){
						Point p = c.getVertex(i);
						stream.println("\t"+p.getLongitude()+"\t"+p.getLatitude());
					}
					stream.println("END");
				}
				stream.println("END");
			} else {
				System.err.println("Can't read db file: " + dbFilePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void create(String sourceFilePath) {
		String poiDbName = "poi.db";
		String addrDbName = "addr.db";

		File poiDbFile = new File(poiDbName);
		File addrDbFile = new File(addrDbName);
		if (poiDbFile.exists()) {
			System.out.println("Deleting old poi db");
			poiDbFile.delete();
		}

		if (addrDbFile.exists()) {
			System.out.println("Deleting old addr db");
			addrDbFile.delete();
		}

		System.out.println("Processing file " + sourceFilePath);

		System.out.println(new File(".").getAbsolutePath());

		DbCreator creator;
		try {
			creator = new DbCreator(new SqliteJDBCCachedFiller(poiDbName), new SqliteJDBCCachedFiller(addrDbName), new ConsoleNotificationManager());

			creator.createEmptyDatabases();
			creator.importToDB(sourceFilePath, settings);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done.");
	}

	/**
	 * 
	 */
	private static void printHelp() {
		System.out.println("Usage:");
		System.out.println(ARGUMENT_CREATE + " <pbf file name>\tcreate new DB from PBF file");
		System.out.println(ARGUMENT_SAVE_GRID + " <db file>\toutputs grid from DB in poly format");
		System.out.println("");
	}

	private static void createSettings() {
		try {
			File propsFile = new File(PROPERTIES_FILE_NAME);
			SortedProperties props = new SortedProperties();

			if (propsFile.exists()) {
				props.load(new FileInputStream(propsFile));
				settings = ImportSettings.createFromProperties(props);
			} else {
				settings = ImportSettings.getDefault();
				settings.writeToProperties(props);
				props.store(new FileOutputStream(propsFile), "OsmPoi-Db-Creator default settings");
				System.out.println("New " + PROPERTIES_FILE_NAME + " file created with default settings");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
