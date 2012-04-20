package com.bertvanbrakel.test.finder.matcher;

import static com.google.common.base.Preconditions.checkNotNull;

import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.Root;

public class ClassPathMatchers extends LogicalMatchers {
	
    @SuppressWarnings("unchecked")
    public static Matcher<Root> any() {
    	return LogicalMatchers.any();
    }
    
    @SuppressWarnings("unchecked")
    public static Matcher<Root> none() {
    	return LogicalMatchers.none();
    }
 
    public static Matcher<Root> resource(Matcher<ClassPathResource> matcher) {
    	return new ResourcMatchereAdapter(matcher);
    }
    
    private static class ResourcMatchereAdapter implements Matcher<Root> {

    	private final Matcher<ClassPathResource> delegate;
    	
    	ResourcMatchereAdapter(Matcher<ClassPathResource> delegate){
    		this.delegate = checkNotNull(delegate);
    	}
    	
		@Override
        public boolean matches(Root root) {
			ClassPathResource cpr = new ClassPathResource(root, root.getPathName());
			return delegate.matches(cpr);
        }
    	
    }
    
}