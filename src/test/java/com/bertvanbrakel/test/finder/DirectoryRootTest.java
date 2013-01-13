package com.bertvanbrakel.test.finder;

import static com.bertvanbrakel.lang.matcher.Assert.assertThat;
import static com.bertvanbrakel.lang.matcher.Assert.is;

import java.io.File;
import java.util.Collection;

import org.junit.Test;

import com.bertvanbrakel.lang.matcher.AList;
import com.bertvanbrakel.test.finder.a.TstBeanOne;
import com.bertvanbrakel.test.finder.b.TstBeanTwo;
import com.bertvanbrakel.test.util.ProjectFinder;
import com.google.common.collect.Lists;

public class DirectoryRootTest {

	@Test
	public void walkResourcesTest(){
		File dir = ProjectFinder.getDefaultResolver().getBaseDir();
		Root root = new DirectoryRoot(new File(dir, "src/test/java/"));
		
		final Collection<RootResource> resources = Lists.newArrayList();
		root.accept(new BaseRootVisitor(){
			@Override
			public boolean visit(RootResource resource) {
				resources.add(resource);
				return true;
			}
		});
		
		assertThat(
			resources,
			is(AList.of(RootResource.class)
				.inAnyOrder()
				.containing()
				.item(ARootResource.with().pathFromClass(TstBeanOne.class))
				.item(ARootResource.with().pathFromClass(TstBeanTwo.class))
				.item(ARootResource.with().pathFromClass(DirectoryRootTest.class)))
		);
	}
}
