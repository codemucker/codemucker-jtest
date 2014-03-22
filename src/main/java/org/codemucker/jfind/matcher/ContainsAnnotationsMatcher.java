package org.codemucker.jfind.matcher;

import java.lang.annotation.Annotation;

import org.codemucker.match.AbstractNotNullMatcher;
import org.codemucker.match.MatchDiagnostics;

public class ContainsAnnotationsMatcher extends AbstractNotNullMatcher<Class<?>> {
	private final Class<? extends Annotation>[] annotations;

	@SafeVarargs
	public ContainsAnnotationsMatcher(Class<? extends Annotation>... annotations) {
        this.annotations = annotations;
    }

	@Override
	@SuppressWarnings("rawtypes")
	public boolean matchesSafely(Class found,MatchDiagnostics diag) {
		for (Class<?> anon : annotations) {
			if (found.getAnnotation(anon) == null) {
				return false;
			}
		}
		return true;
	}
}