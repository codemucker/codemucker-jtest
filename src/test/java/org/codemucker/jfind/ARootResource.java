package org.codemucker.jfind;

import java.util.List;

import org.codemucker.match.AProperty;
import org.codemucker.match.AString;
import org.codemucker.match.AbstractNotNullMatcher;
import org.codemucker.match.Description;
import org.codemucker.match.MatchDiagnostics;
import org.codemucker.match.Matcher;

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
	public boolean matchesSafely(RootResource resource,MatchDiagnostics diag) {
		for (int i = 0; i < matchers.size(); i++) {
			if (!matchers.get(i).matches(resource)) {
				return false;
			}
		}
		return true;
	}
}
