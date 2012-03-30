package com.bertvanbrakel.test.finder.matcher;

public interface Matcher<T> {
	boolean matches(T found);
}
