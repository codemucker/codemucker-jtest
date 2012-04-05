package com.bertvanbrakel.test.finder;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.log4j.Logger;

import com.bertvanbrakel.test.finder.ClassFinder.FinderMatchedCallback;

public class LoggingMatchedCallback implements FinderMatchedCallback {
	private final Logger logger;
	
	public LoggingMatchedCallback(){
		this(Logger.getLogger(LoggingMatchedCallback.class));
	}
	
	public LoggingMatchedCallback(Logger logger){
		this.logger = checkNotNull(logger, "expect logger");
	}
	
	@Override
	public void onArchiveMatched(ClassPathResource archiveFile) {
		logger.info("matched archive:" + archiveFile);
	}

	@Override
	public void onResourceMatched(ClassPathResource resource) {
		logger.info("matched resource:" + resource);
	}

	@Override
	public void onClassNameMatched(String className) {
		logger.info("matched className:" + className);
	}

	@Override
	public void onClassPathMatched(ClassPathRoot root) {
		logger.info("matched class path root:" + root);
	}

	@Override
	public void onClassMatched(Class<?> matchedClass) {
		logger.info("matched class:" + matchedClass.getName());
	}

	@Override
    public void onMatched(Object obj) {
    }
}