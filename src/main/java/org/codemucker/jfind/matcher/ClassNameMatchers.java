package org.codemucker.jfind.matcher;

import java.util.regex.Pattern;

import org.codemucker.match.AString;
import org.codemucker.match.Logical;
import org.codemucker.match.Matcher;

public class ClassNameMatchers { //extends Logical {
	
    public static Matcher<String> any() {
    	return Logical.any();
    }
    
    public static Matcher<String> none() {
    	return Logical.none();
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
    	return AString.withAntPattern(nameAntPattern);
    }

    public Matcher<String> withNamePattern(final String namePattern){
    	return AString.withPattern(namePattern);
    }

    public Matcher<String> withNamePattern(Pattern pattern){
    	return AString.withPattern(pattern);
    }
    
    public Matcher<String> withName(final String name){
    	return AString.equalTo(name);
    }
}
