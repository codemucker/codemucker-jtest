package org.codemucker.jtest;

import java.io.File;
import java.util.Collection;

import com.google.inject.ImplementedBy;

//todo:rename to Project?
@ImplementedBy(MavenProjectLayout.class)
public interface ProjectLayout {
    
	File getBaseOutputDir();

	File getBaseDir();

	File getTmpDir();
	
	/**
	 * Return a newly created random subdirectory within the tmp dir
	 * @return
	 */
	File newTmpSubDir(String name);
    
	Collection<File> getMainSrcDirs();

	Collection<File> getMainResourceDirs();

	Collection<File> getTestSrcDirs();

	Collection<File> getTestResourcesDirs();

    Collection<File> getGeneratedSrcDirs();

    Collection<File> getTestGeneratedSrcDirs();

	Collection<File> getGeneratedResourcesDirs();

	Collection<File> getMainCompileTargetDirs();

	Collection<File> getTestCompileTargetDirs();

	Collection<File> getGeneratedCompileTargetDirs();

}
