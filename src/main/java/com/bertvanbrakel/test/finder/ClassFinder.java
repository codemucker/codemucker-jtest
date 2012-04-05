package com.bertvanbrakel.test.finder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;

import com.bertvanbrakel.test.util.ClassNameUtil;
import com.google.common.collect.ImmutableSet;

public class ClassFinder {
	
	public static interface FinderFilter {
		public boolean isInclude(Object obj);
		public boolean isIncludeClassPath(ClassPathRoot root);
		public boolean isIncludeDir(ClassPathResource resource);
		public boolean isIncludeResource(ClassPathResource resource);
		public boolean isIncludeClassName(String className);
		public boolean isIncludeClass(Class<?> classToMatch);
		public boolean isIncludeArchive(ClassPathResource archiveFile);
	}
	
	public static interface FinderMatchedCallback {
		public void onMatched(Object obj);
		public void onClassPathMatched(ClassPathRoot matchedRoot);
		public void onResourceMatched(ClassPathResource matchedResource);
		public void onArchiveMatched(ClassPathResource matchedArchive);
		public void onClassNameMatched(String matchedClassName);
		public void onClassMatched(Class<?> matchedClass);
	}

	public static interface FinderIgnoredCallback {
		public void onIgnored(Object obj);
		public void onClassPathIgnored(ClassPathRoot ignoredRoot);
		public void onResourceIgnored(ClassPathResource ignoredResource);
		public void onArchiveIgnored(ClassPathResource ignoredArchive);
		public void onClassNameIgnored(String ignoredClassName);
		public void onClassIgnored(Class<?> ignoredClass);
	}
	
	public static interface FinderErrorCallback {
		public void onError(Object obj,Exception e);
		public void onResourceError(ClassPathResource resource,Exception e);
		public void onArchiveError(ClassPathResource archive, Exception e);		
		public void onClassError(String fullClassname, Exception e);
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
	
	private final List<ClassPathRoot> classPathRoots;
	private final ClassLoader classLoader;
	private final FinderFilter filter;
	private final FinderErrorCallback errorHandler;
	private final FinderIgnoredCallback ignoredCallback;
	private final FinderMatchedCallback matchedCallback;
	private final Collection<String> archiveTypes = newHashSet(DEFAULT_ARCHIVE_TYPES);
	

	public static Builder newBuilder(){
		return new Builder();
	}
	
	public static Criteria newCriteria(){
		return new Criteria();
	}
	
//	public static void main(String[] args){
//		
//		FinderMatchedCallback callback = new BaseMatchedCallback(){
//			private int classCount = 0;
//
//			@Override
//            public void onMatched(Object obj) {
//				System.out.println( "matched:" + obj);
//            }
//
//			@Override
//            public void onClassMatched(Class<?> matchedClass) {
//				System.out.println( classCount + " class:" + matchedClass.getName());
//				classCount++;
//			}
//		};
//		
////		ClassFinder finder = ClassFinder
////				.newBuilder()
////					//.setClassMatcher(ClassMatchers.all(ClassMatchers.excludeEnum(),ClassMatchers.includeInterfaces()))
////					//.setIncludeProjectCompileDir()
////					.setCallbackOnMatched(callback)
////					.setSwallowErrors()
////					.build();
//		ClassFinder finder = ClassFinder
//				.newCriteria()
//					//.setMatchCallback(ClassMatchers.all(ClassMatchers.excludeEnum(),ClassMatchers.includeInterfaces()))
//					.setIncludeClassesDir(true)
//					.setMatchCallback(callback)
//					//.setSwallowErrors()
//					
//					.build();
//			
//		System.out.println("starting");
//		finder.findClasses();
//		System.out.println("done!");
//	}

	private ClassFinder(
			List<ClassPathRoot> classPathRoots
			, FinderFilter filter
			, ClassLoader classLoader
			, FinderErrorCallback errorHandler
			, FinderIgnoredCallback ignoredCallback 
			, FinderMatchedCallback matchedCallback
			) {
		this.classPathRoots = cleanAndCheckRoots(classPathRoots);
		this.filter = checkNotNull(filter, "expect filter");
		this.classLoader = checkNotNull(classLoader,"expect class loader");
		this.errorHandler = checkNotNull(errorHandler, "expect errorHandlerr");
		this.ignoredCallback = checkNotNull(ignoredCallback,"expect ignoredCallback");
		this.matchedCallback = checkNotNull(matchedCallback,"expect matchedCallback");	
	}

