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
package com.bertvanbrakel.test.util;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import com.bertvanbrakel.test.finder.ClassFinderException;
import com.google.common.collect.ImmutableSet;

public class ProjectFinder {

	private static final ProjectResolverImpl INSTANCE = new ProjectResolverImpl();
	
	public static ProjectResolver getDefaultResolver(){
		return INSTANCE;
	}

	public static File findDefaultMavenSrcDir(){
		return first(INSTANCE.getMainSrcDirs());
	}
	
	public static File findDefaultMavenResourceDir(){
		return first(INSTANCE.getMainResourceDirs());
	}
	
	public static File findDefaultMavenTestDir(){
		return first(INSTANCE.getTestSrcDirs());
	}
	
	public static File findDefaultMavenTestResourcesDir(){
		return first(INSTANCE.getTestResourcesDirs());
	}
	
	public static File findDefaultMavenCompileDir(){
		return first(INSTANCE.getMainCompileTargetDirs());
	}
	
	public static File findDefaultMavenCompileTestDir(){
		return first(INSTANCE.getTestCompileTargetDirs());
	}
	
	public static File findInProjectDir(String[] relativeDirs){
		return INSTANCE.findInProjectDir(relativeDirs);
	}
	
	private static <T> T first(Collection<T> col){
		return col.iterator().next();
	}

	
	public static File findTargetDir() {
		File targetDir = new File(findProjectDir(), "target");
		if (!targetDir.exists()) {
			boolean created = targetDir.mkdirs();
			if (!created) {
				throw new ClassFinderException("Couldn't create maven target dir " + targetDir.getAbsolutePath());
			}
		}
		return targetDir;
	}

	public static File findInProjectDir(Collection<String> projectFiles, String[] relativeDirs){
		File projectDir = findProjectDir(projectFiles);
		for (String option : relativeDirs) {
			File dir = new File(projectDir, option);
			if (dir.exists() && dir.isDirectory()) {
				return dir;
			}
		}
		throw new ClassFinderException("Can't find any of %s directories in %s", Arrays.asList(relativeDirs), projectDir.getAbsolutePath());	
	}

	public static File findProjectDir() {
		return findProjectDir(INSTANCE.getProjectFiles());
	}
	
	public static File findProjectDir(final Collection<String> projectFiles) {
		FilenameFilter projectDirFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return projectFiles.contains(name);
			}
		};

		try {
			File dir = new File("./");
			while (dir != null) {
				if (dir.listFiles(projectDirFilter).length > 0) {
					return dir.getCanonicalFile();
				}
				dir = dir.getParentFile();
			}
			throw new ClassFinderException("Can't find project dir. Started looking in %s, looking for any parent directory containing one of %s",
			                new File("./").getCanonicalPath(), projectFiles);
		} catch (IOException e) {
			throw new ClassFinderException("Error while looking for project dir", e);
		}
	}

	private static class ProjectResolverImpl implements ProjectResolver {
		private final Collection<String> projectFiles = ImmutableSet.<String>builder().add(
			"pom.xml", // maven2
	        "project.xml", // maven1
	        "build.xml", // ant
	        ".project", // eclipse
	        ".classpath" // eclipse	
		).build();
		
		Collection<String> getProjectFiles(){
			return projectFiles;
		}
		
		public Collection<File> getMainSrcDirs(){
			return findDir( "src/main/java" );
		}
		
		public Collection<File> getMainResourceDirs(){
			return findDir( "src/main/resources" );
		}
		
		public Collection<File> getTestSrcDirs(){
			return findDir( "src/test/java" );
		}
		
		public Collection<File> getTestResourcesDirs(){
			return findDir( "src/test/resources" );
		}
		
		@Override
		public Collection<File> getMainCompileTargetDirs(){
			return findDir( "target/classes" );
		}
		
		@Override
		public Collection<File> getTestCompileTargetDirs(){
			return findDir( "target/test-classes" );
		}

		@Override
        public Collection<File> getCompileGeneratedDirs() {
	        return findDir( "target/classes" );
        }

		private Collection<File> findDir(String relativePath){
			return newArrayList(findInProjectDir(new String[]{ relativePath }));
		}
		private File findInProjectDir(String[] relativeDirs){
			return findInProjectDir(projectFiles, relativeDirs);
		}
		
		private File findInProjectDir(Collection<String> projectFiles, String[] relativeDirs){
			File projectDir = findProjectDir(projectFiles);
			for (String option : relativeDirs) {
				File dir = new File(projectDir, option);
				if (dir.exists() && dir.isDirectory()) {
					return dir;
				}
			}
			throw new ClassFinderException("Can't find any of %s directories in %s", Arrays.asList(relativeDirs), projectDir.getAbsolutePath());	
		}		
	}
}
