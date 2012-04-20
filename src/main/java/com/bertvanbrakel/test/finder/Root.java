package com.bertvanbrakel.test.finder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Root {

	public static enum TYPE {
    	MAIN_SRC,TEST_SRC,MAIN_COMPILE,TEST_COMPILE,DEPENDENCY,GENERATED_SRC,GENERATED_COMPILE,UNKNOWN;
    }

	InputStream getResourceInputStream(String relPath) throws IOException;
	OutputStream getResourceOutputStream(String relPath) throws IOException;

	String getPathName();

	boolean isTypeKnown();

	TYPE getType();

	//TODO:remove at some point. The root should manage walking, reading,writing resources
	boolean isDirectory();

	//TODO:remove at some point. The root should manage walking, reading,writing resources
	boolean isArchive();


}
