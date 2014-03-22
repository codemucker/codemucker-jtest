package org.codemucker.jfind.matcher;

import java.lang.annotation.Annotation;

import org.codemucker.match.AbstractNotNullMatcher;
import org.codemucker.match.Logical;
import org.codemucker.match.MatchDiagnostics;
import org.codemucker.match.Matcher;

import com.google.common.base.Objects;

public class AClass {// extends Logical {

	public static final Matcher<Class<?>> MATCHER_ANONYMOUS = new AbstractNotNullMatcher<Class<?>>() {
		@Override
		public boolean matchesSafely(Class<?> found,MatchDiagnostics diag) {
			return found.isAnonymousClass();
		}
	};
	
	public static final Matcher<Class<?>> MATCHER_ENUM = new AbstractNotNullMatcher<Class<?>>() {
		@Override
		public boolean matchesSafely(Class<?> found,MatchDiagnostics diag) {
			return found.isEnum();
		}
	};
	
	public static final Matcher<Class<?>> MATCHER_INNER_CLASS = new AbstractNotNullMatcher<Class<?>>() {
		@Override
		public boolean matchesSafely(Class<?> found,MatchDiagnostics diag) {
			return found.isMemberClass();
		}
	};

	public static final Matcher<Class<?>> MATCHER_INTERFACE = new AbstractNotNullMatcher<Class<?>>() {
		@Override
		public boolean matchesSafely(Class<?> found,MatchDiagnostics diag) {
			return found.isInterface();
		}
	};
	
	@SafeVarargs
    public static Matcher<Class<?>> any(final Matcher<Class<?>>... matchers) {
    	return Logical.any(matchers);
    }
	
	@SafeVarargs
    public static Matcher<Class<?>> all(final Matcher<Class<?>>... matchers) {
    	return Logical.all(matchers);
    }
	
    public static Matcher<Class<?>> anyClass() {
    	return Logical.any();
    }
    
    public static Matcher<Class<?>> noClass() {
    	return Logical.none();
    }
	
    public static Matcher<Class<?>> withModifier(final int modifier){
    	return new AbstractNotNullMatcher<Class<?>>(){
			@Override
            public boolean matchesSafely(Class<?> found,MatchDiagnostics diag) {
	            return (found.getModifiers() & modifier) != 0;
            }
    	};
    }
    
    @SafeVarargs
	public static Matcher<Class<?>> assignableTo(final Class<?>... superclass) {
		return new AbstractNotNullMatcher<Class<?>>() {
			@Override
			public boolean matchesSafely(Class<?> found,MatchDiagnostics diag) {
				for (Class<?> require : superclass) {
					if (!require.isAssignableFrom(found)) {
						return false;
					}
				}
				return true;
			}
			
			@Override
			public String toString(){
				return Objects.toStringHelper(this)
					.add("superClassesMatching", superclass)
					.toString();
			}
		};
	}
	
	@SafeVarargs
	public static Matcher<Class<?>> withAnnotation(Class<? extends Annotation>... annotations){
		return new ContainsAnnotationsMatcher(annotations);
	}
	
	public static Matcher<Class<?>> excludeEnum() {
		return Logical.not(MATCHER_ENUM);
	}

	public static Matcher<Class<?>> excludeAnonymous() {
		return Logical.not(MATCHER_ANONYMOUS);
	}

	public static Matcher<Class<?>> excludeInner() {
		return Logical.not(MATCHER_INNER_CLASS);
	}

	public static Matcher<Class<?>> excludeInterfaces() {
		return Logical.not(MATCHER_INTERFACE);
	}

	public static Matcher<Class<?>> includeEnum() {
		return MATCHER_ENUM;
	}

	public static Matcher<Class<?>> includeAnonymous() {
		return MATCHER_ANONYMOUS;
	}

	public static Matcher<Class<?>> includeInner() {
		return MATCHER_INNER_CLASS;
	}

	public static Matcher<Class<?>> includeInterfaces() {
		return MATCHER_INTERFACE;
	}
}
