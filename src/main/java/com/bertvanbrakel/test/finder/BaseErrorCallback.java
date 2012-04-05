package com.bertvanbrakel.test.finder;

import com.bertvanbrakel.test.finder.ClassFinder.FinderErrorCallback;

public class BaseErrorCallback implements FinderErrorCallback {

	@Override
    public void onResourceError(ClassPathResource resource, Exception e) {
    }

	@Override
    public void onClassError(String fullClassname, Exception e) {
    }

	@Override
    public void onArchiveError(ClassPathResource archive, Exception e) {
    }

	@Override
    public void onError(Object obj, Exception e) {
    }
}