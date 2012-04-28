package com.bertvanbrakel.test.finder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Function;

public interface Root {

	public static enum RootType {
    	MAIN_SRC
    	, TEST_SRC
    	, MAIN_COMPILE
    	, TEST_COMPILE
    	, DEPENDENCY
    	, GENERATED_SRC
    	, GENERATED_COMPILE
    	, UNKNOWN;
    }

	/**
	 * Return a stream to read the given relative stream from
	 * @param relPath
	 * @return
	 * @throws IOException
	 */
	InputStream getResourceInputStream(String relPath) throws IOException;
	
	/**
	 * Return a stream to write to the given resource
	 * @param relPath
	 * @return
	 * @throws IOException if it was not possible to write to the given resource for any reason. This
	 * could include not having permissions, this root does not support writing
	 */
	OutputStream getResourceOutputStream(String relPath) throws IOException;

	public boolean canWriteResource(String relPath);
	public boolean canReadReource(String relPath);
	
	String getPathName();

	boolean isTypeKnown();

	RootType getType();

	void walkResources(Function<ClassPathResource, Boolean> callback);
}
