package com.bertvanbrakel.test.util;

import java.io.File;
import java.util.Collection;

public interface ProjectResolver {
	File getBaseOutputDir();

	File getBaseDir();

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
