package com.bertvanbrakel.test.finder.matcher;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public class LogicalMatchers {

	private static final Matcher<Object> MATCHER_ANY = new Matcher<Object>() {
		@Override
		public boolean matches(Object found) {
			return true;
		}
	};
	
	private static final Matcher<Object> MATCHER_NONE = new Matcher<Object>() {
		@Override
		public boolean matches(Object found) {
			return true;
		}
	};

	/**
     * Synonym for {@link #and(Matcher...)}
     */
    public static <T> Matcher<T> all(final Matcher<T>... matchers) {
    	return and(matchers);
    }

    public static <I extends Iterable<Matcher<T>>,T> Matcher<T> all(final I matchers) {
    	return and(matchers);
    }
   
    public static <I extends Iterable<Matcher<T>>,T> Matcher<T> and(final I matchers) {
    	return new MatcherAnd<T>(matchers);   
    }
	public static <T> Matcher<T> and(final Matcher<T>... matchers) {
    	return new MatcherAnd<T>(matchers);
    }

	/**
     * Synonym for {@link #or(Matcher...)}
     */
    public static <T> Matcher<T> either(final Matcher<T>... matchers) {
    	return or(matchers);
    }

    @SuppressWarnings("unchecked")
    public static <T> Matcher<T> any() {
    	return (Matcher<T>) MATCHER_ANY;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Matcher<T> none() {
    	return (Matcher<T>) MATCHER_NONE;
    }
    
    /**
     * Synonym for {@link #or(Matcher...)}
     */
    public static <T> Matcher<T> any(final Matcher<T>... matchers) {
    	return or(matchers);
    }

    public static <I extends Iterable<Matcher<T>>,T> Matcher<T> any(final I matchers) {
    	return or(matchers);
    }
    
	public static <T> Matcher<T> or(final Matcher<T>... matchers) {
    	return new MatcherOr<T>(matchers);
    }
	
	public static <I extends Iterable<Matcher<T>>,T> Matcher<T> or(final I matchers) {
    	return new MatcherOr<T>(matchers);
    }
	
	public static <T> Matcher<T> not(final Matcher<T> matcher) {
    	return new Matcher<T>() {
    		@Override
    		public boolean matches(T found) {
    			return !matcher.matches(found);
    		}
    		@Override
    		public String toString(){
    			return Objects.toStringHelper("MatchNot").add("matcher", matcher).toString();
    		}
    	};
    }
	
	private static class MatcherOr<T> implements Matcher<T> {
	    private final ImmutableCollection<Matcher<T>> matchers;

	    private MatcherOr(Iterable<Matcher<T>> matchers) {
        	this.matchers = ImmutableList.copyOf(matchers);		    
	    }
       
	    private MatcherOr(Matcher<T>[] matchers) {
		    this.matchers = ImmutableList.copyOf(matchers);
	    }

	    @Override
	    public boolean matches(T found) {
	    	for(Matcher<T> matcher:matchers){
	    		if( matcher.matches(found)){
	    			return true;
	    		}
	    	}
	    	return false;
	    }

	    @Override
	    public String toString(){
	    	return Objects.toStringHelper("MatchAny").add("matchers", matchers).toString();
	    }
    }

	private static class MatcherAnd<T> implements Matcher<T> {
	    private final ImmutableCollection<Matcher<T>> matchers;

        private MatcherAnd(Iterable<Matcher<T>> matchers) {
        	this.matchers = ImmutableList.copyOf(matchers);		    
	    }
       
	    private MatcherAnd(Matcher<T>[] matchers) {
		    this.matchers = ImmutableList.copyOf(matchers);
	    }

	    @Override
	    public boolean matches(T found) {
	    	for(Matcher<T> matcher:matchers){
	    		if( !matcher.matches(found)){
	    			return false;
	    		}
	    	}
	    	return true;
	    }

	    @Override
	    public String toString(){
	    	return Objects.toStringHelper("MatchAll").add("matchers", matchers).toString();
	    }
    }
}
