/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;


import java.util.Date;
import java.util.List;

/**
 * @author yrtimid
 *
 */
public class DownloadItem{
	public enum ItemType {
		FOLDER, FILE
	}
	
	
	public String Url;
	public String Name;
	public Integer Size;
	public Date LastModified;
	public ItemType Type;
	public DownloadItem Parent;
	public List<DownloadItem> SubItems;
}
