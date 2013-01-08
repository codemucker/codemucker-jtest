package com.bertvanbrakel.test.finder;

import com.bertvanbrakel.test.finder.ClassFinder.FinderFilter;
import com.bertvanbrakel.test.finder.matcher.ClassMatchers;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;

public class MatcherBackedFinderFilter implements FinderFilter {
	
	private final Matcher<Root> classPathMatcher;
	private final Matcher<RootResource> resourceMatcher;
	private final Matcher<String> classNameMatcher;
	private final Matcher<Class<?>> classMatcher;
	
	public static MatcherBackedFinderFilter.Builder newBuilder(){
		return new Builder();
	}
	
	private MatcherBackedFinderFilter(
			Matcher<Root> classPathMatcher
			, Matcher<RootResource> resourceMatcher
			, Matcher<String> resourceNameMatcher
			, Matcher<String> classNameMatcher
			, Matcher<Class<?>> classMatcher
			){
		this.classPathMatcher = anyIfNull(classPathMatcher);
		this.resourceMatcher = anyIfNull(resourceMatcher);
		this.classNameMatcher = anyIfNull(classNameMatcher);
		this.classMatcher = anyIfNull(classMatcher);
	}
	
    private <T> Matcher<T> anyIfNull(Matcher<T> matcher){
		return LogicalMatchers.anyIfNull(matcher);
	}
	
	@Override
	public boolean isIncludeResource(RootResource resource) {
		return resourceMatcher.matches(resource);
	}
	
	@Override
	public boolean isIncludeDir(RootResource resource) {
		return true;//fileMatcher.matches(resource);
	}
	
	@Override
	public boolean isIncludeClassPath(Root root) {
		return classPathMatcher.matches(root);
	}
	
	@Override
	public boolean isIncludeClassName(String className) {
		return classNameMatcher.matches(className);
	}
	
	@Override
	public boolean isIncludeClass(Class<?> classToMatch) {
		return classMatcher.matches(classToMatch);
	}
	
	@Override
	public boolean isIncludeArchive(RootResource archiveFile) {
		return resourceMatcher.matches(archiveFile);
	}

	@Override
    public boolean isInclude(Object obj) {
	    return true;
    }
	
	public static class Builder {
		private Matcher<Root> classPathMatcher;
		private Matcher<RootResource> resourceMatcher;
		private Matcher<String> resourceNameMatcher;
		private Matcher<String> classNameMatcher;
		private Matcher<Class<?>> classMatcher;
		
		public MatcherBackedFinderFilter build(){
			return new MatcherBackedFinderFilter(
				classPathMatcher
				, resourceMatcher
				, resourceNameMatcher
				, classNameMatcher
				, classMatcher
			);
		}
		
		public MatcherBackedFinderFilter.Builder setClassMatcher(Matcher<Class<?>> classMatcher) {
        	this.classMatcher = classMatcher;
        	return this;
        }

		public MatcherBackedFinderFilter.Builder setClassPathMatcher(Matcher<Root> classPathMatcher) {
        	this.classPathMatcher = classPathMatcher;
        	return this;
        }
		
		public MatcherBackedFinderFilter.Builder anyClassMatchers(Matcher<Class<?>>... matchers) {
        	this.classMatcher = ClassMatchers.any(matchers);
        	return this;
        }
		
		public MatcherBackedFinderFilter.Builder setClassNameMatcher(Matcher<String> classNameMatcher) {
        	this.classNameMatcher = classNameMatcher;
        	return this;
		}

		public MatcherBackedFinderFilter.Builder setResourceNameMatcher(Matcher<String> fileNameMatcher) {
        	this.resourceNameMatcher = fileNameMatcher;
        	return this;
		}

		public MatcherBackedFinderFilter.Builder setResourceMatcher(Matcher<RootResource> resourceMatcher) {
        	this.resourceMatcher = resourceMatcher;
        	return this;
		}
	}

}