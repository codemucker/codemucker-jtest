package org.codemucker.jfind;

import org.codemucker.jfind.ClassFinder.FinderMatchedCallback;

public class BaseMatchedCallback implements FinderMatchedCallback {
	@Override
	public void onArchiveMatched(RootResource archiveFile) {
	}

	@Override
	public void onResourceMatched(RootResource resource) {
	}

	@Override
	public void onClassNameMatched(String className) {
	}

	@Override
	public void onClassPathMatched(Root root) {
	}

	@Override
	public void onClassMatched(Class<?> matchedClass) {
	}

	@Override
    public void onMatched(Object obj) {
    }
}