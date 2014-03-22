package org.codemucker.jfind;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.codemucker.jtest.TestHelper;
import org.junit.Test;


public class ClasspathResourceTest {

	TestHelper helper = new TestHelper();
	
	private RootResource newResource(String relPath) {
		File rootDir;
    	rootDir = new File( "target/temp-files/class-path-root" + UUID.randomUUID().toString());
    	rootDir.getParentFile().mkdirs();
    	rootDir.deleteOnExit();
    	
    	try {
        	File resourceFile = new File(rootDir.getAbsolutePath(), relPath);
        	resourceFile.getParentFile().mkdirs();
        	resourceFile.createNewFile();
        	resourceFile.deleteOnExit();
        	return new RootResource(new DirectoryRoot(rootDir), relPath);
    	} catch (IOException e) {
    		throw new RuntimeException("Error creating tmp resource path " + relPath + " in " + rootDir.getAbsolutePath(), e);
    	}
	}
	
	@Test
	public void testPackagePart() {
		RootResource resource = newResource("/foo/bar/Alice.java");

		assertEquals("foo.bar", resource.getPackagePart());
	}

	@Test
	public void testFileNamePart() {
		RootResource resource = newResource("/foo/bar/Alice.java");

		assertEquals("Alice", resource.getBaseFileNamePart());
	}
	
	@Test
	public void testFileNamePart_noExtension() {
		RootResource resource = newResource("/foo/bar/Alice");
		assertEquals("Alice", resource.getBaseFileNamePart());
	}

	@Test
	public void testExtension() {
		RootResource resource = newResource("/foo/bar/Alice.java");

		assertEquals("java", resource.getExtension());
	}

}
