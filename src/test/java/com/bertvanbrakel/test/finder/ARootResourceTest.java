package com.bertvanbrakel.test.finder;

import org.junit.Assert;
import org.junit.Test;

import com.bertvanbrakel.test.util.ProjectFinder;

public class ARootResourceTest {

	@Test
	public void matchRelPath(){
		Assert.assertTrue(ARootResource.with().path("/a/b/c").matches(new RootResource(newTmpRoot(), "/a/b/c")));
	}
	
	@Test
	public void notMatchRelPath(){
		Assert.assertFalse(ARootResource.with().path("/a/b/c/d").matches(new RootResource(newTmpRoot(), "/a/b/c")));
	}
	
	@Test
	public void matchRelPathEndsWith(){
		Assert.assertTrue(ARootResource.with().pathEndsWith("b/c/d").matches(new RootResource(newTmpRoot(), "/a/b/c/d")));
	}
	
	@Test
	public void notMatchRelPathEndsWith(){
		Assert.assertFalse(ARootResource.with().pathEndsWith("b/c/d").matches(new RootResource(newTmpRoot(), "/a/b/c/d/e")));
	}


	private static DirectoryRoot newTmpRoot() {
		return new DirectoryRoot(ProjectFinder.getDefaultResolver().getTmpDir());
	}
}
