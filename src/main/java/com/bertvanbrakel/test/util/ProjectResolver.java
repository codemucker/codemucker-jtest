package com.bertvanbrakel.test.util;

import java.io.File;
import java.util.Collection;

public interface ProjectResolver {

	Collection<File> getMainCompileTargetDirs();

	Collection<File> getTestCompileTargetDirs();

	Collection<File> getCompileGeneratedDirs();
}
