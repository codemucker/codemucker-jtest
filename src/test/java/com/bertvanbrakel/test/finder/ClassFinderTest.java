/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bertvanbrakel.test.finder;

import static com.bertvanbrakel.test.util.TestUtils.list;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import com.bertvanbrakel.test.finder.ClassFinder.Builder;
import com.bertvanbrakel.test.finder.a.TstBeanOne;
import com.bertvanbrakel.test.finder.b.TstBeanTwo;
import com.bertvanbrakel.test.finder.c.TstAnonymous;
import com.bertvanbrakel.test.finder.d.TstInner;
import com.bertvanbrakel.test.finder.e.TstAnnotation;
import com.bertvanbrakel.test.finder.e.TstAnnotationBean;
import com.bertvanbrakel.test.finder.matcher.LogicalMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;

public class ClassFinderTest {

//	@Test
//	public void test_find_class_dir() {
//		ClassFinder.newBuilder().build();
//		ClassFinder finder = new ClassFinder();
//		File dir = finder.findClassesDir();
//		assertNotNull(dir);
//		String path = convertToForwardSlashes(dir.getPath());
//		assertTrue(path.endsWith("/target/classes"));
//	}

//	private String convertToForwardSlashes(String path) {
//		return path.replace('\\', '/');
//	}
	

	@Test
	public void test_find_classes_dir() {
		ClassFinder finderDefault = newCriteria()
			.build();
			
		ClassFinder finderWithout = newCriteria()
			.setIncludeClassesDir(false)
			.build();
		
		ClassFinder finderWith = newCriteria()
			.setIncludeClassesDir(true)
			.build();

		Collection<Class<?>> foundDefault = list(finderDefault.findClasses());
		assertNotNull(foundDefault);
		assertTrue(foundDefault.contains(ClassFinder.class));
		assertFalse(foundDefault.contains(ClassFinderTest.class));
		
		Collection<Class<?>> foundWithout = list(finderWithout.findClasses());
		assertNotNull(foundWithout);
		assertFalse(foundWithout.contains(ClassFinder.class));
		assertFalse(foundWithout.contains(ClassFinderTest.class));
		
		Collection<Class<?>> foundWith = list(finderWith.findClasses());
		assertNotNull(foundWith);
		assertTrue(foundWith.contains(ClassFinder.class));
		assertFalse(foundWith.contains(ClassFinderTest.class));
	}
	
	@Test
	public void test_find_test_classes() {
		ClassFinder finderNoTests = newCriteria()
			.build();
		
		ClassFinder finderTests = newCriteria()
			.setIncludeTestDir(true)
			.build();

		Collection<Class<?>> foundNoTests = list(finderNoTests.findClasses());
		assertTrue(foundNoTests.contains(ClassFinder.class));
		assertFalse(foundNoTests.contains(ClassFinderTest.class));
		
		Collection<Class<?>> foundTests = list(finderTests.findClasses());		
		assertTrue(foundTests.contains(ClassFinder.class));
		assertTrue(foundTests.contains(ClassFinderTest.class));
	}
	
	@Test
	public void test_filename_exclude(){
		ClassFinder finderWith = newCriteria()
			.build();
			
		ClassFinder finderWithout = newCriteria()
			.excludeFileName("*Exception*.class")
			.build();
		
		Collection<Class<?>> foundWith = list(finderWith.findClasses());		
		assertTrue(foundWith.contains(ClassFinder.class));
		assertTrue(foundWith.contains(ClassFinderException.class));

		Collection<Class<?>> foundWithout = list(finderWithout.findClasses());
		assertTrue(foundWithout.contains(ClassFinder.class));
		assertFalse(foundWithout.contains(ClassFinderException.class));
	}
	
	@Test
	public void test_filename_exclude_pkg(){
		ClassFinder finderWith = newCriteria()
			.setIncludeTestDir(true)
			.build();
			
		ClassFinder finderWithout = newCriteria()
			.setIncludeTestDir(true)
			.excludeFileName("*/b/*")
			.build();

		Collection<Class<?>> foundWith = list(finderWith.findClasses());
		assertTrue(foundWith.contains(ClassFinder.class));
		assertTrue(foundWith.contains(TstBeanOne.class));		
		assertTrue(foundWith.contains(TstBeanTwo.class));
		
		Collection<Class<?>> foundWithout = list(finderWithout.findClasses());
		assertTrue(foundWithout.contains(ClassFinder.class));
		assertTrue(foundWithout.contains(TstBeanOne.class));		
		assertFalse(foundWithout.contains(TstBeanTwo.class));
	}

