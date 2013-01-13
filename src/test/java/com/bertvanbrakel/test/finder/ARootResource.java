package com.bertvanbrakel.test.finder;

import java.util.List;

import com.bertvanbrakel.lang.matcher.AProperty;
import com.bertvanbrakel.lang.matcher.AString;
import com.bertvanbrakel.lang.matcher.AbstractNotNullMatcher;
import com.bertvanbrakel.lang.matcher.Description;
import com.bertvanbrakel.lang.matcher.Matcher;
import com.google.common.collect.Lists;

public class ARootResource extends AbstractNotNullMatcher<RootResource> {

	private List<Matcher<RootResource>> matchers = Lists.newArrayList();
	
	public static ARootResource with(){
		return new ARootResource();
	}

	public ARootResource pathFromClass(Class<?> klass){
		path(klass.getName().replace('.', '/') + ".java");
		return this;
	}
	
	public ARootResource pathEndsWith(String val){
		path(AString.endingWith(val));
		return this;
	}
	
	public ARootResource path(String val){
		path(AString.equalTo(val));
		return this;
	}
	
	public ARootResource path(Matcher<String> matcher){
		addMatcher("relPath",matcher);
		return this;
	}
	
	private <T> void addMatcher(String propertyName, Matcher<T> propertyMatcher){
		matchers.add(AProperty.on(RootResource.class).named(propertyName).equals(propertyMatcher));
	}

	@Override
	public void describeTo(Description d) {
		for (int i = 0; i < matchers.size(); i++) {
			matchers.get(i).describeTo(d);
		}
	}

	@Override
	public boolean matchesSafely(RootResource resource) {
		for (int i = 0; i < matchers.size(); i++) {
			if (!matchers.get(i).matches(resource)) {
				return false;
			}
		}
		return true;
	}
}
