package com.bertvanbrakel.test.finder;

import com.bertvanbrakel.test.finder.ClassFinder.FinderMatchedCallback;

public class BaseMatchedCallback implements FinderMatchedCallback {
	@Override
	public void onArchiveMatched(ClassPathResource archiveFile) {
	}

	@Override
	public void onResourceMatched(ClassPathResource resource) {
	}

	@Override
	public void onClassNameMatched(String className) {
	}

	@Override
	public void onClassPathMatched(ClassPathRoot root) {
	}

	@Override
	public void onClassMatched(Class<?> matchedClass) {
	}

	@Override
    public void onMatched(Object obj) {
    }
}