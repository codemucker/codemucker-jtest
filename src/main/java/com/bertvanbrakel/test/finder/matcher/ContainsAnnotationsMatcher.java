package com.bertvanbrakel.test.finder.matcher;

import java.lang.annotation.Annotation;

import com.bertvanbrakel.lang.matcher.AbstractNotNullMatcher;


public class ContainsAnnotationsMatcher extends AbstractNotNullMatcher<Class<?>> {
	private final Class<? extends Annotation>[] annotations;

	public ContainsAnnotationsMatcher(Class<? extends Annotation>... annotations) {
        this.annotations = annotations;
    }

	@Override
	public boolean matchesSafely(Class found) {
		for (Class<?> anon : annotations) {
			if (found.getAnnotation(anon) == null) {
				return false;
			}
		}
		return true;
	}
}