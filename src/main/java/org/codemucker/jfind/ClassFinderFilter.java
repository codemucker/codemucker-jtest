package org.codemucker.jfind;

import org.codemucker.jfind.ClassFinder.FinderFilter;
import org.codemucker.jfind.matcher.AClass;
import org.codemucker.match.Logical;
import org.codemucker.match.Matcher;

public class ClassFinderFilter implements FinderFilter {
	
	private final Matcher<Root> classPathMatcher;
	private final Matcher<RootResource> resourceMatcher;
	private final Matcher<String> classNameMatcher;
	private final Matcher<Class<?>> classMatcher;
	
	public static ClassFinderFilter.Builder newBuilder(){
		return new Builder();
	}
	
	private ClassFinderFilter(
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
		return Logical.anyIfNull(matcher);
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
		
		public ClassFinderFilter build(){
			return new ClassFinderFilter(
				classPathMatcher
				, resourceMatcher
				, resourceNameMatcher
				, classNameMatcher
				, classMatcher
			);
		}
		
		public Builder setClassMatcher(Matcher<Class<?>> classMatcher) {
        	this.classMatcher = classMatcher;
        	return this;
        }

		public Builder setClassPathMatcher(Matcher<Root> classPathMatcher) {
        	this.classPathMatcher = classPathMatcher;
        	return this;
        }
		
		public Builder anyClassMatchers(Matcher<Class<?>>... matchers) {
        	this.classMatcher = AClass.any(matchers);
        	return this;
        }
		
		public Builder setClassNameMatcher(Matcher<String> classNameMatcher) {
        	this.classNameMatcher = classNameMatcher;
        	return this;
		}

		public Builder setResourceNameMatcher(Matcher<String> fileNameMatcher) {
        	this.resourceNameMatcher = fileNameMatcher;
        	return this;
		}

		public Builder setResourceMatcher(Matcher<RootResource> resourceMatcher) {
        	this.resourceMatcher = resourceMatcher;
        	return this;
		}
	}

}