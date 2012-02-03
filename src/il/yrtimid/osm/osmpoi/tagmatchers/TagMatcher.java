/**
 * 
 */
package il.yrtimid.osm.osmpoi.tagmatchers;

import il.yrtimid.osm.osmpoi.domain.Entity;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all entity filters
 * 
 * @author yrtimiD
 */
public abstract class TagMatcher {

	public abstract Boolean isMatch(CharSequence key, CharSequence value);

	public static TagMatcher Parse(CharSequence expression) throws InvalidParameterException {
		String e = expression.toString();
		if (e.length() == 0)
			return new KeyValueMatcher("*", "*");

		//trim braces
		if (e.startsWith(Character.toString(OPEN_BRACE)) && e.endsWith(Character.toString(CLOSE_BRACE))){
			e = e.substring(1, e.length()-1);
		}
		
		char op = '&';
		String[] parts = splitBy(e, op);// (X | Y) & Z will become "(X | Y)", "Z"
		if (parts.length == 1){
			op = '|';
			parts = splitBy(e, op);// (X & Y) | Z will become "(X & Y)", "Z"
		}
		if (parts.length == 1){
			op = '!';
			parts = splitBy(e, op);// !(X & Y) will become "", "(X & Y)"
		}
		if (parts.length == 1){
			op = '=';
			parts = splitBy(e, op);// X=Y Z will become "X", "Y Z"
		}
		
		if (parts.length == 1) { //no &,|,=
			if (!e.contains("*")) e = "*"+e+"*";
			return new KeyValueMatcher("*", e, false);
		}else if (parts.length == 2 && op == '='){
			return new KeyValueMatcher(parts[0], parts[1], false);
		} else if (parts.length == 2 && op == '!') {
			return new NotMatcher(Parse(parts[1]));
		} else {
			if ((op=='&') || (op=='|')) {
				TagMatcher left = Parse(parts[0]);
				for (int i = 1; i < parts.length; i++) { //operator index
					TagMatcher right = Parse(parts[i]);
										
					if (op == '&'){
						left = new AndMatcher(left, right);
					} else if (op=='|'){
						left = new OrMatcher(left, right);
					} else {
						throw new IllegalArgumentException("something goes wrong while parsing '"+e+"'");
					}
				}
				return left;
			}
			else {
				throw new InvalidParameterException("Unknown operator "+parts[1]+" in '"+e+"'");
			}
		}
	}

	enum States
	{
		NORMAL_PROGRESS,
		MATCHING_BRACE_SEARCHING,
		FOUND_END_OF_PART
	}

	private static final char OPEN_BRACE = '(';
	private static final char CLOSE_BRACE = ')';

	public static String[] splitBy(String expression, char separator)
	{
		List<String> parts = new ArrayList<String>();
		States state = States.NORMAL_PROGRESS;
		int index = 0;
		int braces = 0;
		boolean canProgress = true;
		boolean canAppend = true;
		StringBuilder currentPart = new StringBuilder();
		while (index < expression.length())
		{
			char c = expression.charAt(index);
			canProgress = true;
			canAppend = true;
			switch (state)
			{
			case NORMAL_PROGRESS:
				if (c == OPEN_BRACE)
				{
					state = States.MATCHING_BRACE_SEARCHING;
					braces++;
				}
				else if (c == separator)
				{
					state = States.FOUND_END_OF_PART;
					canProgress = false;
					canAppend = false;
				}
				break;
			case MATCHING_BRACE_SEARCHING:
				if (c == OPEN_BRACE)
					braces++;
				if (c == CLOSE_BRACE)
					braces--;
				if (braces == 0)
					state = States.NORMAL_PROGRESS;
				break;
			case FOUND_END_OF_PART:
				parts.add(currentPart.toString().trim());
				currentPart.delete(0, currentPart.length());
				state = States.NORMAL_PROGRESS;
				canAppend = false;
				break;
			default:
				break;
			}
			if (canAppend)
				currentPart.append(c);
			if (canProgress)
				index++;
		}
		parts.add(currentPart.toString().trim());

		return parts.toArray(new String[parts.size()]);
	}

	public abstract Boolean isMatch(Entity entity);
}