	private List<ClassPathRoot> cleanAndCheckRoots(Iterable<ClassPathRoot> roots){
		checkNotNull(roots,"expect class path roots to search");
		Map<String, ClassPathRoot> map = newLinkedHashMap();
		for( ClassPathRoot root:roots){
			String key = root.getPathName();
			if( root.isTypeKnown() || !map.containsKey(key) ){
				map.put(key, root);
			}
		}
		//checkState(map.size()>0,"need some class path roots to search");
		return newArrayList(map.values());
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
			if (filter.isInclude(loadedClass) && filter.isIncludeClass(loadedClass)) {
				matchedCallback.onMatched(loadedClass);
				matchedCallback.onClassMatched(loadedClass);
				foundClasses.add(loadedClass);
			} else {
				ignoredCallback.onIgnored(loadedClass);
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
			if (filter.isInclude(className) && filter.isIncludeClassName(className)) {
				matchedCallback.onMatched(className);
				matchedCallback.onClassNameMatched(className);
				foundClassNames.add(className);
			} else {
				ignoredCallback.onIgnored(className);
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
    		if(filter.isInclude(root) && filter.isIncludeClassPath(root)){
    			matchedCallback.onMatched(root);
    			matchedCallback.onClassPathMatched(root);
        		if( root.isDirectory()){
        			walkResourceDir(resources, root);
        		} else {
        			String extension = FilenameUtils.getExtension(root.getPathName());
        			if( archiveTypes.contains(extension)){
        				walkArchiveEntries(resources, root, root.getPath());
        			} else {
        				ignoredCallback.onIgnored(root);
        				ignoredCallback.onClassPathIgnored(root);
        			}
        		}
    		} else {
				ignoredCallback.onIgnored(root);
    			ignoredCallback.onClassPathIgnored(root);
    		}
    	}
    	return resources;
	}
	
	public void walkArchiveEntries(Collection<ClassPathResource> found, ClassPathRoot root, File archive) {
		ClassPathResource resource = new ClassPathResource(root, archive, "", false);
		if (filter.isInclude(resource) && filter.isIncludeArchive(resource)) {
			matchedCallback.onMatched(resource);
			matchedCallback.onArchiveMatched(resource);
			internalWalkArchive(found,root, archive);
		} else {
			ignoredCallback.onIgnored(resource);
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
				if(filter.isInclude(zipResourceEntry) && filter.isIncludeResource(zipResourceEntry)){
					matchedCallback.onMatched(zipResourceEntry);
					matchedCallback.onResourceMatched(zipResourceEntry);
					found.add(zipResourceEntry);
				} else{
					ignoredCallback.onIgnored(zipResourceEntry);
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
			if (filter.isInclude(child) && filter.isIncludeResource(child)) {
				matchedCallback.onMatched(child);
				matchedCallback.onResourceMatched(child);
				found.add(child);
			} else {
				ignoredCallback.onIgnored(child);
				ignoredCallback.onResourceIgnored(child);
			}
		}
		File[] childDirs = dir.listFiles(DIR_FILTER);
		for (File childDir : childDirs) {
			walkDir(found, rootDir, parentPath + "/" + childDir.getName(), childDir);
		}
	}
	
	public static class Builder {
		
		private FinderMatchedCallback findMatchedCallback;
		private FinderIgnoredCallback findIgnoredCallback;
		private FinderErrorCallback findErrorCallback;
		private FinderFilter finderFilter;
		private ClassLoader classLoader;
		private List<ClassPathRoot> classPathRoots = newArrayList();

		public ClassFinder build(){
			return new ClassFinder(
			    classPathRoots
			  , toFilter()
			  , toClassLoader()
			  , toErrorCallback()
			  , toIgnoredCallback()
			  , toMatchedCallback()  
			);
		}
		
		private ClassLoader toClassLoader() {
			return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
		}

		private FinderIgnoredCallback toIgnoredCallback() {
			return findIgnoredCallback != null ? findIgnoredCallback : new BaseIgnoredCallback();
		}

		private FinderMatchedCallback toMatchedCallback() {
			return findMatchedCallback != null ? findMatchedCallback : new BaseMatchedCallback();
		}

		private FinderErrorCallback toErrorCallback() {
			return findErrorCallback != null ? findErrorCallback : new LoggingErrorCallback();
		}

		private FinderFilter toFilter() {
			return finderFilter != null ? finderFilter : new BaseFilter();
		}	
		
		public Builder copyOf(){
			Builder copy = new Builder();
			copy.classPathRoots.addAll(classPathRoots);
			copy.findErrorCallback = findErrorCallback;
			copy.findIgnoredCallback = findIgnoredCallback;
			copy.findMatchedCallback = findMatchedCallback;
			return copy;
		}
		
		public Builder setSearchClassPaths(ClassPathBuilder builder) {
			setSearchClassPaths(builder.build());
        	return this;
        }
		
		public Builder setSearchClassPaths(Iterable<ClassPathRoot> roots) {
			classPathRoots.addAll(newArrayList(roots));
        	return this;
        }
		
		public Builder setMatchedCallback(FinderMatchedCallback callback) {
        	this.findMatchedCallback = callback;
        	return this;
        }

		public Builder setIgnoredCallback(FinderIgnoredCallback callback) {
        	this.findIgnoredCallback = callback;
        	return this;
		}

		public Builder setErrorCallback(FinderErrorCallback callback) {
        	this.findErrorCallback = callback;
        	return this;
		}
		
		public Builder setFilter(FinderFilter filter) {
        	this.finderFilter = filter;
        	return this;
		}
		
		public Builder setClassLoader(ClassLoader classLoader) {
        	this.classLoader = classLoader;
        	return this;
		}
		
		public Builder setSwallowErrors(){
			this.findErrorCallback = new BaseErrorCallback();
			return this;
		}
	}
}
