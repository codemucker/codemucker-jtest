package com.bertvanbrakel.test.finder;

import com.bertvanbrakel.test.finder.ClassFinder.FinderFilter;

public class BaseFilter implements FinderFilter {

	@Override
    public boolean isIncludeClassPath(Root root) {
        return true;
    }

	@Override
    public boolean isIncludeDir(RootResource resourceh) {
        return true;
    }

	@Override
    public boolean isIncludeResource(RootResource resource) {
        return true;
    }

	@Override
    public boolean isIncludeClassName(String className) {
        return true;
    }

	@Override
    public boolean isIncludeClass(Class<?> classToMatch) {
        return true;
    }

	@Override
    public boolean isIncludeArchive(RootResource archiveFile) {
        return true;
    }

	@Override
    public boolean isInclude(Object obj) {
	    return true;
    }
}