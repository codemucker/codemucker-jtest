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
package org.codemucker.jtest;


import java.io.File;
import java.util.Collection;

public class ProjectLayouts {

	private static final ProjectLayout INSTANCE = new MavenProjectLayout();
	
	public static ProjectLayout getDefaultResolver(){
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
	
	private static <T> T first(Collection<T> col){
		return col.iterator().next();
	}

	public static File findTargetDir() {
		return INSTANCE.getBaseOutputDir();
	}

	public static File findProjectDir() {
		return INSTANCE.getBaseDir();
	}

}
