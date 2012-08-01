/**
 * 
 */
package il.yrtimid.osm.osmpoi.dbcreator;

/**
 * @author yrtimid
 * 
 */
public class Notification {
	private String tickerText;
	private String title;
	private String message;

	public Notification(String tickerText) {
		this.tickerText = tickerText;
	}

	public void setLatestEventInfo(String title, String message) {
		this.title = title;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
	public String getTitle() {
		return title;
	}

	public String getTickerText() {
		return tickerText;
	}
}
