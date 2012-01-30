package il.yrtimid.osm.osmpoi.tagmatchers;

public class OrMatcher extends BinaryMatcher {
	public OrMatcher(TagMatcher left, TagMatcher right) {
		this.left = left;
		this.right = right;
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher#isMatch(java.lang.CharSequence, java.lang.CharSequence)
	 */
	@Override
	public Boolean isMatch(CharSequence key, CharSequence value) {
		return left.isMatch(key, value) || right.isMatch(key, value);
	}
}
