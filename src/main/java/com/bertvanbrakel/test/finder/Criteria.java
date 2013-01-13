/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bertvanbrakel.test.finder;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import com.bertvanbrakel.lang.matcher.Matcher;
import com.bertvanbrakel.test.finder.ClassFinder.Builder;
import com.bertvanbrakel.test.finder.ClassFinder.FinderErrorCallback;
import com.bertvanbrakel.test.finder.ClassFinder.FinderFilter;
import com.bertvanbrakel.test.finder.ClassFinder.FinderIgnoredCallback;
import com.bertvanbrakel.test.finder.ClassFinder.FinderMatchedCallback;
import com.bertvanbrakel.test.finder.matcher.ClassMatchers;
import com.bertvanbrakel.test.finder.matcher.IncludeExcludeMatcherBuilder;
import com.bertvanbrakel.test.finder.matcher.ResourceMatchers;

public class Criteria {
	
	private final Roots.Builder classPathBuilder = Roots.builder();
	
	private final IncludeExcludeMatcherBuilder<RootResource> resources = IncludeExcludeMatcherBuilder.builder();
	private final IncludeExcludeMatcherBuilder<Class<?>> classes = IncludeExcludeMatcherBuilder.builder();
	private final IncludeExcludeMatcherBuilder<String> classNames = IncludeExcludeMatcherBuilder.builder();
	private final IncludeExcludeMatcherBuilder<String> resourceNames = IncludeExcludeMatcherBuilder.builder();
	
	private ClassFinder.Builder builder = ClassFinder.newBuilder();

	public ClassFinder build() {
		Builder copy = builder.copyOf();
		
		FinderFilter filter = MatcherBackedFinderFilter.newBuilder()
			.setClassMatcher(classes.build())
			.setResourceMatcher(resources.build())
			.setClassNameMatcher(classNames.build())
			.setResourceNameMatcher(resourceNames.build())
			.build();
		copy.setFilter(filter);
		copy.setSearchClassPaths(classPathBuilder.build());
		
		
		return copy.build();
	}

	public Criteria setIgnoreCallback(FinderIgnoredCallback callback){
		builder.setIgnoredCallback(callback);
		return this;
	}
	
	public Criteria setMatchCallback(FinderMatchedCallback callback){
		builder.setMatchedCallback(callback);
		return this;
	}
	
	public Criteria setErrorCallback(FinderErrorCallback callback){
		builder.setErrorCallback(callback);
		return this;
	}

	public Criteria setClassLoader(ClassLoader classLoader) {
    	builder.setClassLoader(classLoader);
    	return this;
    }

	public Criteria setIncludeClassesDir(boolean b) {
		classPathBuilder.setIncludeMainSrcDir(b);
		return this;
	}

	public Criteria setIncludeTestDir(boolean b) {
		classPathBuilder.setIncludeTestSrcDir(b);
		return this;
	}

	public Criteria setIncludeClasspath(boolean b) {
		classPathBuilder.setIncludeClasspath(b);
    	return this;
    }

	public Criteria addClassPath(File dir) {
		classPathBuilder.addClassPathDir(dir);
		return this;
	}

	public Criteria excludeFileName(String path) {
		excludeFileName(ResourceMatchers.withAntPath(path));
		return this;
	}
	
	public Criteria excludeFileName(Pattern pattern) {
		excludeFileName(ResourceMatchers.withPath(pattern));
		return this;
	}

	public Criteria excludeFileName(Matcher<RootResource> matcher) {
		resources.addExclude(matcher);
		return this;
	}

	public Criteria includeFileName(String pattern) {
		includeFileName(ResourceMatchers.withAntPath(pattern));
		return this;
	}

	public Criteria includeFileName(Pattern pattern) {
		includeFileName(ResourceMatchers.withPath(pattern));
		return this;
	}
	
	public Criteria includeFileName(Matcher<RootResource> matcher) {
		resources.addInclude(matcher);
		return this;
	}
	
	public Criteria assignableTo(Class<?>... superclass) {
		includeClassMatching(ClassMatchers.assignableTo(superclass));
		return this;
	}
	
	public <T extends Annotation> Criteria withAnnotation(Class<T>... annotations){
		includeClassMatching(ClassMatchers.withAnnotation(annotations));
		return this;
	}
	
	public Criteria includeClassMatching(Matcher<Class<?>> matcher) {
		classes.addInclude(matcher);
		return this;
	}
	
	public Criteria excludeEnum() {
		excludeClassMatching(ClassMatchers.includeEnum());
		return this;
	}

	public Criteria excludeAnonymous() {
		excludeClassMatching(ClassMatchers.includeAnonymous());
		return this;
	}

	public Criteria excludeInner() {
		excludeClassMatching(ClassMatchers.includeInner());
		return this;
	}

	public Criteria excludeInterfaces() {
		excludeClassMatching(ClassMatchers.includeInterfaces());
		return this;
	}

	public Criteria excludeClassMatching(Matcher<Class<?>> matcher) {
		classes.addExclude(matcher);
		return this;
	}

}