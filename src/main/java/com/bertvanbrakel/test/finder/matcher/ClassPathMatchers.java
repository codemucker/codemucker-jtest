package com.bertvanbrakel.test.finder.matcher;

import static com.google.common.base.Preconditions.checkNotNull;

import com.bertvanbrakel.test.finder.RootResource;
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
 
    public static Matcher<Root> resource(Matcher<RootResource> matcher) {
    	return new ResourcMatchereAdapter(matcher);
    }
    
    private static class ResourcMatchereAdapter implements Matcher<Root> {

    	private final Matcher<RootResource> delegate;
    	
    	ResourcMatchereAdapter(Matcher<RootResource> delegate){
    		this.delegate = checkNotNull(delegate);
    	}
    	
		@Override
        public boolean matches(Root root) {
			RootResource cpr = new RootResource(root, root.getPathName());
			return delegate.matches(cpr);
        }
    	
    }
    
}