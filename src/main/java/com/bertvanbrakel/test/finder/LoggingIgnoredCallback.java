package com.bertvanbrakel.test.finder;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.log4j.Logger;

import com.bertvanbrakel.test.finder.ClassFinder.FinderIgnoredCallback;

public class LoggingIgnoredCallback implements FinderIgnoredCallback {
	private final Logger logger;
	
	public LoggingIgnoredCallback(){
		this(Logger.getLogger(LoggingErrorCallback.class));
	}
	
	public LoggingIgnoredCallback(Logger logger){
		this.logger = checkNotNull(logger, "expect logger");
	}
	
	@Override
    public void onArchiveIgnored(ClassPathResource archiveFile) {
		logger.info("ignoring archive:" + archiveFile);
    }

	@Override
    public void onResourceIgnored(ClassPathResource resource) {
		logger.info("ignoring resource:" + resource);
	}

	@Override
    public void onClassNameIgnored(String className) {
		logger.info("ignoring class named:" + className);
	}

	@Override
    public void onClassIgnored(Class<?> ignoredClass) {
		logger.info("ignoring class:" + ignoredClass.getName());
		
	}

	@Override
    public void onClassPathIgnored(ClassPathRoot root) {
		logger.info("ignoring class path:" + root);
	}

	@Override
    public void onIgnored(Object obj) {
    }
}