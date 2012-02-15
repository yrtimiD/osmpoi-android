/**
 * 
 */
package il.yrtimid.osm.osmpoi;

/**
 * @author yrtimid
 *
 */
public class CancelFlag {
	private boolean cancel = false;
	
	public void cancel(){
		this.cancel = true;
	}
	
	public boolean isCancelled(){
		return cancel;
	}
	
	public boolean isNotCancelled(){
		return !cancel;
	}

}
