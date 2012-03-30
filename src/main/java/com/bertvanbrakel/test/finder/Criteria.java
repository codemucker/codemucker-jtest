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

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.regex.Pattern;

import com.bertvanbrakel.test.finder.ClassFinder.Builder;
import com.bertvanbrakel.test.finder.ClassFinder.FinderErrorHandler;
import com.bertvanbrakel.test.finder.ClassFinder.FinderFindCallback;
import com.bertvanbrakel.test.finder.ClassFinder.FinderIgnoredCallback;
import com.bertvanbrakel.test.finder.matcher.ClassMatchers;
import com.bertvanbrakel.test.finder.matcher.FileMatchers;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;

public class Criteria {
	
	private final Collection<Matcher<ClassPathResource>> excludeFileNameMatchers = newArrayList();
	private final Collection<Matcher<ClassPathResource>> includeFileNameMatchers = newArrayList();
	
	private final Collection<Matcher<Class<?>>> includeClassMatchers = newArrayList();
	private final Collection<Matcher<Class<?>>> excludeClassMatchers = newArrayList();
	
	private boolean includeClassesDir = true;
	private boolean includeTestDir = false;
	private boolean includeClasspath = false;
	
	private ClassFinder.Builder builder = ClassFinder.newBuilder();

	public ClassFinder build() {
		Builder copy = builder.copyOf();
		copy.setClassMatcher(toClassMatcher());
		// .set(toResourceMatcher())
		copy.setClassPathResourceMatcher(toResourceMatcher());
		if (includeClassesDir) {
			copy.setIncludeProjectCompileDir();
		}
		if (includeClasspath) {
			copy.setIncludeClassPath();
		}
		if (includeTestDir) {
			copy.setIncludeProjectTestDir();
		}
		return copy.build();
	}

	public Criteria setIgnoreCallback(FinderIgnoredCallback callback){
		builder.setCallbackOnIgnored(callback);
		return this;
	}
	
	public Criteria setMatchCallback(FinderFindCallback callback){
		builder.setCallbackOnMatched(callback);
		return this;
	}
	
	public Criteria setErrorCallback(FinderErrorHandler callback){
		builder.setCallbackOnError(callback);
		return this;
	}

	public Criteria classLoader(ClassLoader classLoader) {
    	builder.setClassLoader(classLoader);
    	return this;
    }

	public Criteria includeClassesDir(boolean b) {
		this.includeClassesDir = b;
		return this;
	}

	public Criteria includeTestDir(boolean b) {
		this.includeTestDir = b;
		return this;
	}

	public Criteria setIncludeClasspath(boolean includeClasspath) {
    	this.includeClasspath = includeClasspath;
    	return this;
    }

	public Criteria addClassPath(File dir) {
		builder.addClassPathDir(dir);
		return this;
	}

	public Criteria excludeFileName(String path) {
		excludeFileName(FileMatchers.withAntPath(path));
		return this;
	}
	
	public Criteria excludeFileName(Pattern pattern) {
		excludeFileName(FileMatchers.withPath(pattern));
		return this;
	}

	public Criteria excludeFileName(Matcher<ClassPathResource> matcher) {
		this.excludeFileNameMatchers.add(matcher);
		return this;
	}

	public Criteria includeFileName(String pattern) {
		includeFileName(FileMatchers.withAntPath(pattern));
		return this;
	}

	public Criteria includeFileName(Pattern pattern) {
		includeFileName(FileMatchers.withPath(pattern));
		return this;
	}
	
	public Criteria includeFileName(Matcher<ClassPathResource> matcher) {
		this.includeFileNameMatchers.add(matcher);
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
		this.includeClassMatchers.add(matcher);
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
		this.excludeClassMatchers.add(matcher);
		return this;
	}
	
	public Matcher<ClassPathResource> toResourceMatcher() {
		return combine(includeFileNameMatchers,excludeFileNameMatchers);
	}
	
	public Matcher<String> toClassNameMatcher(){
		return LogicalMatchers.any();//combine(includeClassNameMatchers,excludeClassNameMatchers);
	}

	public Matcher<Class<?>> toClassMatcher() {		
		return combine(includeClassMatchers,excludeClassMatchers);
	}
	
	private <T> Matcher<T> combine(Collection<Matcher<T>>includes,Collection<Matcher<T>> excludes){
		if( includes.size()>0 && excludes.size() > 0) {
			return LogicalMatchers.all(
					LogicalMatchers.any(includes)
					, LogicalMatchers.not(LogicalMatchers.all(excludes))
			);	
		} else if( excludes.size() > 0){
			return LogicalMatchers.not(LogicalMatchers.any(excludes));
		} else if( includes.size() > 0){
			return LogicalMatchers.any(includes);
		} else {
			return LogicalMatchers.any();
		}
	}
}