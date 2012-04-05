package com.bertvanbrakel.test.finder.matcher;

import java.util.regex.Pattern;

import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.util.TestUtils;

public class ResourceMatchers extends LogicalMatchers {
	
    @SuppressWarnings("unchecked")
    public static Matcher<ClassPathResource> any() {
    	return LogicalMatchers.any();
    }
    
    @SuppressWarnings("unchecked")
    public static Matcher<ClassPathResource> none() {
    	return LogicalMatchers.none();
    }
    
	public Matcher<ClassPathResource> withPackage(String packageName) {
		String regExp = "/" + packageName.replace('.', '/') + "/.*";
		return withPath(Pattern.compile(regExp));
	}

	public static Matcher<ClassPathResource> withExtension(String extension) {
		return withAntPath("*." + extension);
	}

	public static Matcher<ClassPathResource> withName(Class<?> classToMatch) {
		String path = '/' + classToMatch.getSimpleName() + "\\.java";
		Package pkg = classToMatch.getPackage();
		if (pkg != null) {
			path = '/' + pkg.getName().replace('.', '/') + path;
		}
		return withPath(Pattern.compile(path));
	}
	
	public static Matcher<ClassPathResource> withName(String antPattern) {
		return withAntPath("*/" + antPattern);
	}

	public static Matcher<ClassPathResource> inPackage(Class<?> classWithPkg) {
		return inPackage(classWithPkg.getPackage());
	}

	public static Matcher<ClassPathResource> inPackage(Package pkg) {
		return withAntPath(pkg.toString().replace('.', '/'));
	}

	public static Matcher<ClassPathResource> withAntPath(String antPattern) {
		return withPath(TestUtils.antExpToPattern(antPattern));
	}

	public static Matcher<ClassPathResource> withPath(Pattern pattern) {
		return new RegExpPatternResourceMatcher(pattern);
	}
}
