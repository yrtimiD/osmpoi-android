package il.yrtimid.osm.osmpoi.tagmatchers;

import il.yrtimid.osm.osmpoi.domain.Entity;
import il.yrtimid.osm.osmpoi.domain.Tag;

public class KeyValueMatcher extends TagMatcher {

	private String k;
	private String v;

	/**
	 * Creates new matcher which will match tags with exact name of the key and
	 * exact value '*' may be used to match any key or any value. Example: *=* -
	 * matches all nodes with at least one tag name=* - matches nodes which has
	 * name tag with any value *=test - matches nodes which has any tag with the
	 * "test" value
	 * 
	 * @param key
	 *            - '*' for any
	 * @param value
	 *            - '*' for any
	 */
	public KeyValueMatcher(String key, String value) {
		this.k = key.replace('%', '*');
		this.v = value.replace('%', '*');
	}

	@Override
	public Boolean isMatch(CharSequence key, CharSequence value) {
		return (isMatchPattern(key, k) && isMatchPattern(value, v));
	}

	private Boolean isMatchPattern(CharSequence value, CharSequence pattern) {
		String pat = pattern.toString();

		if (pattern.equals("*"))
			return true;

		boolean isExactMatch = !pat.contains("*");
		boolean isBegins = false, isEnds = false, isContains = false;
		if (pat.startsWith("*")) {
			isBegins = true;
			pat = pat.substring(1);
		}
		if (pat.endsWith("*")) {
			isEnds = true;
			pat = pat.substring(0, pat.length() - 1);
		}
		if (isBegins && isEnds) {
			isContains = true;
			isBegins = false;
			isEnds = false;
		}

		if (isExactMatch) {
			return value.toString().equalsIgnoreCase(pat);
		}
		else {
			if (isBegins)
				return value.toString().toLowerCase().startsWith(pat.toLowerCase());
			else if (isEnds)
				return value.toString().toLowerCase().endsWith(pat.toLowerCase());
			else if (isContains)
				return value.toString().toLowerCase().contains(pat.toLowerCase());
		}

		return false;
	}

	public String getKey() {
		return k;
	}

	public String getValue() {
		return v;
	}

	public boolean isKeyExactMatch() {
		return !(k.contains("*"));
	}

	public boolean isValueExactMatch() {
		return !(v.contains("*"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher#isMatch(il.yrtimid.osm.osmpoi
	 * .domain.Entity)
	 */
	@Override
	public Boolean isMatch(Entity entity) {
		for (Tag t : entity.getTags()) {
			if (isMatch(t.getKey(), t.getValue()))
				return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "key:"+k+"=value:"+v;
	}
}
