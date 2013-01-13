package com.bertvanbrakel.test.finder.matcher;

import java.util.regex.Pattern;

import com.bertvanbrakel.lang.matcher.AbstractNotNullMatcher;
import com.bertvanbrakel.test.finder.RootResource;
import com.google.common.base.Objects;

public class RegExpPatternResourceMatcher extends AbstractNotNullMatcher<RootResource> {
	private final Pattern pattern;
	
	public RegExpPatternResourceMatcher(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean matchesSafely(RootResource resource) {
		return pattern.matcher(resource.getRelPath()).matches();
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
			.add("pattern", pattern)
			.toString();
	}
}