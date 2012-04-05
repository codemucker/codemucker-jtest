package com.bertvanbrakel.test.finder.matcher;

import java.util.regex.Pattern;

import com.bertvanbrakel.test.finder.ClassPathResource;
import com.google.common.base.Objects;

public class RegExpPatternResourceMatcher implements Matcher<ClassPathResource> {
	private final Pattern pattern;
	
	public RegExpPatternResourceMatcher(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean matches(ClassPathResource resource) {
		return pattern.matcher(resource.getRelPath()).matches();
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
			.add("pattern", pattern)
			.toString();
	}
}