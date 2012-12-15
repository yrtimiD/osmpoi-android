/**
 * 
 */
package il.yrtimid.osm.osmpoi.tagmatchers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yrtimid
 * 
 */
public abstract class BinaryMatcher extends TagMatcher {
	protected TagMatcher left;
	protected TagMatcher right;

	public TagMatcher getLeft() {
		return left;
	}

	public TagMatcher getRight() {
		return right;
	}

	/**
	 * Returns all sibling items of same operator. For ex: ((a|b)|(c&d)|e) will return a,b,(c&d),e
	 * @return
	 */
	public List<TagMatcher> getAllSiblings() {
		List<TagMatcher> list = new ArrayList<TagMatcher>();
		getAllSiblings(this, list);
		return list;
	}

	private void getAllSiblings(BinaryMatcher matcher, List<TagMatcher> list) {
		if (this.getClass().isInstance(matcher.left))
			getAllSiblings((BinaryMatcher)matcher.left, list);
		else
			list.add(matcher.left);
			
		if (this.getClass().isInstance(matcher.right))
			getAllSiblings((BinaryMatcher)matcher.right, list);
		else 
			list.add(matcher.right);

	}

}
