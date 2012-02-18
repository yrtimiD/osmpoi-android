/**
 * 
 */
package il.yrtimid.osm.osmpoi.services;

import il.yrtimid.osm.osmpoi.ImportSettings;
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.ItemPipe;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.dal.CachedDbOpenHelper;
import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.EntityType;
import il.yrtimid.osm.osmpoi.domain.Node;
import il.yrtimid.osm.osmpoi.pbf.OsmImporter;
import il.yrtimid.osm.osmpoi.pbf.ProgressNotifier;
import il.yrtimid.osm.osmpoi.ui.SearchActivity;
import il.yrtimid.osm.osmpoi.ui.Preferences;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;

import org.apache.http.util.ByteArrayBuffer;

import dalvik.system.PotentialDeadlockError;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.webkit.URLUtil;

/**
 * @author yrtimid
 * 
 */
public class FileProcessingService extends Service {
	public enum Operation {
		IMPORT_TO_DB,
		COMPACT_PBF,
		CLEAR_DB
	}

	public static final String EXTRA_FILE_PATH = "file_path";
	public static final String EXTRA_OPERATION = "operation";
	public static final int IMPORT_TO_DB_ID = 1;
	public static final int CLEAR_DB = 3;
	public static final int ABORTED = 4;

	private boolean hasRunningJobs = false;

	CachedDbOpenHelper poiDbHelper;
	CachedDbOpenHelper addressDbHelper;
	NotificationManager notificationManager;
	PendingIntent contentIntent;
	Context context;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		poiDbHelper = new CachedDbOpenHelper(this, OsmPoiApplication.Config.getPoiDbLocation());
		addressDbHelper = new CachedDbOpenHelper(this, OsmPoiApplication.Config.getAddressDbLocation());
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Intent notificationIntent = new Intent(this, SearchActivity.class);
		contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		poiDbHelper.close();
		addressDbHelper.close();
		
