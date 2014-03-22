package org.codemucker.jfind;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Objects;

public class RootResource  {

	private final Root root;
	private final String relPath;
	private final int depth;

	public RootResource(Root root, String relPath) {
		this.root = checkNotNull(root,"expect class path root");
		this.relPath = checkNotNull(relPath,"expect relative path");
		this.depth = countForwardSlashes(relPath);
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

	/**
	 * Return a stream to read this resource
	 * @return
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {
		return root.getResourceInputStream(relPath);
	}
	
	/**
	 * Read this resource as a uf8 string
	 * @return
	 * @throws IOException
	 */
	public String readAsString() throws IOException {
		return readAsString("utf8");
	}
	
	/**
	 * Read this resource as a string using the given encoding
	 * @param encoding
	 * 
	 * @return
	 * @throws IOException
	 */
	//TODO:should we throw an unchecked exception here?
	public String readAsString(String encoding) throws IOException{
		InputStream is  = null;
		try {
			is = root.getResourceInputStream(relPath);
			return IOUtils.toString(is,encoding);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	/**
	 * Return a stream to write to this resource
	 * @return
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException{
		return root.getResourceOutputStream(relPath);
	}
	
	public Root getRoot() {
		return root;
	}

	public int getDepthFromRoot() {
    	return depth;
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
	
	private boolean isDir(){
		return relPath.endsWith("/");
	}
	
	@Override
	public String toString(){
		return Objects
			.toStringHelper(this)
			.add("classPathRoot", root)
			.add("relPath", relPath)
			.add("depth",depth)
			.add("extension",getExtension())
			.toString();	
	}
}
