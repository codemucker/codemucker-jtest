package com.bertvanbrakel.test.finder.matcher;

import java.lang.annotation.Annotation;


public class ContainsAnnotationsMatcher implements Matcher<Class<?>> {
	private final Class<? extends Annotation>[] annotations;

	public ContainsAnnotationsMatcher(Class<? extends Annotation>... annotations) {
        super();
        this.annotations = annotations;
    }

	@Override
	public boolean matches(Class found) {
		for (Class<?> anon : annotations) {
			if (found.getAnnotation(anon) == null) {
				return false;
			}
		}
		return true;
	}
}