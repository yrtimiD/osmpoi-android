/**
 * 
 */
package il.yrtimid.osm.osmpoi.tagmatchers;

/**
 * @author yrtimid
 *
 */
public abstract class BinaryMatcher extends TagMatcher {
	protected TagMatcher left;
	protected TagMatcher right;
	
	public TagMatcher getLeft(){
		return left;
	}
	
	public TagMatcher getRight(){
		return right;
	}
}
