package com.bertvanbrakel.test.finder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.bertvanbrakel.lang.IsBuilder;
import com.bertvanbrakel.test.finder.Root.RootType;
import com.bertvanbrakel.test.util.ClassNameUtil;

public class ClassFinder {
	
	public static interface FinderFilter {
		public boolean isInclude(Object obj);
		public boolean isIncludeClassPath(Root root);
		public boolean isIncludeDir(RootResource resource);
		public boolean isIncludeResource(RootResource resource);
		public boolean isIncludeClassName(String className);
		public boolean isIncludeClass(Class<?> classToMatch);
		public boolean isIncludeArchive(RootResource archiveFile);
	}
	
	public static interface FinderMatchedCallback {
		public void onMatched(Object obj);
		public void onClassPathMatched(Root matchedRoot);
		public void onResourceMatched(RootResource matchedResource);
		public void onArchiveMatched(RootResource matchedArchive);
		public void onClassNameMatched(String matchedClassName);
		public void onClassMatched(Class<?> matchedClass);
	}

	public static interface FinderIgnoredCallback {
		public void onIgnored(Object obj);
		public void onClassPathIgnored(Root ignoredRoot);
		public void onResourceIgnored(RootResource ignoredResource);
		public void onArchiveIgnored(RootResource ignoredArchive);
		public void onClassNameIgnored(String ignoredClassName);
		public void onClassIgnored(Class<?> ignoredClass);
	}
	
	public static interface FinderErrorCallback {
		public void onError(Object obj,Exception e);
		public void onResourceError(RootResource resource,Exception e);
		public void onArchiveError(RootResource archive, Exception e);		
		public void onClassError(String fullClassname, Exception e);
	}
	
	private final List<Root> classPathRoots;
	private final ClassLoader classLoader;
	private final FinderFilter filter;
	private final FinderErrorCallback errorHandler;
	private final FinderIgnoredCallback ignoredCallback;
	private final FinderMatchedCallback matchedCallback;

	public static Builder newBuilder(){
		return new Builder();
	}
	
	public static Criteria newCriteria(){
		return new Criteria();
	}

	private ClassFinder(
			Iterable<Root> roots
			, FinderFilter filter
			, ClassLoader classLoader
			, FinderErrorCallback errorHandler
			, FinderIgnoredCallback ignoredCallback 
			, FinderMatchedCallback matchedCallback
			) {
		this.classPathRoots = ensureUnique(roots);
		this.filter = checkNotNull(filter, "expect filter");
		this.classLoader = checkNotNull(classLoader,"expect class loader");
		this.errorHandler = checkNotNull(errorHandler, "expect errorHandlerr");
		this.ignoredCallback = checkNotNull(ignoredCallback,"expect ignoredCallback");
		this.matchedCallback = checkNotNull(matchedCallback,"expect matchedCallback");	
	}

	private List<Root> ensureUnique(Iterable<Root> roots){
		checkNotNull(roots,"expect class path roots to search");
		Map<String, Root> map = newLinkedHashMap();
		for( Root root:roots){
			String key = root.getPathName();
			if( (RootType.UNKNOWN != root.getType()) || !map.containsKey(key) ){
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
	
	private Collection<String> findClassNames(Collection<RootResource> resources){
		Collection<String> foundClassNames = newArrayList();
		for(RootResource resource:resources){
			walkClassNames(foundClassNames, resource);
		}
		return foundClassNames;
	}
	
	private void walkClassNames(Collection<String> foundClassNames, RootResource resource) {
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
	
	public Collection<RootResource> findResources(){
		return findResources(classPathRoots);
	}
	
	private Collection<RootResource> findResources(Iterable<Root> roots){
    	final Collection<RootResource> resources = newArrayList();
    	RootVisitor visitor = new BaseRootVisitor(){
    		@Override
    		public boolean visit(Root root) {
        		if(filter.isInclude(root) && filter.isIncludeClassPath(root)){
        			matchedCallback.onMatched(root);
        			matchedCallback.onClassPathMatched(root);
        			return true;
        		} else {
    				ignoredCallback.onIgnored(root);
        			ignoredCallback.onClassPathIgnored(root);
        			return false;
        		}
    		}
    		
			@Override
			public boolean visit(RootResource resource) {
				if (filter.isInclude(resource) && filter.isIncludeResource(resource)) {
					matchedCallback.onMatched(resource);
					matchedCallback.onResourceMatched(resource);
					resources.add(resource);
				} else {
					ignoredCallback.onIgnored(resource);
					ignoredCallback.onResourceIgnored(resource);
				}
				return true;
			}	
		};
		
		for (Root root : roots) {
    		root.accept(visitor);	
    	}
    	return resources;
	}
	
	public static class Builder {
		
		private FinderMatchedCallback findMatchedCallback;
		private FinderIgnoredCallback findIgnoredCallback;
		private FinderErrorCallback findErrorCallback;
		private FinderFilter finderFilter;
		private ClassLoader classLoader;
		private List<Root> classPathRoots = newArrayList();

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
		
		public Builder setClassPathRoots(IsBuilder<Iterable<Root>> builder) {
			setSearchClassPaths(builder.build());
        	return this;
        }
		
		public Builder setSearchClassPaths(Iterable<Root> roots) {
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
