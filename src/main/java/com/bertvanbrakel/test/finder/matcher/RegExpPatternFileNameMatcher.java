package com.bertvanbrakel.test.finder.matcher;

import java.util.regex.Pattern;

import com.bertvanbrakel.test.finder.ClassPathResource;

public class RegExpPatternFileNameMatcher implements Matcher<String> {
	private final Pattern pattern;
	
	RegExpPatternFileNameMatcher(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean matches(String name) {
		return pattern.matcher(name).matches();
	}
}