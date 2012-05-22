/**
 * 
 */
package il.yrtimid.osm.osmpoi.dal;

import java.util.regex.Matcher;

import il.yrtimid.osm.osmpoi.tagmatchers.AndMatcher;
import il.yrtimid.osm.osmpoi.tagmatchers.KeyValueMatcher;
import il.yrtimid.osm.osmpoi.tagmatchers.NotMatcher;
import il.yrtimid.osm.osmpoi.tagmatchers.OrMatcher;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

/**
 * @author yrtimid
 * 
 */
public class TagMatcherFormatter {
	public static class WhereClause{
		public String where;
		
		public WhereClause(String where){
			this.where = where;
		}
	}
	
	public static WhereClause format(TagMatcher matcher, String baseQuery) {
		if (matcher instanceof KeyValueMatcher) {
			
			KeyValueMatcher kv = (KeyValueMatcher) matcher;
			String kf;
			String vf;
			if (kv.isKeyExactMatch())
				kf = String.format("k='%s'", kv.getKey().replace("'", "''"));
			else 
				kf = String.format("k like '%s'", kv.getKey().replace('*', '%').replace("'", "''"));
			
			if (kv.isValueExactMatch())
				vf = String.format("v='%s'", kv.getValue().replace("'", "''"));
			else
				vf = String.format("v like '%s'", kv.getValue().replace('*', '%').replace("'", "''"));
				
			String where = String.format("(%s) AND (%s)", kf, vf);
			return new WhereClause(String.format(baseQuery, where));
			
		} else if (matcher instanceof AndMatcher) {
			AndMatcher am = (AndMatcher) matcher;
			WhereClause wcLeft = format(am.getLeft(), baseQuery);
			WhereClause wcRight = format(am.getRight(), baseQuery);
			return new WhereClause(String.format("(%s AND %s)", wcLeft.where, wcRight.where));
		} else if (matcher instanceof OrMatcher) {
			OrMatcher om = (OrMatcher) matcher;
			WhereClause wcLeft = format(om.getLeft(), baseQuery);
			WhereClause wcRight = format(om.getRight(), baseQuery);
			return new WhereClause(String.format("(%s OR %s)", wcLeft.where, wcRight.where));
		} else if (matcher instanceof NotMatcher) {
			NotMatcher nm = (NotMatcher) matcher;
			WhereClause wc = format(nm.getMatcher(), baseQuery);
			return new WhereClause(String.format("(NOT %s)", wc.where));
		} else {
			throw new IllegalArgumentException("Unknown matcher type "+ Matcher.class.getName());
		}
	}
}
