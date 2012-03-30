package com.bertvanbrakel.test.finder.matcher;

import java.util.regex.Pattern;

import com.bertvanbrakel.test.finder.ClassPathResource;

public class RegExpPatternResourceMatcher implements Matcher<ClassPathResource> {
	private final Pattern pattern;
	
	RegExpPatternResourceMatcher(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean matches(ClassPathResource resource) {
		return pattern.matcher(resource.getRelPath()).matches();
	}
}