package com.bertvanbrakel.test.finder.matcher;

public class ClassNameMatchers extends LogicalMatchers {
	
    @SuppressWarnings("unchecked")
    public static Matcher<String> any() {
    	return LogicalMatchers.any();
    }
    
    @SuppressWarnings("unchecked")
    public static Matcher<String> none() {
    	return LogicalMatchers.none();
    }
    
}
