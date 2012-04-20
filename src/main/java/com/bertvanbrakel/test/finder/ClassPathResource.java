package com.bertvanbrakel.test.finder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Objects;

public class ClassPathResource  {

	private final Root root;
//	private final File file;
	private final String relPath;
	private final int depth;
	private final boolean fromArchive;

	public ClassPathResource(Root root, String relPath) {
		this.root = checkNotNull(root,"expect class path root");
		this.relPath = checkNotNull(relPath,"expect relative path");
		this.depth = countForwardSlashes(relPath);
		this.fromArchive = root.isArchive();
	}
	
	private static int countForwardSlashes(String s){
		int count = 0;
		for( int i = 0; i < s.length(); i++){
			if( s.charAt(i) == '/'){
				count++;
			}
		}
		return count;
	}

//	/**
//	 * @deprecated to be removed at some stage. Access should be via the class path root? To 
//	 * Allow dynamic creation of resources
//	 */
//	@Deprecated
//	public File getFile() {
//		return file;
//	}
	
	public InputStream getInputStream() throws IOException{
		return root.getResourceInputStream(relPath);
	}
	
	public String readAsString() throws IOException{
		return readAsString("utf8");
	}
	
	public String readAsString(String encoding) throws IOException{
		InputStream is  = null;
		try {
			is = root.getResourceInputStream(relPath);
			return IOUtils.toString(is,encoding);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	public OutputStream getOutputStream() throws IOException{
		return root.getResourceOutputStream(relPath);
	}
	
	public Root getRoot() {
		return root;
	}

	public int getDepth() {
    	return depth;
    }
	
	public boolean isDir(){
		return relPath.endsWith("/");
	}
	
	public boolean isArchiveEntry(){
		return fromArchive;
	}
	
	public String getRelPath() {
		return relPath;
	}

	public String getPackagePart(){
		int slash = relPath.lastIndexOf('/');
		if( slash != -1){
			String dottified = relPath.substring(0, slash).replace('/', '.');	
			if (dottified.charAt(0) == '.') {
				dottified = dottified.substring(1);
			}
			return dottified;
		}
		return null;
	}
	
	public String getBaseFileNamePart(){
		return FilenameUtils.getBaseName(relPath);
	}
	
	public String getPathWithoutExtension(){
		String ext = getExtension();
		if( ext != null ){
			return relPath.substring(0,relPath.length() - ext.length() - 1);
		}
		return relPath;
	}
	
	public boolean hasExtension(String extension){
		return extension.equals(getExtension());
	}

	public boolean hasExtensionIgnoreCase(String extension){
		return extension.toLowerCase().equals(getExtension());
	}
	
	public String getExtension(){
		return isDir()?null:FilenameUtils.getExtension(getRelPath());
	}
	
	@Override
	public String toString(){
		return Objects
			.toStringHelper(this)
			.add("classPathRoot", root)
			.add("relPath", relPath)
			.add("depth",depth)
			.add("extension",getExtension())
			.add("fromArchive", isArchiveEntry())
			.toString();	
	}
}
