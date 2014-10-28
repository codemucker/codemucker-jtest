package org.codemucker.jtest;

import java.io.File;
import java.util.Collection;

import com.google.inject.ImplementedBy;

//todo:rename to Project?
@ImplementedBy(MavenLayoutProjectResolver.class)
public interface ProjectResolver {
    
    String getSourceVersion();
    String getTargetVersion();
    
	File getBaseOutputDir();

	File getBaseDir();

	File getTmpDir();
	
	/**
	 * Return a newly created random subdirectory within the tmp dir
	 * @return
	 */
	File newTmpSubDir();
    
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
