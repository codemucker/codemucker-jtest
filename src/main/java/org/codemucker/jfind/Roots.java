package org.codemucker.jfind;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jtest.ProjectFinder;
import org.codemucker.jtest.ProjectResolver;
import org.codemucker.lang.IBuilder;

import com.google.common.collect.Sets;
import com.google.common.io.Files;

public final class Roots  {
	
	public static Builder builder(){
		return new Builder();
	}
	
	public static Builder builder(Iterable<Root> roots){
		return new Builder(roots);
	}
	
	public static class Builder implements IBuilder<List<Root>> {

		private final Map<String,Root> roots = newLinkedHashMap();
		
		//TODO:support multiple projects?
		private ProjectResolver projectResolver;
		
		//TODO: set all to false to force explicit include

		private boolean includeMainSrcDir = true;
		private boolean includeTestSrcDir = false;
		private boolean includeGeneratedSrcDir = false;
		private boolean includeClasspath = false;
		
		private Set<String> archiveTypes = Sets.newHashSet("jar","zip","ear","war");

		private boolean includeMainCompiledDir = false;
		private boolean includeTestCompiledDir = false;	
		
		private Builder(){
			//prevent instantiation outside of builder method
		}
		
		private Builder(Iterable<Root> roots){
			//prevent instantiation outside of builder method
			addRoots(roots);
		}
		/**
		 * Return a mutable list of class path roots. CHanges in the builder are not reflected in the returned
		 * list (or vice versa)
		 */
		public List<Root> build(){
			ProjectResolver resolver = toResolver();
			
			Builder copy = new Builder();
			copy.roots.putAll(roots);
			if (includeMainSrcDir) {
				copy.addRoot(resolver.getMainSrcDirs(),RootType.MAIN, RootContentType.SRC);
			}
			if (includeTestSrcDir) {
				copy.addRoot(resolver.getTestSrcDirs(),RootType.MAIN, RootContentType.SRC);
			}
			if (includeGeneratedSrcDir) {
				copy.addRoot(resolver.getGeneratedSrcDirs(),RootType.MAIN, RootContentType.SRC);
			}
			if (includeMainCompiledDir) {
				copy.addRoot(resolver.getMainCompileTargetDirs(),RootType.MAIN, RootContentType.BINARY);
			}
			if (includeTestCompiledDir) {
				copy.addRoot(resolver.getTestCompileTargetDirs(),RootType.TEST, RootContentType.BINARY);
			}
			
			if (includeClasspath) {
				copy.addRoots(findClassPathDirs());
			}
			return newArrayList(copy.roots.values());
		}
		
		private ProjectResolver toResolver(){
			return projectResolver != null ? projectResolver : ProjectFinder.getDefaultResolver();
		}
		
		public Builder copyOf() {
			Builder copy = new Builder();
			copy.projectResolver = projectResolver;
			copy.includeMainSrcDir = includeMainSrcDir;
			copy.includeClasspath = includeClasspath;
			copy.includeGeneratedSrcDir = includeGeneratedSrcDir;
			copy.includeTestSrcDir = includeTestSrcDir;
			copy.includeMainCompiledDir = includeMainCompiledDir;
			copy.includeTestCompiledDir = includeTestCompiledDir;
			copy.roots.putAll(roots);
			copy.archiveTypes.addAll(archiveTypes);
			
			return copy;
		}
		
		private Set<File> findClassPathDirs() {
			Set<File> files = newLinkedHashSet();

			String classpath = System.getProperty("java.class.path");
			String sep = System.getProperty("path.separator");
			String[] paths = classpath.split(sep);
	
			Collection<String> fullPathNames = newArrayList();
			for (String path : paths) {
				try {
					File f = new File(path);
					if (f.exists() & f.canRead()) {
						String fullPath = f.getCanonicalPath();
						if (!fullPathNames.contains(fullPath)) {
							files.add(f);
							fullPathNames.add(fullPath);
						}
					}
				} catch (IOException e) {
					throw new ClassFinderException("Error trying to resolve pathname " + path);
				}
			}
			return files;
		}	
		
		public Builder setProjectResolver(ProjectResolver resolver){
			this.projectResolver = resolver;
			return this;
		}
		
		/**
		 * Add additional file extension types to denote an archive resources (like a jar). E.g. 'jar'
		 * 
		 * Default contains jar,zip,war,ear
		 * 
		 * @param extension
		 * @return
		 */
		public Builder addArchiveFileExtension(String extension) {
			this.archiveTypes.add(extension);
	    	return this;
	    }
		
		public Builder addRoot(String path) {
	    	addRootDir(new File(path));
	    	return this;
	    }
	
		public Builder addRoots(Collection<File> paths) {
			for( File path:paths){
				addRootDir(path);
			}
	    	return this;
	    }
		
		public Builder addRoot(Collection<File> paths, RootType relation, RootContentType contentType) {
			for(File path:paths){
				addRoot(new DirectoryRoot(path,relation, contentType));
			}
	    	return this;
	    }
		
		public Builder addRootDir(File path) {
			if( path.isFile()){
				String extension = Files.getFileExtension(path.getName()).toLowerCase();
				if(archiveTypes.contains(extension)){
					addRoot(new ArchiveRoot(path,RootType.DEPENDENCY, RootContentType.BINARY));	
				} else {
					throw new IllegalArgumentException("Don't currently know how to handle roots with file extension ." + extension); 
				}
			} else {
				addRoot(new DirectoryRoot(path,RootType.DEPENDENCY,RootContentType.BINARY));
			}
			return this;
	    }
	
		public Builder addRoots(Iterable<Root> roots) {
			for(Root root:roots){
				addRoot(root);
			}
			return this;
		}
		
		public Builder addRoot(Root root) {
			String key = root.getPathName();
			if ((RootType.UNKNOWN != root.getType()) || !roots.containsKey(key)) {
				roots.put(key, root);
			}
			return this;
		}
		
		public Builder setIncludeAll() {
			setIncludeMainSrcDir(true);
			setIncludeTestSrcDir(true);
			setIncludeGeneratedSrcDir(true);
			setIncludeClasspath(true);
			setIncludeMainCompiledDir(true);
			setIncludeTestCompiledDir(true);
			return this;
		}

		public Builder setIncludeAllSrcs() {
			setIncludeMainSrcDir(true);
			setIncludeGeneratedSrcDir(true);
			setIncludeTestSrcDir(true);
			setIncludeClasspath(false);
			return this;
		}
		
		public Builder setIncludeMainSrcDir(boolean b) {
			this.includeMainSrcDir = b;
			return this;
		}
	
		public Builder setIncludeTestSrcDir(boolean b) {
			this.includeTestSrcDir = b;
			return this;
		}
	
		public Builder setIncludeGeneratedSrcDir(boolean b) {
			this.includeGeneratedSrcDir = b;
			return this;
		}
	
		public Builder setIncludeMainCompiledDir(boolean b) {
			this.includeMainCompiledDir = b;
			return this;
		}
		
		public Builder setIncludeTestCompiledDir(boolean b) {
			this.includeTestCompiledDir = b;
			return this;
		}
		public Builder setIncludeClasspath(boolean b) {
	    	this.includeClasspath = b;
	    	return this;
	    }
	}
}