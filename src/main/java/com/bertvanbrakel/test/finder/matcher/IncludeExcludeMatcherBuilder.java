package com.bertvanbrakel.test.finder.matcher;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;

public final class IncludeExcludeMatcherBuilder<T> {

	private final Collection<Matcher<T>> excludes = newArrayList();
	private final Collection<Matcher<T>> includes= newArrayList();
	
	public static <T> IncludeExcludeMatcherBuilder<T> newBuilder(){
		return new IncludeExcludeMatcherBuilder<T>();
	}
	
	@SuppressWarnings("unchecked")
    public Matcher<T> build(){
		if( includes.size() > 0 && excludes.size() > 0) {
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
	
	public IncludeExcludeMatcherBuilder<T> copyOf(){
		IncludeExcludeMatcherBuilder<T> copy = new IncludeExcludeMatcherBuilder<T>();
		copy.includes.addAll(includes);
		copy.excludes.addAll(excludes);
		return copy;
	}
	
	public IncludeExcludeMatcherBuilder<T> addInclude(Matcher<T> matcher){
		includes.add(matcher);
		return this;
	}
	
	public IncludeExcludeMatcherBuilder<T> setIncludes(Iterable<Matcher<T>> matchers){
		clearIncludes();
		addIncludes(matchers);
		return this;
	}
	
	public IncludeExcludeMatcherBuilder<T> addIncludes(Iterable<Matcher<T>> matchers){
		addAll(includes, matchers);
		return this;
	}
	
	public IncludeExcludeMatcherBuilder<T> addExclude(Matcher<T> matcher){
		excludes.add(matcher);
		return this;
	}
	
	public IncludeExcludeMatcherBuilder<T> setExcludes(Iterable<Matcher<T>> matchers){
		clearExcludes();
		addExcludes(matchers);
		return this;
	}
	
	public IncludeExcludeMatcherBuilder<T> addExcludes(Iterable<Matcher<T>> matchers){
		addAll(excludes, matchers);
		return this;
	}

	public IncludeExcludeMatcherBuilder<T> clearAll(){
		clearIncludes();
		clearExcludes();
		return this;
	}
	
	public IncludeExcludeMatcherBuilder<T> clearIncludes(){
		includes.clear();
		return this;
	}
	
	public IncludeExcludeMatcherBuilder<T> clearExcludes(){
		includes.clear();
		return this;
	}
	
	private static <T> void addAll(Collection<T> col, Iterable<T> iter){
		for( T item:iter){
			col.add(item);
		}
	}
}
