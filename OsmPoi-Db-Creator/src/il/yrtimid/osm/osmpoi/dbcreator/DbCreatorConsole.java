package il.yrtimid.osm.osmpoi.dbcreator;

import java.io.File;

import il.yrtimid.osm.osmpoi.ImportSettings;
import il.yrtimid.osm.osmpoi.db.SqliteJDBCCachedFiller;
import il.yrtimid.osm.osmpoi.dbcreator.DbCreator;
import il.yrtimid.osm.osmpoi.domain.EntityType;

/**
 * 
 */

/**
 * @author yrtimid
 *
 */
public class DbCreatorConsole {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0){
			printHelp();
			return;
		}
		
		String poiDbName = "poi.db";
		String addrDbName = "addr.db";
		
		File poiDbFile = new File(poiDbName);
		File addrDbFile = new File(addrDbName);
		if (poiDbFile.exists()){
			System.out.println("Deleting old poi db");
			poiDbFile.delete();
		}
		
		if (addrDbFile.exists()){
			System.out.println("Deleting old addr db");
			addrDbFile.delete();
		}
			
		String sourceFilePath = args[0];
		System.out.println("Processing file "+sourceFilePath);
		
		ImportSettings settings = ImportSettings.getDefault();
		settings.setImportAddresses(false);
		settings.reset(EntityType.Node);
		settings.reset(EntityType.Way);
		settings.reset(EntityType.Relation);
		settings.setKey(EntityType.Node, "*", true);
		settings.setKey(EntityType.Way, "*", true);
		settings.setKey(EntityType.Relation, "*", true);
		
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
		
	}

}
