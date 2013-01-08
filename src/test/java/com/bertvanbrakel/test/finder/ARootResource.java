package com.bertvanbrakel.test.finder;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.beans.HasPropertyWithValue;

import com.google.common.collect.Lists;

public class ARootResource extends TypeSafeMatcher<RootResource> {

	private List<Matcher<Object>> matchers = Lists.newArrayList();
	
	public static ARootResource with(){
		return new ARootResource();
	}

	public ARootResource pathFromClass(Class<?> klass){
		path(klass.getName().replace('.', '/') + ".java");
		return this;
	}
	
	public ARootResource pathEndsWith(String val){
		path(endsWith(val));
		return this;
	}
	
	public ARootResource path(String val){
		path(equalTo(val));
		return this;
	}
	
	public ARootResource path(Matcher<String> matcher){
		addMatcher("relPath",matcher);
		return this;
	}
	
	private <T> void addMatcher(String propertyName, Matcher<T> propertyMatcher){
		matchers.add(HasPropertyWithValue.hasProperty(propertyName, propertyMatcher));
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
			if(!matchers.get(i).matches(resource)){
				return false;
			}
		}
		return true;
	}
}
