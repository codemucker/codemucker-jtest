package org.codemucker.jtest;

import java.io.File;
import java.util.Collection;

//todo:rename to Project?
public interface ProjectResolver {
	File getBaseOutputDir();

	File getBaseDir();

	File getTmpDir();
	
	Collection<File> getMainSrcDirs();

	Collection<File> getMainResourceDirs();

	Collection<File> getTestSrcDirs();

	Collection<File> getTestResourcesDirs();

	Collection<File> getGeneratedSrcDirs();

	Collection<File> getGeneratedResourcesDirs();

	Collection<File> getMainCompileTargetDirs();

	Collection<File> getTestCompileTargetDirs();

	Collection<File> getGeneratedCompileTargetDirs();

	
}