		if (hasRunningJobs) {

			final Notification notif = new Notification(R.drawable.ic_launcher, "OsmPoi Service", System.currentTimeMillis());
			notif.flags |= Notification.FLAG_AUTO_CANCEL;
			notif.setLatestEventInfo(context, "OsmPoi Service", "Backgroud job was canceled!", contentIntent);
			notificationManager.notify(ABORTED, notif);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(final Intent intent, int flags, final int startId) {
		super.onStartCommand(intent, flags, startId);

		Runnable task = null;

		switch (Enum.valueOf(Operation.class, intent.getStringExtra(EXTRA_OPERATION))) {
		case IMPORT_TO_DB:
			task = new Runnable() {
				public void run() {
					hasRunningJobs = true;
					importToDB(intent.getStringExtra(EXTRA_FILE_PATH));
					hasRunningJobs = false;
					stopSelf(startId);
				}
			};
			break;
		case CLEAR_DB:
			task = new Runnable() {
				public void run() {
					hasRunningJobs = true;
					clearDb();
					hasRunningJobs = false;
					stopSelf(startId);
				}
			};
			break;
		}

		if (task != null){
			Thread t = new Thread(task, "Service");
			t.setPriority(Thread.NORM_PRIORITY-1);
			t.start();
		}else {
			stopSelf();
		}
		
		return START_REDELIVER_INTENT;
	}

	private void clearDb() {
		Notification notif = new Notification(R.drawable.ic_launcher, "Clearing DB", System.currentTimeMillis());
		notif.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

		
		try {
			notif.setLatestEventInfo(context, "Clearing DB", "Clearing DB...", contentIntent);
			notificationManager.notify(CLEAR_DB, notif);
	
			startForeground(CLEAR_DB, notif);
			
			poiDbHelper.clearAll();
			addressDbHelper.clearAll();

			stopForeground(true);
			
			notif = new Notification(R.drawable.ic_launcher, "Clearing DB", System.currentTimeMillis());
			notif.flags |= Notification.FLAG_AUTO_CANCEL;
			notif.setLatestEventInfo(context, "Clearing DB", "Clearing DB finished", contentIntent);
			notificationManager.notify(CLEAR_DB, notif);

		} catch (Exception ex) {
			Log.wtf("Service clearDb", ex);
		}
	}

	private void importToDB(String sourceFilePath) {
		try {
			Log.d("Importing file: "+sourceFilePath);
			if (sourceFilePath.startsWith("/")){
				//local file, can use directly
			}else {
				sourceFilePath = downloadFile(sourceFilePath);
			}

			final Notification notif = new Notification(R.drawable.ic_launcher, "Importing file into DB", System.currentTimeMillis());
			notif.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
			
			notif.setLatestEventInfo(context, "PBF Import", "Clearing DB...", contentIntent);
			notificationManager.notify(IMPORT_TO_DB_ID, notif);

			startForeground(IMPORT_TO_DB_ID, notif);

			final ImportSettings settings = Preferences.getImportSettings(context);

			if (settings.isClearBeforeImport()){
				poiDbHelper.clearAll();
				addressDbHelper.clearAll();
			}
			
			notif.setLatestEventInfo(context, "PBF Import", "Importing in progress...", contentIntent);
			notificationManager.notify(IMPORT_TO_DB_ID, notif);

			File sourceFile = new File(sourceFilePath);
			InputStream input;

			if (settings.isImportRelations()){
				//input = new BufferedInputStream(new FileInputStream(sourceFile));
				//importNodes(input, notif, settings);
				//TODO import relations
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
			
			notif.setLatestEventInfo(context, "PBF Import", "Post-import calculations...", contentIntent);
			notificationManager.notify(IMPORT_TO_DB_ID, notif);
			
			if(settings.isBuildGrid()){
				poiDbHelper.updateNodesGrid();
				poiDbHelper.optimizeGrid(1000);
				//TODO: is addr db needs grid too? 
			}

			stopForeground(true);
			
			Notification finalNotif = new Notification(R.drawable.ic_launcher, "Importing file into DB", System.currentTimeMillis());
			finalNotif.flags |= Notification.FLAG_AUTO_CANCEL;
			finalNotif.setLatestEventInfo(context, "PBF Import", "Import done successfully", contentIntent);
			notificationManager.notify(IMPORT_TO_DB_ID, finalNotif);

		} catch (Exception ex) {
			Log.wtf("Exception while importing PBF into DB", ex);
		}
	}

	public Long importWays(final InputStream input, final Notification notif, final ImportSettings settings) {
		poiDbHelper.beginAdd();
		addressDbHelper.beginAdd();
		Long count = OsmImporter.processAll(input, new ItemPipe<Entity>() {
			@Override
			public void pushItem(Entity item) {
				if (item.getType() == EntityType.Way){
					settings.cleanTags(item);
					if (settings.isPoi(item))
						poiDbHelper.addEntity(item);
					else if (settings.isAddress(item))
						addressDbHelper.addEntity(item);
					else if (settings.isImportRelations()){
						//TODO: dbHelper.addWayIfBelongsToRelation((Way)item);
					}
				}
			}
		}, new ProgressNotifier() {
			@Override
			public void onProgressChange(Progress progress) {
				notif.setLatestEventInfo(context, "PBF Import", "Importing ways: " + progress.toString(), contentIntent);
				notificationManager.notify(IMPORT_TO_DB_ID, notif);
			}
		});
		
		if (count == -1L){
			notif.setLatestEventInfo(context, "PBF Import", "Nodes import failed", contentIntent);
		}
		
		poiDbHelper.endAdd();
		addressDbHelper.endAdd();
		
		return count;
	}
	
	public Long importNodes(final InputStream input, final Notification notif, final ImportSettings settings) {
		poiDbHelper.beginAdd();
		Long count = OsmImporter.processAll(input, new ItemPipe<Entity>() {
			@Override
			public void pushItem(Entity item) {
				if (item.getType() == EntityType.Node){
					
					settings.cleanTags(item);
					if (settings.isPoi(item))
						poiDbHelper.addEntity(item);
					else if (settings.isAddress(item))
						addressDbHelper.addEntity(item);
					else if (settings.isImportWays() || settings.isImportRelations()){
						Node n = (Node)item;
						poiDbHelper.addNodeIfBelongsToWay(n);
						addressDbHelper.addNodeIfBelongsToWay(n);
					}
				}
			}
		}, new ProgressNotifier() {
			@Override
			public void onProgressChange(Progress progress) {
				notif.setLatestEventInfo(context, "PBF Import", "Importing nodes: " + progress.toString(), contentIntent);
				notificationManager.notify(IMPORT_TO_DB_ID, notif);
			}
		});
		
		if (count == -1L){
			notif.setLatestEventInfo(context, "PBF Import", "Nodes import failed", contentIntent);
		}
		
		poiDbHelper.endAdd();
		return count;
	}

	private String downloadFile(String path){
		String localPath = path;
		File homeFolder = Preferences.getHomeFolder(context);
		byte[] buffer = new byte[1024];
		int downloadedSize = 0;
		int totalSize = 0;
		try{
			URL u = new URL(path);

			String outputFileName = URLUtil.guessFileName(path, null, null);
			File outputPath = new File(homeFolder,outputFileName);
			OutputStream output = new BufferedOutputStream(new FileOutputStream(outputPath), buffer.length);
			
			URLConnection conn = u.openConnection();
			totalSize = conn.getContentLength();
			InputStream input = new BufferedInputStream(conn.getInputStream(), buffer.length);
			
	        int bufferLength = 0;
	        while ( (bufferLength = input.read(buffer)) > 0 ) {
	        	output.write(buffer, 0, bufferLength);
	        	downloadedSize += bufferLength;
		        Log.d(String.format("Downloaded %d/%d", downloadedSize, totalSize));
	        }
	        output.close();
	        input.close();
	        localPath = outputPath.getPath();
		}catch(Exception e){
			Log.wtf("downloadFile", e);
		}
		return localPath;
	}
}
