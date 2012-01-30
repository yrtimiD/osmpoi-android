package il.yrtimid.osm.osmpoi.tagmatchers;

public class KeyValueMatcher extends TagMatcher {

	private String k;
	private String v;
	private Boolean caseSensitive = true;

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
		this(key, value, false);
	}

	public KeyValueMatcher(String key, String value, Boolean caseSensitive) {
		this.k = key.replace('%', '*');
		this.v = value.replace('%', '*');
		this.caseSensitive = caseSensitive;
	}

	@Override
	public Boolean isMatch(CharSequence key, CharSequence value) {
		return (isMatchPattern(key, k) && isMatchPattern(value,v));
	}
	
	private Boolean isMatchPattern(CharSequence value, CharSequence pattern){
		String pat = pattern.toString();
		boolean isExactMatch = !pat.contains("*");
		
		if (pattern.equals("*")) return true;
		else if (isExactMatch){
			if (caseSensitive)
				return value.equals(pattern);
			else
				return value.toString().equalsIgnoreCase(pat);
		}
		else {
			if (caseSensitive)
				return value.toString().contains(pattern);
			else
				return value.toString().toLowerCase().contains(pat.toLowerCase());
		}
	}
	
	public String getKey(){
		return k;
	}
	
	public String getValue(){
		return v;
	}
	
	public boolean isKeyExactMatch(){
		return !(k.contains("*"));
	}

	public boolean isValueExactMatch(){
		return !(v.contains("*"));
	}

}
