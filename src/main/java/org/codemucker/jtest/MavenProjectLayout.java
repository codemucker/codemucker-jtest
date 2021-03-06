package org.codemucker.jtest;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.codemucker.lang.PathUtil;

public class MavenProjectLayout implements ProjectLayout {
	
	private final String TMP_DIR = "target/jmutate-" + MavenProjectLayout.class.getSimpleName() + "/tmp" + System.nanoTime();
	
	private static final String[] DEFAULT_FILES = new String[]{
		"pom.xml", // maven2
        "project.xml", // maven1
        "build.xml", // ant
        "build.gradle", // gradle
        ".project", // eclipse
        ".classpath", // eclipse
        "*.ipr", //intellij file based config
        "*.iws",//intellij file based config
        ".idea",//intellij dir based config
        ".netbeans"//possibly. Not sure how reliable this is
    };
	
	private final File baseDir;
	
	/**
	 * Create a new maven layout starting in the current directory and walking up until a pom.xml file is found
	 * @return
	 */
	public static MavenProjectLayout create() {
		return new MavenProjectLayout(currentDir(), DEFAULT_FILES);
	}

	/**
	 * Create a new maven layout walking up from the current directory until a directory is found with one of the given project files
	 * @param projectFiles
	 * @return
	 */
	public static MavenProjectLayout createUsingProjectFiles(String... projectFiles) {
		return new MavenProjectLayout(currentDir(), projectFiles);
	}

	/**
	 * Create a new maven layout using the given basedir. No walking up looking for a pom.
	 * 
	 * @param baseDir
	 * @return
	 */
	public static MavenProjectLayout createUsingBaseDir(File baseDir) {
		return new MavenProjectLayout(baseDir);
	}

	public MavenProjectLayout() {
		this(currentDir(), DEFAULT_FILES);
	}
	
	private MavenProjectLayout(File startSearchingFromDir, String... projectFiles){
		this.baseDir = findBaseDir(startSearchingFromDir, Arrays.asList(projectFiles));
	}

	private MavenProjectLayout(File baseDir){
		this.baseDir = baseDir;
	}

	
	private static File currentDir(){
		return new File("./");
	}
	
	private static File findBaseDir(final File startSearchingFromDir, final Collection<String> projectRootFiles) {
		FilenameFilter projectDirFilter = new FilenameFilter() {
			@Override
			public boolean accept(File fileOrDir, String name) {
				return projectRootFiles.contains(name);
			}
		};

		try {
			File dir = startSearchingFromDir;
			while (dir != null) {
				if (dir.listFiles(projectDirFilter).length > 0) {
					return dir.getCanonicalFile();
				}
				dir = dir.getParentFile();
			}
			throw new JTestException("Can't find project dir. Started looking in %s, looking for any parent directory containing one of %s",
			                new File("./").getCanonicalPath(), projectRootFiles);
		} catch (IOException e) {
			throw new JTestException("Error while looking for project dir", e);
		}
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
    public Collection<File> getTestGeneratedSrcDirs() {
        return findDir( "src/test-generated/java" );
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
				throw new JTestException("Couldn't create maven target dir " + targetDir.getAbsolutePath());
			}
		}
		return targetDir;
	}

    @Override
    public File newTmpSubDir(String name) {
        try {
            return PathUtil.newTmpDir(getTmpDir(), name,"");
        } catch (IOException e) {
            throw new JTestException("error creating project sub tmp dir", e);
        }
    }
	
	@Override
    public File getTmpDir() {
	    return findInProjectDir(TMP_DIR, true);
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
				throw new JTestException("Couldn't create dir " + dir.getAbsolutePath());
			}
		}
		if(!dir.isDirectory()){
			throw new JTestException("Couldn't create dir, is not a directory " + dir.getAbsolutePath());		
		}
		return dir;
	}
	
	@Override
	public File getBaseDir() {
		return baseDir;
	}

}