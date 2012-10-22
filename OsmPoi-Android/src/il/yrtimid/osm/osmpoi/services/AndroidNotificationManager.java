/**
 * 
 */
package il.yrtimid.osm.osmpoi.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.dbcreator.common.INotificationManager;
import il.yrtimid.osm.osmpoi.dbcreator.common.Notification2;

/**
 * @author yrtimid
 *
 */
public class AndroidNotificationManager implements INotificationManager {

	NotificationManager notificationManager;
	Context context;
	PendingIntent contentIntent;
	
	public AndroidNotificationManager(Context context, PendingIntent contentIntent) {
		this.context = context;
		this.contentIntent = contentIntent;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	

	public void notify(int id, Notification2 notification) {
		Notification notif = new Notification(R.drawable.ic_launcher, notification.tickerText, notification.when);
		notif.flags |= Notification.FLAG_AUTO_CANCEL;
		notif.setLatestEventInfo(context, notification.title, notification.text, contentIntent);
		notificationManager.notify(id, notif);
	}

}
