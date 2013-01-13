package com.bertvanbrakel.test.finder.matcher;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Pattern;

import com.bertvanbrakel.lang.matcher.AbstractMatcher;
import com.bertvanbrakel.lang.matcher.MatchDiagnostics;
import com.bertvanbrakel.test.util.TestUtils;

public class RegExpMatcher extends AbstractMatcher<String> {
	private final Pattern pattern;
	
	public static RegExpMatcher withAntPattern(String antExpression){
		return new RegExpMatcher(TestUtils.antExpToPattern(antExpression));
	}
	
	public static RegExpMatcher withPattern(String pattern){
		return new RegExpMatcher(Pattern.compile(pattern));
	}
	
	public static RegExpMatcher withPattern(Pattern pattern){
		return new RegExpMatcher(pattern);
	}
	
	public RegExpMatcher(Pattern pattern) {
		this.pattern = checkNotNull(pattern,"expect regexp pattern");
	}

	@Override
	public boolean matches(String name, MatchDiagnostics diag) {
		return pattern.matcher(name).matches();
	}
}