/**
 * 
 */
package il.yrtimid.osm.osmpoi.dbcreator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
	
	INotificationManager notificationManager;
	IDbCachedFiller poiDbHelper;
	IDbCachedFiller addressDbHelper;

	
	/**
	 * 
	 */
	public DbCreator(IDbCachedFiller poiDB, IDbCachedFiller addrDB, INotificationManager notificationManager) {
		this.poiDbHelper = poiDB;
		this.addressDbHelper = addrDB;
		this.notificationManager = notificationManager;
	}
	
	/**
	 * 
	 */
	public void createEmptyDatabases() {
		try {
			poiDbHelper.create();
			addressDbHelper.create();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void importToDB(String sourceFilePath, ImportSettings settings) {
		try {
			
			Log.d("Importing file: "+sourceFilePath);

			File sourceFile = new File(sourceFilePath);
			if ((sourceFile.exists() && sourceFile.canRead())){

				long startTime = System.currentTimeMillis();
				final Notification notif = new Notification("Importing file into DB");
				//notif.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

			
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
					Log.d("Finished importing relations");
				}
				
				if (settings.isImportWays()){
					input = new BufferedInputStream(new FileInputStream(sourceFile));
					importWays(input, notif, settings);
					Log.d("Finished importing ways");
				}
				
				if (settings.isImportNodes()){
					input = new BufferedInputStream(new FileInputStream(sourceFile));
					importNodes(input, notif, settings);
					Log.d("Finished importing nodes");
				}
				
				notif.setLatestEventInfo("PBF Import", "Post-import calculations...");
				notificationManager.notify(IMPORT_TO_DB, notif);
				
				if(settings.isBuildGrid()){
					notif.setLatestEventInfo("PBF Import", "Creating grid...");
					notificationManager.notify(IMPORT_TO_DB, notif);
					poiDbHelper.initGrid();
					
					notif.setLatestEventInfo("PBF Import", "Optimizing grid...");
					notificationManager.notify(IMPORT_TO_DB, notif);
					poiDbHelper.optimizeGrid(settings.getGridSize());
				}
	
				long endTime = System.currentTimeMillis();
				int workTime = Math.round((endTime-startTime)/1000/60);

				Notification finalNotif = new Notification("Importing file into DB");
				//finalNotif.flags |= Notification.FLAG_AUTO_CANCEL;
				finalNotif.setLatestEventInfo("PBF Import", "Import done successfully. ("+workTime+"min.)");
				notificationManager.notify(IMPORT_TO_DB, finalNotif);
			}else {
				Notification finalNotif = new Notification("Importing file into DB");
				//finalNotif.flags |= Notification.FLAG_AUTO_CANCEL;
				finalNotif.setLatestEventInfo("PBF Import", "Import failed. File not found.");
				notificationManager.notify(IMPORT_TO_DB, finalNotif);
			}

		} catch (Exception ex) {
			Log.wtf("Exception while importing PBF into DB", ex);
		}
	}
	

	protected Long importRelations(final InputStream input, final Notification notif, final ImportSettings settings) {
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
							else if (settings.isAddress(item))
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
	
	protected Long importWays(final InputStream input, final Notification notif, final ImportSettings settings) {
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
						else if (settings.isAddress(item))
							addressDbHelper.addEntity(item);
						else if (settings.isImportRelations()){
							Way w = (Way)item;
							poiDbHelper.addWayIfBelongsToRelation(w);
							addressDbHelper.addWayIfBelongsToRelation(w);
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
	
	protected Long importNodes(final InputStream input, final Notification notif, final ImportSettings settings) {
		poiDbHelper.beginAdd();
		addressDbHelper.beginAdd();
		Long count = OsmImporter.processAll(input, new ItemPipe<Entity>() {
			@Override
			public void pushItem(Entity item) {
				try{
					if (item.getType() == EntityType.Node){
						
						settings.cleanTags(item);
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

	
}
