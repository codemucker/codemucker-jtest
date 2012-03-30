package com.bertvanbrakel.test.finder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.bertvanbrakel.test.finder.ClassPathRoot.TYPE;
import com.bertvanbrakel.test.finder.matcher.ClassMatchers;
import com.bertvanbrakel.test.finder.matcher.ClassNameMatchers;
import com.bertvanbrakel.test.finder.matcher.ClassPathMatchers;
import com.bertvanbrakel.test.finder.matcher.FileMatchers;
import com.bertvanbrakel.test.finder.matcher.Matcher;
import com.bertvanbrakel.test.util.ClassNameUtil;
import com.bertvanbrakel.test.util.ProjectFinder;
import com.bertvanbrakel.test.util.ProjectResolver;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ClassFinder {
	
	public static interface FinderErrorHandler {
		public void onResourceError(ClassPathResource resource,Exception e);
		public void onClassError(String fullClassname, Exception e);
		public void onArchiveError(ClassPathResource archive, Exception e);		
	}
	
	public static interface FinderFindCallback {
		public void onArchiveMatched(ClassPathResource matchedArchive);
		public void onResourceMatched(ClassPathResource matchedResource);
		public void onClassNameMatched(String matchedClassName);
		public void onClassPath(ClassPathRoot matchedRoot);
		public void onClassMatched(Class<?> matchedClass);
	}

	public static interface FinderIgnoredCallback {
		public void onArchiveIgnored(ClassPathResource ignoredArchive);
		public void onResourceIgnored(ClassPathResource ignoredResource);
		public void onClassNameIgnored(String ignoredClassName);
		public void onClassIgnored(Class<?> ignoredClass);
		public void onClassPathIgnored(ClassPathRoot ignoredRoot);
	}
	
	public static interface FinderFilter {
		public boolean isIncludeClassPath(ClassPathRoot root);
		public boolean isIncludeDir(ClassPathResource resource);
		public boolean isIncludeResource(ClassPathResource resource);
		public boolean isIncludeClassName(String className);
		public boolean isIncludeClass(Class<?> classToMatch);
		public boolean isIncludeArchive(ClassPathResource archiveFile);
	}
	
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
	
	private static final Collection<String> DEFAULT_ARCHIVE_TYPES = ImmutableSet.of("jar", "war", "zip", "ear");
	
	private final Set<ClassPathRoot> classPathRoots;
	private final ClassLoader classLoader;
	private final FinderFilter filter;
	private final FinderErrorHandler errorHandler;
	private final FinderIgnoredCallback ignoredCallback;
	private final FinderFindCallback matchedCallback;
	private final Collection<String> archiveTypes = newHashSet(DEFAULT_ARCHIVE_TYPES);
	

	public static Builder newBuilder(){
		return new Builder();
	}
	
	public static Criteria newCriteria(){
		return new Criteria();
	}
	
//	public static void main(String[] args){
//		
//		FinderFindCallback callback = new BaseMatchedCallback(){
//			private int classCount = 0;
//			@Override
//            public void onClassMatched(Class<?> matchedClass) {
//				System.out.println( classCount + " class:" + matchedClass.getName());
//				classCount++;
//			}
//		};
//		
//        ClassFinder finder = ClassFinder
//			.newBuilder()
//				.setClassMatcher(ClassMatchers.all(ClassMatchers.excludeEnum(),ClassMatchers.includeInterfaces()))
//				.setIncludeProjectCompileDir()
//				.setCallbackOnMatched(callback)
//				.setSwallowErrors()
//				.build();
//		
//		System.out.println("starting");
//		finder.findClasses();
//		System.out.println("done!");
//	}

	private ClassFinder(
			List<ClassPathRoot> classPathRoots
			, FinderFilter filter
			, ClassLoader classLoader
			, FinderErrorHandler errorHandler
			, FinderIgnoredCallback ignoredCallback 
			, FinderFindCallback matchedCallback
			) {
		this.classPathRoots = Sets.newLinkedHashSet(checkNotNull(classPathRoots,"expect classPathRoots to search"));
		this.filter = checkNotNull(filter, "expect filter");
		this.classLoader = checkNotNull(classLoader,"expect class loader");
		this.errorHandler = checkNotNull(errorHandler, "expect errorHandlerr");
		this.ignoredCallback = checkNotNull(ignoredCallback,"expect ignoredCallback");
		this.matchedCallback = checkNotNull(matchedCallback,"expect matchedCallback");	
	}

	public Collection<Class<?>> findClasses() {
		return findClasses(findClassNames());
	}
	
	private Collection<Class<?>> findClasses(Collection<String> classNames){
		Collection<Class<?>> classes = newArrayList();
		for(String className:classNames){
			loadClass(classes,className);
		}
		return classes;
	}
	
	private void loadClass(Collection<Class<?>> foundClasses, String className) {
		Class<?> loadedClass = null;
		try {
			loadedClass = loadClass(className);
			if (filter.isIncludeClass(loadedClass)) {
				foundClasses.add(loadedClass);
				matchedCallback.onClassMatched(loadedClass);
			} else {
				ignoredCallback.onClassIgnored(loadedClass);
			}
		} catch (Exception e) {
			// allow clients to ignore errors if they want
			errorHandler.onClassError(className, e);
		}
	}

	private Class<?> loadClass(String className) {
		try {
			return (Class<?>) classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new ClassFinderException("couldn't load class " + className, e);
		} catch (NoClassDefFoundError e) {
			throw new ClassFinderException("couldn't load class " + className, e);
		} catch (IllegalAccessError e){
			throw new ClassFinderException("couldn't load class " + className, e);		
		}
	}

	public Collection<String> findClassNames(){
		return findClassNames(findResources());
	}
	
	private Collection<String> findClassNames(Collection<ClassPathResource> resources){
		Collection<String> foundClassNames = newArrayList();
		for(ClassPathResource resource:resources){
			walkClassNames(foundClassNames, resource);
		}
		return foundClassNames;
	}
	
	private void walkClassNames(Collection<String> foundClassNames, ClassPathResource resource) {
		if (resource.hasExtension("class")) {
			String className = ClassNameUtil.pathToClassName(resource.getRelPath());
			if (filter.isIncludeClassName(className)) {
				foundClassNames.add(className);
				matchedCallback.onClassNameMatched(className);
			} else {
				ignoredCallback.onClassNameIgnored(className);
			}
		}
	}
	
	public Collection<ClassPathResource> findResources(){
		return findResources(classPathRoots);
	}
	
	private Collection<ClassPathResource> findResources(Collection<ClassPathRoot> rootClassPathEntries){
    	Collection<ClassPathResource> resources = newArrayList();
    	for (ClassPathRoot root : rootClassPathEntries) {
    		if( filter.isIncludeClassPath(root)){
    			matchedCallback.onClassPath(root);
        		if( root.isDirectory()){
        			walkResourceDir(resources, root);
        		} else {
        			String extension = FilenameUtils.getExtension(root.getPathName());
        			if( archiveTypes.contains(extension)){
        				walkArchiveEntries(resources, root, root.getPath());
        			} else {
        				ignoredCallback.onClassPathIgnored(root);
        			}
        		}
    		} else {
    			ignoredCallback.onClassPathIgnored(root);
    		}
    	}
    	return resources;
	}
	
	public void walkArchiveEntries(Collection<ClassPathResource> found, ClassPathRoot root, File archive) {
		ClassPathResource resource = new ClassPathResource(root, archive, "", false);
		if (filter.isIncludeArchive(resource)) {
			matchedCallback.onArchiveMatched(resource);
			internalWalkArchive(found,root, archive);
		} else {
			ignoredCallback.onArchiveIgnored(resource);
		}
	}

	private void internalWalkArchive(Collection<ClassPathResource> found, ClassPathRoot root, File zipFile){
		ZipFile zip;
		try {
			zip = new ZipFile(zipFile);
		} catch (ZipException e) {
			throw new ClassFinderException("Error opening archive file:" + zipFile.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new ClassFinderException("Error opening archive file:" + zipFile.getAbsolutePath(), e);
		}

		try {
			walkZipEntries(found, root, zipFile, zip);
		} finally {
			try {
	            zip.close();
            } catch (IOException e) {
            	//ignore
            }
		}
	}
	
	private void walkZipEntries(Collection<ClassPathResource> found, ClassPathRoot root, File zipFile, ZipFile zip){
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while(entries.hasMoreElements()){
			ZipEntry entry = entries.nextElement();
			if( !entry.isDirectory()){
				String name = entry.getName();
				name = ensureStartsWithSlash(name);
				ClassPathResource zipResourceEntry = new ClassPathResource(root, zipFile, name, true);
				if( filter.isIncludeResource(zipResourceEntry)){
					matchedCallback.onResourceMatched(zipResourceEntry);
					found.add(zipResourceEntry);
				} else{
					ignoredCallback.onResourceIgnored(zipResourceEntry);
				}
			}
		}
	}

	private static String ensureStartsWithSlash(String name) {
	    if( !name.startsWith("/")){
	    	name = "/" + name;
	    }
	    return name;
    }

	
	private void walkResourceDir(Collection<ClassPathResource> found, ClassPathRoot root) {
		walkDir(found, root, "", root.getPath());
	}
	
	private void walkDir(Collection<ClassPathResource> found, ClassPathRoot rootDir, String parentPath, File dir) {
		ClassPathResource dirResource = new ClassPathResource(rootDir, dir, parentPath + "/", false);
		if (!filter.isIncludeDir(dirResource)) {
			ignoredCallback.onResourceIgnored(dirResource);
			return;
		}
		File[] files = dir.listFiles(FILE_FILTER);
		for (File f : files) {
			String relPath = parentPath + "/" + f.getName();
			ClassPathResource child = new ClassPathResource(rootDir, f, relPath, false);
			if (filter.isIncludeResource(child)) {
				found.add(child);
				matchedCallback.onResourceMatched(child);
			} else {
				ignoredCallback.onResourceIgnored(child);
			}
		}
		File[] childDirs = dir.listFiles(DIR_FILTER);
		for (File childDir : childDirs) {
			walkDir(found, rootDir, parentPath + "/" + childDir.getName(), childDir);
		}
	}
	
	public static class Builder {
		
		private final Map<String,ClassPathRoot> classPathsRoots = newLinkedHashMap();

		private ProjectResolver projectResolver = ProjectFinder.getDefaultResolver();
		private Matcher<ClassPathRoot> classPathMatcher;
		private Matcher<String> fileNameMatcher;
		private Matcher<String> classNameMatcher;
		private Matcher<Class<?>> classMatcher;
		private Matcher<ClassPathResource> classPathResourceMatcher;
		private FinderFindCallback findMatchedCallback;
		private FinderIgnoredCallback findIgnoredCallback;
		private FinderErrorHandler findErrorCallback;
		
		private ClassLoader classLoader;

		public ClassFinder build(){
			return new ClassFinder(
				toClassPathDirs()
			  , toFinderFilter()
			  , toClassLoader()
			  , toFinderErrorHandler()
			  , toIgnoredCallback()
			  , toMatchedCallback()  
			);
		}
		
		public Builder copyOf(){
			Builder copy = new Builder();
			copy.classNameMatcher = classNameMatcher;
			copy.classPathMatcher = classPathMatcher;
			copy.classPathResourceMatcher = classPathResourceMatcher;
			copy.classPathsRoots.putAll(classPathsRoots);
			copy.fileNameMatcher = fileNameMatcher;
			copy.findErrorCallback = findErrorCallback;
			copy.findIgnoredCallback = findIgnoredCallback;
			copy.findMatchedCallback = findMatchedCallback;
			copy.projectResolver = projectResolver;
			
			return copy;
		}
		
		public Builder setProjectResolver(ProjectResolver projectResolver) {
        	this.projectResolver = projectResolver;
        	return this;
        }
		
		public Builder setCallbackOnMatched(FinderFindCallback findMatchedCallback) {
        	this.findMatchedCallback = findMatchedCallback;
        	return this;
        }

		public Builder setCallbackOnIgnored(FinderIgnoredCallback findIgnoredCallback) {
        	this.findIgnoredCallback = findIgnoredCallback;
        	return this;
		}

		public Builder setCallbackOnError(FinderErrorHandler findErrorCallback) {
        	this.findErrorCallback = findErrorCallback;
        	return this;
		}
		
		public Builder setClassMatcher(Matcher<Class<?>> classMatcher) {
        	this.classMatcher = classMatcher;
        	return this;
        }

		public Builder setClassPathMatcher(Matcher<ClassPathRoot> classPathMatcher) {
        	this.classPathMatcher = classPathMatcher;
        	return this;
        }
//
//		public Builder setClassMatchers(Matcher<Class<?>>... matchers) {
//        	this.classMatcher = ClassMatchers.all(matchers);
//        	return this;
//        }
		
		public Builder anyClassMatchers(Matcher<Class<?>>... matchers) {
        	this.classMatcher = ClassMatchers.any(matchers);
        	return this;
        }
		
		public Builder setClassNameMatcher(Matcher<String> classNameMatcher) {
        	this.classNameMatcher = classNameMatcher;
        	return this;
		}

		public Builder setFileNameMatcher(Matcher<String> fileNameMatcher) {
        	this.fileNameMatcher = fileNameMatcher;
        	return this;
		}

		public Builder setClassPathResourceMatcher(Matcher<ClassPathResource> classPathResourceMatcher) {
        	this.classPathResourceMatcher = classPathResourceMatcher;
        	return this;
		}

		public Builder setClassLoader(ClassLoader classLoader) {
        	this.classLoader = classLoader;
        	return this;
		}
		
		public Builder setIncludeProjectCompileDir(){
			addClassPaths(getResolver().getMainCompileTargetDirs(),TYPE.COMPILE);
			return this;
		}
		
		public Builder setIncludeProjectGeneratedDir(){
			addClassPaths(getResolver().getCompileGeneratedDirs(),TYPE.COMPILE_GENERATED);
			return this;
		}
		
		public Builder setIncludeProjectTestDir(){
			addClassPaths(getResolver().getTestCompileTargetDirs(),TYPE.COMPILE_TEST);
			return this;
		}
		
		private ProjectResolver getResolver(){
			return projectResolver;
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
		
		public Builder addClassPaths(Collection<File> paths, TYPE type) {
			for( File path:paths){
				addClassPath(new ClassPathRoot(path,type));
			}
	    	return this;
	    }
		
		public Builder addClassPathDir(File path) {
			addClassPath(new ClassPathRoot(path,TYPE.UNKNOWN));
	    	return this;
	    }
		
		public Builder addClassPath(ClassPathRoot root) {
			if (!classPathsRoots.containsKey(root.getPathName()) || root.isTypeKnown()) {
				classPathsRoots.put(root.getPathName(), root);
			}
			return this;
		}
		
		public Builder setIncludeClassPath() {
			addClassPaths(findClassPathDirs());
	    	return this;
	    }
		
		public Builder setSwallowErrors(){
			this.findErrorCallback = new BaseErrorHandler();
			return this;
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
	
		private List<ClassPathRoot> toClassPathDirs(){
			if( classPathsRoots == null ){
				return newArrayList();
			}
			return newArrayList(classPathsRoots.values());
		}
		
		private ClassLoader toClassLoader(){
			return classLoader != null?classLoader:Thread.currentThread().getContextClassLoader();
		}
	
		private Matcher<ClassPathResource> toFileMatcher() {
			return classPathResourceMatcher == null ? FileMatchers.any() : classPathResourceMatcher;
		}

		private Matcher<ClassPathRoot> toClassPathMatcher() {
			return classPathMatcher == null ? ClassPathMatchers.any() : classPathMatcher;
		}

		private Matcher<String> toClassNameMatcher() {
			return classNameMatcher == null ? ClassNameMatchers.any() : classNameMatcher;
		}

		private Matcher<Class<?>> toClassMatcher() {
			return classMatcher == null ? ClassMatchers.anyClass() : classMatcher;
		}

		private FinderIgnoredCallback toIgnoredCallback() {
			return findIgnoredCallback != null ? findIgnoredCallback : new BaseIgnoredCallback();
		}

		private FinderFindCallback toMatchedCallback() {
			return findMatchedCallback != null ? findMatchedCallback : new BaseMatchedCallback();
		}
		
		private FinderErrorHandler toFinderErrorHandler() {
			return findErrorCallback != null ? findErrorCallback : new LoggingErrorHandler();
		}

		private FinderFilter toFinderFilter(){
			return new FinderFilter() {
				private final Matcher<Class<?>> classMatcher = toClassMatcher();
				private final Matcher<String> classNameMatcher = toClassNameMatcher();
				private final Matcher<ClassPathResource> fileMatcher = toFileMatcher();
				private final Matcher<ClassPathRoot> classPathMatcher = toClassPathMatcher();
				
				@Override
				public boolean isIncludeResource(ClassPathResource resource) {
					return fileMatcher.matches(resource);
				}
				
				@Override
				public boolean isIncludeDir(ClassPathResource resource) {
					return true;//fileMatcher.matches(resource);
				}
				
				@Override
				public boolean isIncludeClassPath(ClassPathRoot root) {
					return classPathMatcher.matches(root);
				}
				
				@Override
				public boolean isIncludeClassName(String className) {
					return classNameMatcher.matches(className);
				}
				
				@Override
				public boolean isIncludeClass(Class<?> classToMatch) {
					return classMatcher.matches(classToMatch);
				}
				
				@Override
				public boolean isIncludeArchive(ClassPathResource archiveFile) {
					return fileMatcher.matches(archiveFile);
				}
			};
		}		
	}
	
	private static class LoggingErrorHandler implements FinderErrorHandler {
		private final Logger logger;
		
		public LoggingErrorHandler(){
			this(Logger.getLogger(LoggingErrorHandler.class));
		}
		
		public LoggingErrorHandler(Logger logger){
			this.logger = checkNotNull(logger, "expect logger");
		}
		
		@Override
		public void onResourceError(ClassPathResource resource, Exception e) {
			logger.warn("error handling resource " + resource,e);
		}
		
		@Override
		public void onClassError(String fullClassname, Exception e) {
			logger.warn("error handling class " + fullClassname,e);
		}
		
		@Override
		public void onArchiveError(ClassPathResource archive, Exception e) {
			logger.warn("error handling archive " + archive,e);
		}
		
	}
	public static class BaseErrorHandler implements FinderErrorHandler {

		@Override
        public void onResourceError(ClassPathResource resource, Exception e) {
        }

		@Override
        public void onClassError(String fullClassname, Exception e) {
        }

		@Override
        public void onArchiveError(ClassPathResource archive, Exception e) {
        }
	}
	
	public static class BaseMatchedCallback implements FinderFindCallback {
		@Override
		public void onArchiveMatched(ClassPathResource archiveFile) {
		}

		@Override
		public void onResourceMatched(ClassPathResource resource) {
		}

		@Override
		public void onClassNameMatched(String className) {
		}

		@Override
		public void onClassPath(ClassPathRoot root) {
		}

		@Override
		public void onClassMatched(Class<?> matchedClass) {
		}
	}
	
	public static class BaseIgnoredCallback implements FinderIgnoredCallback {

		@Override
        public void onArchiveIgnored(ClassPathResource archiveFile) {
        }

		@Override
        public void onResourceIgnored(ClassPathResource resource) {
        }

		@Override
        public void onClassNameIgnored(String className) {
        }

		@Override
        public void onClassIgnored(Class<?> matchedClass) {
        }

		@Override
        public void onClassPathIgnored(ClassPathRoot root) {
        }
	}

	public static class LoggingMatchedCallback implements FinderFindCallback {
		private final Logger logger;
		
		public LoggingMatchedCallback(){
			this(Logger.getLogger(LoggingMatchedCallback.class));
		}
		
		public LoggingMatchedCallback(Logger logger){
			this.logger = checkNotNull(logger, "expect logger");
		}
		
		@Override
		public void onArchiveMatched(ClassPathResource archiveFile) {
			logger.info("matched archive:" + archiveFile);
		}

		@Override
		public void onResourceMatched(ClassPathResource resource) {
			logger.info("matched resource:" + resource);
		}

		@Override
		public void onClassNameMatched(String className) {
			logger.info("matched className:" + className);
		}

		@Override
		public void onClassPath(ClassPathRoot root) {
			logger.info("matched class path root:" + root);
		}

		@Override
		public void onClassMatched(Class<?> matchedClass) {
			logger.info("matched class:" + matchedClass.getName());
		}
	}
	
	public static class LoggingIgnoredCallback implements FinderIgnoredCallback {
		private final Logger logger;
		
		public LoggingIgnoredCallback(){
			this(Logger.getLogger(LoggingErrorHandler.class));
		}
		
		public LoggingIgnoredCallback(Logger logger){
			this.logger = checkNotNull(logger, "expect logger");
		}
		
		@Override
        public void onArchiveIgnored(ClassPathResource archiveFile) {
			logger.info("ignoring archive:" + archiveFile);
        }

		@Override
        public void onResourceIgnored(ClassPathResource resource) {
			logger.info("ignoring resource:" + resource);
		}

		@Override
        public void onClassNameIgnored(String className) {
			logger.info("ignoring class named:" + className);
		}

		@Override
        public void onClassIgnored(Class<?> ignoredClass) {
			logger.info("ignoring class:" + ignoredClass.getName());
			
		}

		@Override
        public void onClassPathIgnored(ClassPathRoot root) {
			logger.info("ignoring class path:" + root);
		}
	}
		
	
	public static class BaseFilter implements FinderFilter {

		@Override
        public boolean isIncludeClassPath(ClassPathRoot root) {
	        return true;
        }

		@Override
        public boolean isIncludeDir(ClassPathResource resourceh) {
	        return true;
        }

		@Override
        public boolean isIncludeResource(ClassPathResource resource) {
	        return true;
        }

		@Override
        public boolean isIncludeClassName(String className) {
	        return true;
        }

		@Override
        public boolean isIncludeClass(Class<?> classToMatch) {
	        return true;
        }

		@Override
        public boolean isIncludeArchive(ClassPathResource archiveFile) {
	        return true;
        }


	}
	
}
