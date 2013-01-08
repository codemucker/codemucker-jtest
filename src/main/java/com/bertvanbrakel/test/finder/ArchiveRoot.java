package com.bertvanbrakel.test.finder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.google.common.base.Objects;

/**
 * Classpath root which handles archive (zip) files
 */
public class ArchiveRoot implements Root {
	private final File path;
	private final RootType type;
	private final RootContentType contentType;
	private final AtomicReference<ZipFile> cachedZip = new AtomicReference<ZipFile>();
	
	public ArchiveRoot(File path){
		this(path,RootType.UNKNOWN,RootContentType.BINARY);
	}
	
	public ArchiveRoot(File path,RootType type,RootContentType contentType){
		this.path = checkNotNull(path,"expect path");
		this.type = checkNotNull(type,"expect root relation");
		this.contentType = checkNotNull(contentType,"expect root content type");
		checkState(path.isFile(),"expect archive file to be a file");
	}
	
	@Override
    public boolean canWriteResource(String relPath) {
	    return false;
    }

	@Override
    public boolean canReadReource(String relPath) {
	    return path.exists() && path.canRead() && getZip().getEntry(relPath) != null;
    }
	
	@Override
	public OutputStream getResourceOutputStream(String relPath) throws IOException {
		throw new IOException("Can't don't support writing a resources to an archive");
	}
	
	@Override
	public InputStream getResourceInputStream(String relPath) throws IOException {
		if(!path.exists()){
			throw new FileNotFoundException(String.format("Couldn't find archive '%s' for root %s",path.getAbsolutePath(),this));	
		}
		if(!path.canRead()){
			throw new FileNotFoundException(String.format("Couldn't read archive entry '%s' in archive '%s' as archive is not readable for root %s",relPath,path.getAbsolutePath(),this));	
		}
		ZipFile zip = getZip();
		ZipEntry entry = zip.getEntry(relPath);
		if( entry == null){
			throw new FileNotFoundException(String.format("Couldn't find archive entry '%s' in archive '%s' for root %s",relPath,path.getAbsolutePath(),this));
		}
		return zip.getInputStream(entry);
	}

	private ZipFile getZip(){
		ZipFile zip = cachedZip.get();
		if (zip == null) {
			try {
				zip = new ZipFile(path);
				cachedZip.set(zip);
			} catch (ZipException e) {
				throw new ClassFinderException("Error opening archive file:" + path.getAbsolutePath(), e);
			} catch (IOException e) {
				throw new ClassFinderException("Error opening archive file:" + path.getAbsolutePath(), e);
			}
		}
		return zip;
	}
	
	@Override
	public String getPathName(){
		return path.getAbsolutePath();
	}

	@Override
	public RootType getType(){
		return type;
	}

	@Override
	public RootContentType getContentType(){
		return contentType;
	}
	
	@Override
	public String toString(){
		return Objects
    		.toStringHelper(this)
    		.add("path", getPathName())
    		.add("type", type)
    		.add("contentType", contentType)
    		.add("isArchive", true)
    		.add("exists", path.canRead())
     		.toString();
    }
	
	@Override
	public void accept(RootVisitor visitor) {
		if( visitor.visit(this)){
			visitResources(visitor);
		}
		visitor.endVisit(this);
	}

	private void visitResources(RootVisitor visitor) {
		ZipFile zip = getZip();
		try {
			internalWalkZipEntries(this, visitor, zip);
		} finally {
			try {
	            zip.close();
            } catch (IOException e) {
            	//ignore
            }
		}
	}
	
	private static void internalWalkZipEntries(Root root, RootVisitor visitor, ZipFile zip){
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while(entries.hasMoreElements()){
			ZipEntry entry = entries.nextElement();
			if( !entry.isDirectory()){
				String name = entry.getName();
				name = ensureStartsWithSlash(name);
				RootResource zipResourceEntry = new RootResource(root, name);
				visitor.visit(zipResourceEntry);
				visitor.endVisit(zipResourceEntry);
				if (isCancelled()) {
					return;
				}
			}
		}
	}
	
	private static boolean isCancelled(){
		return Thread.interrupted();
	}

	private static String ensureStartsWithSlash(String name) {
	    if( !name.startsWith("/")){
	    	name = "/" + name;
	    }
	    return name;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result
				+ ((type == null) ? 0 : type.hashCode());
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
		ArchiveRoot other = (ArchiveRoot) obj;
		if (contentType != other.contentType)
			return false;
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
