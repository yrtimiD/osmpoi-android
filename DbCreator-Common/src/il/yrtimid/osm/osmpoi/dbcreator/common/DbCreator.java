/**
 * 
 */
package il.yrtimid.osm.osmpoi.dbcreator.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import il.yrtimid.osm.osmpoi.ImportSettings;
import il.yrtimid.osm.osmpoi.ItemPipe;
import il.yrtimid.osm.osmpoi.dal.IDbCachedFiller;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.domain.Way;
import il.yrtimid.osm.osmpoi.pbf.OsmImporter;
import il.yrtimid.osm.osmpoi.pbf.ProgressNotifier;

/**
 * @author yrtimid
 *
 */
public class DbCreator {
	static final int IMPORT_TO_DB = 1;
	public static final int BUILD_GRID = 40;
	
	INotificationManager notificationManager;
	IDbCachedFiller poiDbHelper;
	IDbCachedFiller addressDbHelper;

	private static final Logger LOG = Logger.getLogger(DbCreator.class.getName());

	/**
	 * 
	 */
	public DbCreator(IDbCachedFiller poiDB, IDbCachedFiller addrDB, INotificationManager notificationManager) {
		this.poiDbHelper = poiDB;
		this.addressDbHelper = addrDB;
		this.notificationManager = notificationManager;
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void createEmptyDatabases() throws Exception {
		poiDbHelper.create();
		addressDbHelper.create();
	}

	public void importToDB(String sourceFilePath, ImportSettings settings) {
		try {
			
			LOG.finest("Importing file: "+sourceFilePath);

			File sourceFile = new File(sourceFilePath);
			if ((sourceFile.exists() && sourceFile.canRead())){

				long startTime = System.currentTimeMillis();
				final Notification2 notif = new Notification2("Importing file into DB", System.currentTimeMillis());
				notif.flags |= Notification2.FLAG_ONGOING_EVENT | Notification2.FLAG_NO_CLEAR;

			
				notif.setLatestEventInfo("PBF Import", "Clearing DB...");
				notificationManager.notify(IMPORT_TO_DB, notif);
	
				if (settings.isClearBeforeImport()){
					poiDbHelper.clearAll();
					addressDbHelper.clearAll();
				}
				
				notif.setLatestEventInfo("PBF Import", "Importing in progress...");
				notificationManager.notify(IMPORT_TO_DB, notif);
	
				
				InputStream input;
	
				if (settings.isImportRelations()){
					input = new BufferedInputStream(new FileInputStream(sourceFile));
					importRelations(input, notif, settings);
					LOG.finest("Finished importing relations");
				}
				
				if (settings.isImportWays()){
					input = new BufferedInputStream(new FileInputStream(sourceFile));
					importWays(input, notif, settings);
					LOG.finest("Finished importing ways");
				}
				
				if (settings.isImportNodes()){
					input = new BufferedInputStream(new FileInputStream(sourceFile));
					importNodes(input, notif, settings);
					LOG.finest("Finished importing nodes");
				}
				
				notif.setLatestEventInfo("PBF Import", "Post-import calculations...");
				notificationManager.notify(IMPORT_TO_DB, notif);
				
				if(settings.isBuildGrid()){
					notif.setLatestEventInfo("PBF Import", "Creating grid...");
					notificationManager.notify(IMPORT_TO_DB, notif);
					poiDbHelper.initGrid();
					
					notif.setLatestEventInfo("PBF Import", "Optimizing grid...");
					notificationManager.notify(IMPORT_TO_DB, notif);
					poiDbHelper.optimizeGrid(settings.getGridCellSize());
				}
	
				long endTime = System.currentTimeMillis();
				int workTime = Math.round((endTime-startTime)/1000/60);

				Notification2 finalNotif = new Notification2("Importing file into DB", System.currentTimeMillis());
				finalNotif.flags |= Notification2.FLAG_AUTO_CANCEL;
				finalNotif.setLatestEventInfo("PBF Import", "Import done successfully. ("+workTime+"min.)");
				notificationManager.notify(IMPORT_TO_DB, finalNotif);
			}else {
				Notification2 finalNotif = new Notification2("Importing file into DB", System.currentTimeMillis());
				finalNotif.flags |= Notification2.FLAG_AUTO_CANCEL;
				finalNotif.setLatestEventInfo("PBF Import", "Import failed. File not found.");
				notificationManager.notify(IMPORT_TO_DB, finalNotif);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.severe("Exception while importing PBF into DB. "+ ex.toString());
		}
	}
	

	protected Long importRelations(final InputStream input, final Notification2 notif, final ImportSettings settings) {
		Long count = 0L;
		try{
			poiDbHelper.beginAdd();
			addressDbHelper.beginAdd();
			count = OsmImporter.processAll(input, new ItemPipe<Entity>() {
				@Override
				public void pushItem(Entity item) {
					try{
						if (item.getType() == EntityType.Relation){
							settings.cleanTags(item);
							if (settings.isPoi(item))
								poiDbHelper.addEntity(item);
							
							if (settings.isImportAddresses() && settings.isAddress(item))
								addressDbHelper.addEntity(item);
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}, new ProgressNotifier() {
				@Override
				public void onProgressChange(Progress progress) {
					notif.setLatestEventInfo("PBF Import", "Importing relations: " + progress.toString());
					notificationManager.notify(IMPORT_TO_DB, notif);
				}
			});
			
			if (count == -1L){
				notif.setLatestEventInfo("PBF Import", "Relations import failed");
			}
			
			poiDbHelper.endAdd();
			addressDbHelper.endAdd();
		
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return count;
	}
	
	protected Long importWays(final InputStream input, final Notification2 notif, final ImportSettings settings) {
		poiDbHelper.beginAdd();
		addressDbHelper.beginAdd();
		Long count = OsmImporter.processAll(input, new ItemPipe<Entity>() {
			@Override
			public void pushItem(Entity item) {
				try{
					if (item.getType() == EntityType.Way){
						settings.cleanTags(item);
						if (settings.isPoi(item))
							poiDbHelper.addEntity(item);
						else if (settings.isImportRelations()){
							Way w = (Way)item;
							poiDbHelper.addWayIfBelongsToRelation(w);
						} 
							
						if (settings.isImportAddresses()){
							if (settings.isAddress(item))
								addressDbHelper.addEntity(item);
							else if (settings.isImportRelations()){
								Way w = (Way)item;
								addressDbHelper.addWayIfBelongsToRelation(w);
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}, new ProgressNotifier() {
			@Override
			public void onProgressChange(Progress progress) {
				notif.setLatestEventInfo("PBF Import", "Importing ways: " + progress.toString());
				notificationManager.notify(IMPORT_TO_DB, notif);
			}
		});
		
		if (count == -1L){
			notif.setLatestEventInfo("PBF Import", "Nodes import failed");
		}
		
		poiDbHelper.endAdd();
		addressDbHelper.endAdd();
		
		return count;
	}
	
	protected Long importNodes(final InputStream input, final Notification2 notif, final ImportSettings settings) {
		poiDbHelper.beginAdd();
		addressDbHelper.beginAdd();
		Long count = OsmImporter.processAll(input, new ItemPipe<Entity>() {
			@Override
			public void pushItem(Entity item) {
				try{
					if (item.getType() == EntityType.Node){
						
						settings.cleanTags(item);
						if (settings.isImportAddresses()){
							if (settings.isAddress(item))
								addressDbHelper.addEntity(item);
							else {
								Node n = (Node)item;
								if (settings.isImportWays()){
									addressDbHelper.addNodeIfBelongsToWay(n);
								}
								if (settings.isImportRelations()){
									addressDbHelper.addNodeIfBelongsToRelation(n);
								}
							}
						}
						
						if (settings.isPoi(item))
							poiDbHelper.addEntity(item);
						else {
							Node n = (Node)item;
							if (settings.isImportWays()){
								poiDbHelper.addNodeIfBelongsToWay(n);
							}
							if (settings.isImportRelations()){
								poiDbHelper.addNodeIfBelongsToRelation(n);
							}
						}
					}else if (item.getType() == EntityType.Bound){
						poiDbHelper.addEntity(item);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}, new ProgressNotifier() {
			@Override
			public void onProgressChange(Progress progress) {
				notif.setLatestEventInfo("PBF Import", "Importing nodes: " + progress.toString());
				notificationManager.notify(IMPORT_TO_DB, notif);
			}
		});
		
		if (count == -1L){
			notif.setLatestEventInfo("PBF Import", "Nodes import failed");
		}
		
		addressDbHelper.endAdd();
		poiDbHelper.endAdd();
		return count;
	}

	public void rebuildGrid(final ImportSettings settings){
		try {
			long startTime = System.currentTimeMillis();
			
			final Notification2 notif = new Notification2("Rebuilding grid", System.currentTimeMillis());
			notif.flags |= Notification2.FLAG_ONGOING_EVENT | Notification2.FLAG_NO_CLEAR;
			
			notif.setLatestEventInfo("Rebuilding grid", "Clearing old grid...");
			notificationManager.notify(BUILD_GRID, notif);

			notif.setLatestEventInfo("Rebuilding grid", "Creating grid...");
			notificationManager.notify(BUILD_GRID, notif);
			poiDbHelper.initGrid();

			notif.setLatestEventInfo("Rebuilding grid", "Optimizing grid...");
			notificationManager.notify(BUILD_GRID, notif);
			poiDbHelper.optimizeGrid(settings.getGridCellSize());

			long endTime = System.currentTimeMillis();
			int workTime = Math.round((endTime-startTime)/1000/60);
			Notification2 finalNotif = new Notification2("Rebuilding grid", System.currentTimeMillis());
			finalNotif.flags |= Notification2.FLAG_AUTO_CANCEL;
			finalNotif.setLatestEventInfo("Rebuilding grid", "Done successfully. ("+workTime+"min)");
			notificationManager.notify(IMPORT_TO_DB, finalNotif);

		} catch (Exception ex) {
			LOG.severe("Exception while rebuilding grid. "+ex.toString());
		}
	}
}
