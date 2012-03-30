package com.bertvanbrakel.test.finder.matcher;

import static com.google.common.base.Preconditions.checkNotNull;

import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.ClassPathRoot;

public class ClassPathMatchers extends LogicalMatchers {
	
    @SuppressWarnings("unchecked")
    public static Matcher<ClassPathRoot> any() {
    	return LogicalMatchers.any();
    }
    
    @SuppressWarnings("unchecked")
    public static Matcher<ClassPathRoot> none() {
    	return LogicalMatchers.none();
    }
 
    public static Matcher<ClassPathRoot> resource(Matcher<ClassPathResource> matcher) {
    	return new ResourcMatchereAdapter(matcher);
    }
    
    private static class ResourcMatchereAdapter implements Matcher<ClassPathRoot> {

    	private final Matcher<ClassPathResource> delegate;
    	
    	ResourcMatchereAdapter(Matcher<ClassPathResource> delegate){
    		this.delegate = checkNotNull(delegate);
    	}
    	
		@Override
        public boolean matches(ClassPathRoot root) {
			ClassPathResource cpr = new ClassPathResource(root, root.getPath(), root.getPathName(), false);
			return delegate.matches(cpr);
        }
    	
    }
    
}