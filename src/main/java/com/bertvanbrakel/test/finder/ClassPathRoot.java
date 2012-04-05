package com.bertvanbrakel.test.finder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public class ClassPathRoot {

	public static enum TYPE {
		MAIN_SRC,TEST_SRC,MAIN_COMPILE,TEST_COMPILE,DEPENDENCY,GENERATED_SRC,GENERATED_COMPILE,UNKNOWN;
	}
	
	private static final Collection<String> DEFAULT_ARCHIVE_TYPES = ImmutableSet.of("jar", "war", "zip", "ear");
	private final File path;
	private TYPE type;
	
	public ClassPathRoot(File path){
		this(path,TYPE.UNKNOWN);
	}
	
	public ClassPathRoot(File path,TYPE type){
		this.path = checkNotNull(path,"expect path");
		this.type = checkNotNull(type,"expect type");
	}
	
	public boolean isTypeKnown(){
		return !TYPE.UNKNOWN.equals(type);
	}
	public String getPathName(){
		return path.getAbsolutePath();
	}
	
	public File getPath(){
		return path;
	}
	
	public TYPE getType(){
		return type;
	}
	
	public boolean isDirectory(){
		return path.isDirectory();
	}
	
	public boolean isArchive() {
		String extension = FilenameUtils.getExtension(path.getName());
		return extension == null ? false : DEFAULT_ARCHIVE_TYPES.contains(extension);
	}

	@Override
	public String toString(){
		return Objects
    		.toStringHelper(this)
    		.add("path", getPathName())
    		.add("type", type)
    		.add("isArchive", isArchive())
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
	    ClassPathRoot other = (ClassPathRoot) obj;
	    if (path == null) {
		    if (other.path != null)
			    return false;
	    } else if (!path.equals(other.path))
		    return false;
	    if (type != other.type)
		    return false;
	    return true;
    }
	
	
	
	
	
}
