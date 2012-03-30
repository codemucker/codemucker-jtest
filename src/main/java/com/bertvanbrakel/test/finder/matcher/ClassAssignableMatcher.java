package com.bertvanbrakel.test.finder.matcher;


public class ClassAssignableMatcher implements Matcher<Class<?>> {
	private final Class<?>[] superclass;

	public ClassAssignableMatcher(Class<?>... superclass) {
        super();
        this.superclass = superclass;
    }

	@Override
	public boolean matches(Class<?> found) {
		for (Class<?> require : superclass) {
			if (!require.isAssignableFrom(found)) {
				return false;
			}
		}
		return true;
	}
}