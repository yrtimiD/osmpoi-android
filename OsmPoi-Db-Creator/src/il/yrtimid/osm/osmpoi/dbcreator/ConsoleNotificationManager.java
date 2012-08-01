/**
 * 
 */
package il.yrtimid.osm.osmpoi.dbcreator;

/**
 * @author yrtimid
 *
 */
public class ConsoleNotificationManager implements INotificationManager {
	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.dbcreator.INotificationManager#notify(int, il.yrtimid.osm.osmpoi.dbcreator.Notification)
	 */
	@Override
	public void notify(int id, Notification notification){
		System.out.println(notification.getTitle()+": "+notification.getMessage());
	}
}
