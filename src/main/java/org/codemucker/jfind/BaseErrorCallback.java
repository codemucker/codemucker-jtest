package org.codemucker.jfind;

import org.codemucker.jfind.ClassFinder.FinderErrorCallback;

public class BaseErrorCallback implements FinderErrorCallback {

	@Override
    public void onResourceError(RootResource resource, Exception e) {
    }

	@Override
    public void onClassError(String fullClassname, Exception e) {
    }

	@Override
    public void onArchiveError(RootResource archive, Exception e) {
    }

	@Override
    public void onError(Object obj, Exception e) {
    }
}