package com.bertvanbrakel.test.finder;

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.Test;

import com.bertvanbrakel.test.util.TestHelper;

public class ClasspathResourceTest {

	private final boolean NOT_ARCHIVE = false;

	TestHelper helper = new TestHelper();
	
	private ClassPathResource newResource(String relPath) {
		File rootDir;
    	rootDir = new File( "target/temp-files/class-path-root" + UUID.randomUUID().toString());
    	rootDir.getParentFile().mkdirs();
    	rootDir.deleteOnExit();
    	
    	try {
        	File resourceFile = new File(rootDir.getAbsolutePath(), relPath);
        	resourceFile.getParentFile().mkdirs();
        	resourceFile.createNewFile();
        	resourceFile.deleteOnExit();
        	return new ClassPathResource(new ClassPathRoot(rootDir), resourceFile, relPath, NOT_ARCHIVE);
    	} catch (IOException e) {
    		throw new RuntimeException("Error creating tmp resource path " + relPath + " in " + rootDir.getAbsolutePath(), e);
    	}
	}
	
	@Test
	public void testPackagePart() {
		ClassPathResource resource = newResource("/foo/bar/Alice.java");

		assertEquals("foo.bar", resource.getPackagePart());
	}

	@Test
	public void testFileNamePart() {
		ClassPathResource resource = newResource("/foo/bar/Alice.java");

		assertEquals("Alice", resource.getBaseFileNamePart());
	}
	
	@Test
	public void testFileNamePart_noExtension() {
		ClassPathResource resource = newResource("/foo/bar/Alice");
		assertEquals("Alice", resource.getBaseFileNamePart());
	}

	@Test
	public void testExtension() {
		ClassPathResource resource = newResource("/foo/bar/Alice.java");

		assertEquals("java", resource.getExtension());
	}

}