	@Test
	public void test_filename_exclude_target_has_no_effect(){
		ClassFinder finder = newCriteria()
			.excludeFileName("*/target/*")
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(ClassFinder.class));
	}
	
	@Test
	public void test_filename_include(){
		ClassFinder finderNoInclude = newCriteria()
			.setIncludeTestDir(true)
			.build();
			
		ClassFinder finderInclude = newCriteria()
			.setIncludeTestDir(true)
			.includeFileName("*/a/*")
			.build();

		Collection<Class<?>> foundNoInclude = list(finderNoInclude.findClasses());
		assertTrue(foundNoInclude.contains(TstBeanTwo.class));
		
		Collection<Class<?>> foundInclude = list(finderInclude.findClasses());
		assertFalse(foundInclude.contains(TstBeanTwo.class));
	}
	
	@Test
	public void test_filename_include_multiple_packages(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.includeFileName("*/a/*")
			.includeFileName("*/b/*")
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstBeanTwo.class));
	}
	
	@Test
	public void test_filename_exclude_trumps_include(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.includeFileName("*/a/*")
			.excludeFileName("*/a/*")
			.build();
			
		Collection<Class<?>> found = list(finder.findClasses());

		assertFalse(found.contains(TstBeanOne.class));
	}
	
	@Test
	public void test_include_instance_of(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.assignableTo(TstInterface1.class)
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstInterface1.class));
		assertTrue(found.contains(TstBeanOne.class));
		assertTrue(found.contains(TstBeanOneAndTwo.class));
		
		assertEquals(3, found.size());
	}

	@Test
	public void test_multiple_implements(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.assignableTo(TstInterface1.class)
			.assignableTo(TstInterface2.class)
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstInterface1.class));
		assertTrue(found.contains(TstInterface2.class));
		assertTrue(found.contains(TstBeanOne.class));
		assertTrue(found.contains(TstBeanTwo.class));
		assertTrue(found.contains(TstBeanOneAndTwo.class));
		
		assertEquals(5, found.size());
	}

	@Test
	public void test_class_must_match_multiple_matchers(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.assignableTo(TstInterface1.class, TstInterface2.class)
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstBeanOneAndTwo.class));

		assertEquals(1, found.size());
	}

	@Test
	public void test_find_enums(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstEnum.class));
		assertTrue(found.contains(TstBeanOneAndTwo.InstanceEnum.class));
		assertTrue(found.contains(TstBeanOneAndTwo.StaticEnum.class));
	}
	
	@Test
	public void test_filter_enum(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.excludeEnum()
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertFalse(found.contains(TstEnum.class));
		assertFalse(found.contains(TstBeanOneAndTwo.InstanceEnum.class));
		assertFalse(found.contains(TstBeanOneAndTwo.StaticEnum.class));

		assertTrue(found.size() > 1);
	}
	
	@Test
	public void test_filter_anonymous(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.includeFileName("*/c/*")
			.excludeAnonymous()
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertEquals(list(TstAnonymous.class),list(found));
	}
	
	@Test
	public void test_filter_inner_class(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.includeFileName("*/d/*")
			.excludeInner()
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertEquals(list(TstInner.class),list(found));
	}	
	
	@Test
	public void test_filter_interfaces(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.excludeInterfaces()
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		
		assertFalse(found.contains(TstInterface.class));
		assertFalse(found.contains(TstInterface1.class));
		assertFalse(found.contains(TstInterface2.class));

		assertTrue(found.contains(TstBeanOneAndTwo.class));
	}
	
	@Test
	public void test_find_has_annotations(){
		ClassFinder finder = newCriteria()
			.setIncludeTestDir(true)
			.withAnnotation(TstAnnotation.class)
			.build();

		Collection<Class<?>> found = list(finder.findClasses());

		assertEquals(list(TstAnnotationBean.class), found);
	}
	
	private static Criteria newCriteria(){
		Criteria c = ClassFinder.newCriteria();
		//c.setIgnoreCallback(new ClassFinder.LoggingIgnoredCallback());
		//c.setMatchCallback(new ClassFinder.LoggingMatchedCallback());
		return c;
	}

	private static Builder newBuilder(){
		 return ClassFinder.newBuilder();
	}
	
	private <T> Matcher<T> exclude(Matcher<T> mactcher){
		return LogicalMatchers.not(mactcher);
	}
}
