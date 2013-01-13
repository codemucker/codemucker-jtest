package com.bertvanbrakel.test.finder.matcher;

import java.util.regex.Pattern;

import com.bertvanbrakel.lang.matcher.Logical;
import com.bertvanbrakel.lang.matcher.Matcher;
import com.bertvanbrakel.test.finder.RootResource;
import com.bertvanbrakel.test.util.TestUtils;

public class ResourceMatchers extends Logical {
	
    @SuppressWarnings("unchecked")
    public static Matcher<RootResource> any() {
    	return Logical.any();
    }
    
    @SuppressWarnings("unchecked")
    public static Matcher<RootResource> none() {
    	return Logical.none();
    }
    
	public Matcher<RootResource> withPackage(String packageName) {
		String regExp = "/" + packageName.replace('.', '/') + "/.*";
		return withPath(Pattern.compile(regExp));
	}

	public static Matcher<RootResource> withExtension(String extension) {
		return withAntPath("*." + extension);
	}

	public static Matcher<RootResource> withName(Class<?> classToMatch) {
		String path = '/' + classToMatch.getSimpleName() + "\\.java";
		Package pkg = classToMatch.getPackage();
		if (pkg != null) {
			path = '/' + pkg.getName().replace('.', '/') + path;
		}
		return withPath(Pattern.compile(path));
	}
	
	public static Matcher<RootResource> withName(String antPattern) {
		return withAntPath("*/" + antPattern);
	}

	public static Matcher<RootResource> inPackag(Class<?> classWithPkg) {
		return inPackage(classWithPkg.getPackage());
	}

	public static Matcher<RootResource> inPackage(Package pkg) {
		return withAntPath(pkg.toString().replace('.', '/'));
	}

	public static Matcher<RootResource> withAntPath(String antPattern) {
		return withPath(TestUtils.antExpToPattern(antPattern));
	}

	public static Matcher<RootResource> withPath(Pattern pattern) {
		return new RegExpPatternResourceMatcher(pattern);
	}
}
