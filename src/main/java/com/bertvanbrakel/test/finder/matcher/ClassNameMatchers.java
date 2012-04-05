package com.bertvanbrakel.test.finder.matcher;

import java.util.regex.Pattern;

import com.google.common.base.Objects;

public class ClassNameMatchers extends LogicalMatchers {
	
    @SuppressWarnings("unchecked")
    public static Matcher<String> any() {
    	return LogicalMatchers.any();
    }
    
    @SuppressWarnings("unchecked")
    public static Matcher<String> none() {
    	return LogicalMatchers.none();
    }

    public Matcher<String> withExactPackage(String packageName) {
		String regExp = packageName.replaceAll("\\.", "\\.") + "\\.[^.]*";
		return withNamePattern(regExp);
	}
    
    public Matcher<String> withPackageStartingWith(String packageName) {
		String regExp = packageName.replaceAll("\\.", "\\.") + "\\..*";
		return withNamePattern(regExp);
	}
    
    public Matcher<String> withNameAntPattern(final String nameAntPattern){
    	return RegExpMatcher.withAntPattern(nameAntPattern);
    }

    public Matcher<String> withNamePattern(final String namePattern){
    	return RegExpMatcher.withPattern(namePattern);
    }

    public Matcher<String> withNamePattern(Pattern pattern){
    	return RegExpMatcher.withPattern(pattern);
    }
    
    public Matcher<String> withName(final String name){
    	return new Matcher<String>(){
			@Override
            public boolean matches(String found) {
	            return name.equals(found);
            }
			
			@Override
			public String toString(){
				return Objects.toStringHelper(this)
					.add("name", name)
					.toString();
			}
    	};
    }
}
