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
package org.codemucker.jfind;

import static org.codemucker.jtest.TestUtils.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.codemucker.jfind.a.TstBeanOne;
import org.codemucker.jfind.b.TstBeanTwo;
import org.codemucker.jfind.c.TstAnonymous;
import org.codemucker.jfind.d.TstInner;
import org.codemucker.jfind.e.TstAnnotation;
import org.codemucker.jfind.e.TstAnnotationBean;
import org.codemucker.jfind.matcher.AClass;
import org.codemucker.match.AString;
import org.junit.Test;


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
		ClassFinder finderDefault = newFinderBuilder()
			.build();
			
		ClassFinder finderWithout = newFinderBuilder()
				.setRoots(Roots.builder().build())
			.build();
		
		ClassFinder finderWith = newFinderBuilder()
				.setRoots(Roots.builder()
					.setIncludeMainCompiledDir(true)
					.setIncludeTestCompiledDir(true).build())
			.build();

		Collection<Class<?>> foundDefault = list(finderDefault.findClasses());
		assertNotNull(foundDefault);
		assertTrue(foundDefault.contains(ClassFinder.class));
		assertTrue(foundDefault.contains(ClassFinderTest.class));
		
		Collection<Class<?>> foundWithout = list(finderWithout.findClasses());
		assertNotNull(foundWithout);
		assertFalse(foundWithout.contains(ClassFinder.class));
		assertFalse(foundWithout.contains(ClassFinderTest.class));
		
		Collection<Class<?>> foundWith = list(finderWith.findClasses());
		assertNotNull(foundWith);
		assertTrue(foundWith.contains(ClassFinder.class));
		assertTrue(foundWith.contains(ClassFinderTest.class));
	}
	
	@Test
	public void test_find_test_classes() {
		ClassFinder finderNoTests = newFinderBuilder()
			.setRoots(Roots.builder().setIncludeMainCompiledDir(true).build())
			.build();
		
		ClassFinder finderTests = newFinderBuilder()
			.setRoots(Roots.builder()
				.setIncludeMainCompiledDir(true)
				.setIncludeTestCompiledDir(true)
				.build()
			)
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
		ClassFinder finderWith = newFinderBuilder()
			.build();
			
		ClassFinder finderWithout = newFinderBuilder()
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
		ClassFinder finderWith = newFinderBuilder()
			.build();
			
		ClassFinder finderWithout = newFinderBuilder()
			.excludeResource(ARootResource.with().path(AString.withAntPattern("*/b/*")))
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
		ClassFinder finder = newFinderBuilder()
			.excludeResource(ARootResource.with().path(AString.withAntPattern("*/target/*")))
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(ClassFinder.class));
	}
	
	@Test
	public void test_filename_include(){
		ClassFinder finderNoInclude = newFinderBuilder()
			.build();
			
		ClassFinder finderInclude = newFinderBuilder()
			.includeResource(ARootResource.with().path("*/a/*"))
			.build();

		Collection<Class<?>> foundNoInclude = list(finderNoInclude.findClasses());
		assertTrue(foundNoInclude.contains(TstBeanTwo.class));
		
		Collection<Class<?>> foundInclude = list(finderInclude.findClasses());
		assertFalse(foundInclude.contains(TstBeanTwo.class));
	}
	
	@Test
	public void test_filename_include_multiple_packages(){
		ClassFinder finder = newFinderBuilder()
			.includeResource(ARootResource.with().path(AString.withAntPattern("*/a/*")))
			.includeResource(ARootResource.with().path(AString.withAntPattern("*/b/*")))
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstBeanTwo.class));
	}
	
	@Test
	public void test_filename_exclude_trumps_include(){
		ClassFinder finder = newFinderBuilder()
			.includeResource(ARootResource.with().path(AString.withAntPattern("*/a/*")))
			.excludeResource(ARootResource.with().path(AString.withAntPattern("*/a/*")))
			.build();
			
		Collection<Class<?>> found = list(finder.findClasses());

		assertFalse(found.contains(TstBeanOne.class));
	}
	
	@Test
	public void test_include_instance_of(){
		ClassFinder finder = newFinderBuilder()
			.includeClass(AClass.assignableTo(TstInterface1.class))
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstInterface1.class));
		assertTrue(found.contains(TstBeanOne.class));
		assertTrue(found.contains(TstBeanOneAndTwo.class));
		
		assertEquals(3, found.size());
	}

	@Test
	public void test_multiple_implements(){
		ClassFinder finder = newFinderBuilder()
			.includeClass(AClass.assignableTo(TstInterface1.class))
			.includeClass(AClass.assignableTo(TstInterface2.class))	
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
		ClassFinder finder = newFinderBuilder()
			.includeClass(AClass.all(
					AClass.assignableTo(TstInterface1.class),
					AClass.assignableTo(TstInterface2.class)))	
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstBeanOneAndTwo.class));

		assertEquals(1, found.size());
	}

	@Test
	public void test_find_enums(){
		ClassFinder finder = newFinderBuilder().build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertTrue(found.contains(TstEnum.class));
		assertTrue(found.contains(TstBeanOneAndTwo.InstanceEnum.class));
		assertTrue(found.contains(TstBeanOneAndTwo.StaticEnum.class));
	}
	
	@Test
	public void test_filter_enum(){
		ClassFinder finder = newFinderBuilder()
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
		ClassFinder finder = newFinderBuilder()
			.includeResource(ARootResource.with().path(AString.withAntPattern("*/c/*")))
			.excludeAnonymous()
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertEquals(list(TstAnonymous.class),list(found));
	}
	
	@Test
	public void test_filter_inner_class(){
		ClassFinder finder = newFinderBuilder()
			.includeResource(ARootResource.with().path(AString.withAntPattern("*/d/*")))
			.excludeInner()
			.build();
		
		Collection<Class<?>> found = list(finder.findClasses());

		assertEquals(list(TstInner.class),list(found));
	}	
	
	@Test
	public void test_filter_interfaces(){
		ClassFinder finder = newFinderBuilder()
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
		ClassFinder finder = newFinderBuilder()			
			.withAnnotation(TstAnnotation.class)
			.build();

		Collection<Class<?>> found = list(finder.findClasses());

		assertEquals(list(TstAnnotationBean.class), found);
	}
	
	private static Criteria newFinderBuilder(){
		Criteria c = new Criteria()
			.setRoots(Roots.builder()
				.setIncludeMainCompiledDir(true)
				.setIncludeTestCompiledDir(true)
				.build()
			)
			.setConsoleLoggingCallback();
		//c.setIgnoreCallback(new ClassFinder.LoggingIgnoredCallback());
		//c.setMatchCallback(new ClassFinder.LoggingMatchedCallback());
		return c;
	}

}
