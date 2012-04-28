package com.bertvanbrakel.test.util;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;

import com.bertvanbrakel.test.finder.ClassFinderException;
import com.google.common.collect.ImmutableSet;

public class MavenLayoutProjectResolver implements ProjectResolver {
	
	private final Collection<String> projectFiles;

	public MavenLayoutProjectResolver(){
		this(
			"pom.xml", // maven2
	        "project.xml", // maven1
	        "build.xml", // ant
	        ".project", // eclipse
	        ".classpath" // eclipse
		);
	}
	
	public MavenLayoutProjectResolver(Iterable<String> projectFiles){
		this.projectFiles = ImmutableSet.<String>builder().addAll(projectFiles).build();
	}

	public MavenLayoutProjectResolver(String... projectFiles){
		this.projectFiles = ImmutableSet.<String>builder().add(projectFiles).build();
	}
	
	@Override
	public Collection<File> getMainSrcDirs(){
		return findDir( "src/main/java" );
	}
	
	@Override
	public Collection<File> getMainResourceDirs(){
		return findDir( "src/main/resources" );
	}
	
	@Override
	public Collection<File> getTestSrcDirs(){
		return findDir( "src/test/java" );
	}
	
	@Override
    public Collection<File> getGeneratedSrcDirs() {
        return findDir( "src/generated/java" );
    }
	
	@Override
    public Collection<File> getGeneratedResourcesDirs() {
        return findDir( "src/generated/resources" );
    }
	
	@Override
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
    public Collection<File> getGeneratedCompileTargetDirs() {
        return findDir( "target/classes" );
    }


	private Collection<File> findDir(String relativePath){
		return newArrayList(findInProjectDir(relativePath, true));
	}

	@Override
	public File getBaseOutputDir() {
		File targetDir = new File(getBaseDir(), "target");
		if (!targetDir.exists()) {
			boolean created = targetDir.mkdirs();
			if (!created) {
				throw new ClassFinderException("Couldn't create maven target dir " + targetDir.getAbsolutePath());
			}
		}
		return targetDir;
	}

	@Override
    public File getTmpDir() {
	    return findInProjectDir("target/tmp/", true);
    }
	
	private File findInProjectDir(String relativeDir, boolean createIfNotFound){
		File projectDir = getBaseDir();
		File dir = new File(projectDir, relativeDir);
		if (dir.exists() && dir.isDirectory()) {
			return dir;
		}
		
		if(!dir.exists() && createIfNotFound){
			boolean created = dir.mkdirs();
			if (!created) {
				throw new ClassFinderException("Couldn't create dir " + dir.getAbsolutePath());
			}
		}
		if(!dir.isDirectory()){
			throw new ClassFinderException("Couldn't create dir, is not a directory " + dir.getAbsolutePath());		
		}
		return dir;
	}
	
	@Override
	public File getBaseDir() {
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
}