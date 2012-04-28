package com.bertvanbrakel.test.finder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Function;
import com.google.common.base.Objects;

/**
 * Classpath root which handles directory type classpath entries
 */
public class DirectoryRoot implements Root {
	
	private static FileFilter DIR_FILTER = new FileFilter() {
		private static final char HIDDEN_DIR_PREFIX = '.';//like .git, .svn,....
		
		@Override
		public boolean accept(File dir) {
			return dir.isDirectory() && dir.getName().charAt(0) != HIDDEN_DIR_PREFIX && !dir.getName().equals("CVS");
		}
	};
	
	private static FileFilter FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File f) {
			return f.isFile();
		}
	};
	
	private final File path;
	private final RootType type;
	
	public DirectoryRoot(File path){
		this(path,RootType.UNKNOWN);
	}
	
	public DirectoryRoot(File path,RootType type){
		this.path = checkNotNull(path,"expect path");
		this.type = checkNotNull(type,"expect root type");
		if( !path.isDirectory()){
			throw new IllegalArgumentException("expect path to be a directory, path=" + path.getAbsolutePath());
		}
	}
	
	@Override
    public boolean canWriteResource(String relPath) {
	    return path.canWrite();
    }

	@Override
	public boolean canReadReource(String relPath) {
		if (path.canRead()) {
			File f = getRelPath(relPath);
			return f.exists() && f.canRead();
		}
		return false;
	}

	@Override
	public OutputStream getResourceOutputStream(String relPath) throws IOException {
		if(isDirectory()){
			//TODO:check relPath is in the given directory, no escaping up!
			File f = new File(path.getAbsolutePath(),relPath);
			if( !f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			if( !f.canWrite()){
				throw new IOException(String.format("Don't have permission to write file '%s' in dir '%s' for root %s. Full path %s",relPath,path.getAbsolutePath(),this, f.getAbsolutePath()));
			}
			if( !f.exists()){
				f.getParentFile().mkdir();
			}
			return new FileOutputStream(f);
		} else {
			throw new IOException(String.format("Couldn't write resource path '%s' for root %s",relPath,this));
		}
	}
	
	@Override
	public InputStream getResourceInputStream(String relPath) throws IOException {
		if( isDirectory()){
			//TODO:check relPath is in the given directory, no escaping up!
			File f = getRelPath(relPath);
			if( !f.exists()){
				throw new FileNotFoundException(String.format("Couldn't find file '%s' in dir '%s' for root %s",relPath,path.getAbsolutePath(),this));
			}
			if(!f.canRead()){
				throw new IOException(String.format("Don't have permission to read file '%s' in dir '%s' for root %s",relPath,path.getAbsolutePath(),this));
			}
			return new FileInputStream(f);
		} else {
			throw new IOException(String.format("Couldn't read resource path '%s' for root %s",relPath,this));
		}
	}
	
	private File getRelPath(String relpath){
		return new File(path.getAbsolutePath(),relpath);
	}
		
	@Override
	public boolean isTypeKnown(){
		return !RootType.UNKNOWN.equals(type);
	}

	@Override
	public String getPathName(){
		return path.getAbsolutePath();
	}
	
	public File getPath(){
		return path;
	}
	
	@Override
	public RootType getType(){
		return type;
	}
	
	
	private boolean isDirectory(){
		return path.isDirectory();
	}

	@Override
	public String toString(){
		return Objects
    		.toStringHelper(this)
    		.add("path", getPathName())
    		.add("type", type)
    		.add("isDirectory", isDirectory())
    		.toString();
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((path == null) ? 0 : path.hashCode());
	    result = prime * result + ((type == null) ? 0 : type.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    DirectoryRoot other = (DirectoryRoot) obj;;
	    if (path == null) {
		    if (other.path != null)
			    return false;
	    } else if (!path.equals(other.path))
		    return false;
	    if (type != other.type)
		    return false;
	    return true;
    }

	@Override
	public void walkResources(Function<ClassPathResource, Boolean> callback){
		walkDir(callback, this, "", path);
	}
	
	private static void walkDir(Function<ClassPathResource, Boolean> callback, Root root, String parentPath, File dir) {
		File[] files = dir.listFiles(FILE_FILTER);
		for (File f : files) {
			String relPath = parentPath + "/" + f.getName();
			ClassPathResource child = new ClassPathResource(root, relPath);
			boolean carryOn = callback.apply(child) && !isCancelled();
			if (!carryOn) {
				return;
			}
		}
		File[] childDirs = dir.listFiles(DIR_FILTER);
		for (File childDir : childDirs) {
			walkDir(callback, root, parentPath + "/" + childDir.getName(), childDir);
		}
	}
	
	private static boolean isCancelled(){
		return Thread.interrupted();
	}
}
