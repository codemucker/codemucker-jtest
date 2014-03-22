package org.codemucker.jfind;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	
	private final File baseDir;
	private final RootType type;
	private final RootContentType contentType;
	
	public DirectoryRoot(File path){
		this(path,RootType.UNKNOWN,RootContentType.BINARY);
	}
	
	public DirectoryRoot(File path,RootType type,RootContentType contentType){
		this.baseDir = checkNotNull(path,"expect path");
		this.type = checkNotNull(type,"expect root relation");
		this.contentType = checkNotNull(contentType,"expect root content type");
		if( path.exists() && !path.isDirectory()){
			throw new IllegalArgumentException("expect path to be a directory, path=" + path.getAbsolutePath());
		}
	}
	
	@Override
    public boolean canWriteResource(String relPath) {
	    return baseDir.canWrite();
    }

	@Override
	public boolean canReadReource(String relPath) {
		if (baseDir.canRead()) {
			File f = getByRelPath(relPath);
			return f.exists() && f.canRead();
		}
		return false;
	}

	@Override
	public OutputStream getResourceOutputStream(String relPath) throws IOException {
		if(isDirectoryAndExists()){
			//TODO:check relPath is in the given directory, no escaping up!
			File f = new File(baseDir.getAbsolutePath(),relPath);
			if(!f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			if(!f.canWrite()){
				throw new IOException(String.format("Don't have permission to write file '%s' in dir '%s' for root %s. Full path %s",relPath,baseDir.getAbsolutePath(),this, f.getAbsolutePath()));
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
		if(isDirectoryAndExists()){
			//TODO:check relPath is in the given directory, no escaping up!
			File f = getByRelPath(relPath);
			if(!f.exists()){
				throw new FileNotFoundException(String.format("Couldn't find file '%s' in dir '%s' for root %s",relPath,baseDir.getAbsolutePath(),this));
			}
			if(!f.canRead()){
				throw new IOException(String.format("Don't have permission to read file '%s' in dir '%s' for root %s",relPath,baseDir.getAbsolutePath(),this));
			}
			return new FileInputStream(f);
		} else {
			throw new IOException(String.format("Couldn't read resource path '%s' for root %s",relPath,this));
		}
	}
	
	private File getByRelPath(String relpath){
		return new File(baseDir.getAbsolutePath(),relpath);
	}

	@Override
	public String getPathName(){
		return baseDir.getAbsolutePath();
	}
	
	public File getPath(){
		return baseDir;
	}
	
	@Override
	public RootType getType(){
		return type;
	}
		
	@Override
	public RootContentType getContentType(){
		return contentType;
	}
	
	private boolean isDirectoryAndExists(){
		return baseDir.exists() && baseDir.isDirectory();
	}

	@Override
	public String toString(){
		return Objects
    		.toStringHelper(this)
    		.add("path", getPathName())
    		.add("type", type)
    		.add("contentType", contentType)
    		.add("isDirectory", baseDir.isDirectory())
    		.add("exists", baseDir.exists())
    		.toString();
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((baseDir == null) ? 0 : baseDir.hashCode());
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
	    if (baseDir == null) {
		    if (other.baseDir != null)
			    return false;
	    } else if (!baseDir.equals(other.baseDir))
		    return false;
	    if (type != other.type)
		    return false;
	    if (contentType != other.contentType)
		    return false;
	    return true;
    }
	
	@Override
	public void accept(RootVisitor visitor) {
		if( visitor.visit(this)){
			visitResources(visitor);
		}
		visitor.endVisit(this);
	}

	private void visitResources(RootVisitor visitor) {
		if(isDirectoryAndExists()){
			visitResources(this,visitor,null,baseDir);
		}
	}

	private static void visitResources(Root root, RootVisitor visitor, String parentPath, File dir) {
		File[] files = dir.listFiles(FILE_FILTER);
		String basePath = parentPath==null?"":(parentPath + "/");
		for (File f : files) {
			String relPath = basePath + f.getName();
			RootResource child = new RootResource(root, relPath);
			visitor.visit(child);
			visitor.endVisit(child);
			if( isCancelled()){
				return;
			}
		}
		File[] childDirs = dir.listFiles(DIR_FILTER);
		for (File childDir : childDirs) {
			visitResources(root, visitor, basePath + childDir.getName(), childDir);
		}
	}
	
	private static boolean isCancelled(){
		return Thread.interrupted();
	}
}
