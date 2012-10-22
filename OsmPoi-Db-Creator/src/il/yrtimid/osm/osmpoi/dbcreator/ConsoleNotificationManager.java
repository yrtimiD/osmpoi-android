/**
 * 
 */
package il.yrtimid.osm.osmpoi.dbcreator;

import il.yrtimid.osm.osmpoi.dbcreator.common.*;

/**
 * @author yrtimid
 *
 */
public class ConsoleNotificationManager implements INotificationManager {
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dbcreator.INotificationManager#notify(int, il.yrtimid.osm.osmpoi.dbcreator.Notification)
	 */
	@Override
	public void notify(int id, Notification2 notification){
		System.out.println(notification.title+": "+notification.text);
	}
}
