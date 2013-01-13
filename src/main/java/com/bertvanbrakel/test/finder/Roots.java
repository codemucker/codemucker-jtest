package com.bertvanbrakel.test.finder;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bertvanbrakel.lang.IBuilder;
import com.bertvanbrakel.test.finder.Root.RootContentType;
import com.bertvanbrakel.test.finder.Root.RootType;
import com.bertvanbrakel.test.util.ProjectFinder;
import com.bertvanbrakel.test.util.ProjectResolver;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public final class Roots  {
	
	public static Builder builder(){
		return new Builder();
	}
	
	public static class Builder implements IBuilder<List<Root>> {

		private final Map<String,Root> roots = newLinkedHashMap();
		
		private ProjectResolver projectResolver;

		private boolean includeMainSrcDir = true;
		private boolean includeTestSrcDir = false;
		private boolean includeGeneratedSrcDir = false;
		private boolean includeClasspath = false;
		
		private Set<String> archiveTypes = Sets.newHashSet("jar","zip","ear","war");	
		
		private Builder(){
			//prevent instantiation outside of builder method
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
				copy.addClassPaths(resolver.getMainSrcDirs(),RootType.MAIN, RootContentType.SRC);
			}
			if (includeTestSrcDir) {
				copy.addClassPaths(resolver.getTestSrcDirs(),RootType.MAIN, RootContentType.SRC);
			}
			if (includeGeneratedSrcDir) {
				copy.addClassPaths(resolver.getGeneratedSrcDirs(),RootType.MAIN, RootContentType.SRC);
			}
			if (includeClasspath) {
				copy.addClassPaths(findClassPathDirs());
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
		
		public Builder addClassPathDir(String path) {
	    	addClassPathDir(new File(path));
	    	return this;
	    }
	
		public Builder addClassPaths(Collection<File> paths) {
			for( File path:paths){
				addClassPathDir(path);
			}
	    	return this;
	    }
		
		public Builder addClassPaths(Collection<File> paths, RootType relation, RootContentType contentType) {
			for(File path:paths){
				addClassPath(new DirectoryRoot(path,relation, contentType));
			}
	    	return this;
	    }
		
		public Builder addClassPathDir(File path) {
			if( path.isFile()){
				String extension = Files.getFileExtension(path.getName()).toLowerCase();
				if(archiveTypes.contains(extension)){
					addClassPath(new ArchiveRoot(path,RootType.DEPENDENCY, RootContentType.BINARY));	
				} else {
					throw new IllegalArgumentException("Don't currently know how to handle roots with file extension ." + extension); 
				}
			} else {
				addClassPath(new DirectoryRoot(path,RootType.DEPENDENCY,RootContentType.BINARY));
			}
			return this;
	    }
	
		public Builder addClassPaths(Iterable<Root> roots) {
			for(Root root:roots){
				addClassPath(root);
			}
			return this;
		}
		
		public Builder addClassPath(Root root) {
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
		
		public Builder setIncludeClasspath(boolean b) {
	    	this.includeClasspath = b;
	    	return this;
	    }
	}
}