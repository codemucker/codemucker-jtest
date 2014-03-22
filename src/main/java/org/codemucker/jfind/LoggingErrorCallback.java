package org.codemucker.jfind;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.log4j.Logger;
import org.codemucker.jfind.ClassFinder.FinderErrorCallback;


public class LoggingErrorCallback implements FinderErrorCallback {
	private final Logger logger;
	
	public LoggingErrorCallback(){
		this(Logger.getLogger(LoggingErrorCallback.class));
	}
	
	public LoggingErrorCallback(Logger logger){
		this.logger = checkNotNull(logger, "expect logger");
	}
	
	@Override
	public void onResourceError(RootResource resource, Exception e) {
		logger.warn("error handling resource " + resource,e);
	}
	
	@Override
	public void onClassError(String fullClassname, Exception e) {
		logger.warn("error handling class " + fullClassname,e);
	}
	
	@Override
	public void onArchiveError(RootResource archive, Exception e) {
		logger.warn("error handling archive " + archive,e);
	}

	@Override
    public void onError(Object obj, Exception e) {
    }	
}