package il.yrtimid.osm.osmpoi.tagmatchers;

public class NotMatcher extends TagMatcher {

	TagMatcher matcher;

	public NotMatcher(TagMatcher matcher) {
		this.matcher = matcher;
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher#isMatch(java.lang.CharSequence, java.lang.CharSequence)
	 */
	@Override
	public Boolean isMatch(CharSequence key, CharSequence value) {
		return !matcher.isMatch(key, value);
	}

	public TagMatcher getMatcher(){
		return matcher;
	}
}
