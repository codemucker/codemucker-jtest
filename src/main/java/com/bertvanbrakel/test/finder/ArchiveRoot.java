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

import com.google.common.base.Function;
import com.google.common.base.Objects;

/**
 * Classpath root which handles archive (zip) files
 */
public class ArchiveRoot implements Root {
	private final File path;
	private final RootType type;
	private final AtomicReference<ZipFile> cachedZip = new AtomicReference<ZipFile>();
	
	public ArchiveRoot(File path){
		this(path,RootType.UNKNOWN);
	}
	
	public ArchiveRoot(File path,RootType type){
		this.path = checkNotNull(path,"expect path");
		this.type = checkNotNull(type,"expect root type");
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
	public boolean isTypeKnown(){
		return !RootType.UNKNOWN.equals(type);
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
	public String toString(){
		return Objects
    		.toStringHelper(this)
    		.add("path", getPathName())
    		.add("type", type)
    		.add("isArchive", true)
    		.add("exists", path.canRead())
     		.toString();
    }

	@Override
    public void walkResources(Function<ClassPathResource, Boolean> callback) {
		ZipFile zip = getZip();
		try {
			internalWalkZipEntries(callback, this, zip);
		} finally {
			try {
	            zip.close();
            } catch (IOException e) {
            	//ignore
            }
		}
	}
	
	private static void internalWalkZipEntries(Function<ClassPathResource,Boolean> callback, Root root, ZipFile zip){
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while(entries.hasMoreElements()){
			ZipEntry entry = entries.nextElement();
			if( !entry.isDirectory()){
				String name = entry.getName();
				name = ensureStartsWithSlash(name);
				ClassPathResource zipResourceEntry = new ClassPathResource(root, name);
				boolean carryOn = callback.apply(zipResourceEntry) && !isCancelled();
				if (!carryOn) {
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

}
