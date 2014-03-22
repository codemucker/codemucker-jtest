package org.codemucker.jfind.matcher;

import java.util.regex.Pattern;

import org.codemucker.jfind.RootResource;
import org.codemucker.match.AString;
import org.codemucker.match.AbstractMatcher;
import org.codemucker.match.Description;
import org.codemucker.match.Logical;
import org.codemucker.match.MatchDiagnostics;
import org.codemucker.match.Matcher;

import com.google.common.base.Preconditions;

public class ResourceMatchers { //extends Logical {
	
    public static Matcher<RootResource> any() {
    	return Logical.any();
    }
    
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
		return withPath(AString.withAntPattern(antPattern));
	}

	public static Matcher<RootResource> withPath(Pattern pattern) {
		return withPath(AString.withPattern(pattern));
	}
	
	public static Matcher<RootResource> withPath(Matcher<String> pathMatcher) {
		return new ResourcePathMatcher(pathMatcher);
	}
	
	
	private static class ResourcePathMatcher extends AbstractMatcher<RootResource>{
		private final Matcher<String> pathMatcher;

		ResourcePathMatcher(Matcher<String> pathMatcher){
			this.pathMatcher = Preconditions.checkNotNull(pathMatcher,"null path matcher");
		}

		@Override
		public boolean matchesSafely(RootResource actual,MatchDiagnostics diag) {
			return actual != null && diag.TryMatch(actual.getRelPath(), pathMatcher);
		}
		
		@Override
		public void describeTo(Description desc) {
			super.describeTo(desc);
			desc.text("not null resouce");
			desc.value("relPath", pathMatcher);
		}
		
	}
}
