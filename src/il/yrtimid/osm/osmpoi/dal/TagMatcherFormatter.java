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
		public int count;
		
		public WhereClause(String where, int count){
			this.where = where;
			this.count = count;
		}
	}
	
	public static WhereClause format(TagMatcher matcher) {
		return format(matcher, 0);
	}
	
	private  static WhereClause format(TagMatcher matcher, int index) {
		if (matcher instanceof KeyValueMatcher) {
			index++;
			String tbl = "t"+index;
			
			KeyValueMatcher kv = (KeyValueMatcher) matcher;
			String kf;
			String vf;
			if (kv.isKeyExactMatch())
				kf = String.format("k='%s'", kv.getKey());
			else 
				kf = String.format("k like '%s'", kv.getKey().replace('*', '%'));
			
			if (kv.isValueExactMatch())
				vf = String.format("v='%s'", kv.getValue());
			else
				vf = String.format("v like '%s'", kv.getValue().replace('*', '%'));
				
			return new WhereClause(String.format("(%s.%s) AND (%s.%s)", tbl, kf, tbl, vf), index);
			
		} else if (matcher instanceof AndMatcher) {
			AndMatcher am = (AndMatcher) matcher;
			WhereClause wcLeft = format(am.getLeft(), index);
			index = wcLeft.count;
			WhereClause wcRight = format(am.getRight(), index);
			index = wcRight.count;
			return new WhereClause(String.format("(%s) AND (%s)", wcLeft.where, wcRight.where), index);
		} else if (matcher instanceof OrMatcher) {
			OrMatcher om = (OrMatcher) matcher;
			WhereClause wcLeft = format(om.getLeft(), index);
			index = wcLeft.count;
			WhereClause wcRight = format(om.getRight(), index);
			index = wcRight.count;
			return new WhereClause(String.format("(%s) OR (%s)", wcLeft.where, wcRight.where), index);
		} else if (matcher instanceof NotMatcher) {
			NotMatcher nm = (NotMatcher) matcher;
			WhereClause wc = format(nm.getMatcher(), index);
			index = wc.count;
			return new WhereClause(String.format("NOT (%s)", wc.where), index);
		} else {
			throw new IllegalArgumentException("Unknown matcher type "+ Matcher.class.getName());
		}
	}
}
