package il.yrtimid.osm.osmpoi.dbcreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import il.yrtimid.osm.osmpoi.ImportSettings;
import il.yrtimid.osm.osmpoi.SortedProperties;
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
	private static final String PROPERTIES_FILE_NAME = "config.properties"; 
	private static ImportSettings settings = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		createSettings();
		
		args = new String[0]; ////////////// DEBUG /////////////////////
		
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
	
	private static void createSettings(){
		try{
			File propsFile = new File(PROPERTIES_FILE_NAME);
			SortedProperties props = new SortedProperties();
			
			if (propsFile.exists()){
				props.load(new FileInputStream(propsFile));
				settings = ImportSettings.createFromProperties(props);
			}else{
				settings = ImportSettings.getDefault();
				settings.writeToProperties(props);
				props.store(new FileOutputStream(propsFile),"OsmPoi-Db-Creator default settings");
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
