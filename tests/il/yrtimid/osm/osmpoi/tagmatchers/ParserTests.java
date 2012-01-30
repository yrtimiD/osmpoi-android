/**
 * 
 */
package il.yrtimid.osm.osmpoi.tagmatchers;

import junit.framework.TestCase;

/**
 * @author yrtimid
 *
 */
public class ParserTests extends TestCase {

	/**
	 * Test method for {@link il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher#Parse(java.lang.CharSequence)}.
	 */
	public void testParse() {
		String expression = "(k1=v1 & k2=v2 & k3=v3) | k4=v4";
		TagMatcher tm = TagMatcher.Parse(expression);
		assertNotNull(tm);
	}

}
