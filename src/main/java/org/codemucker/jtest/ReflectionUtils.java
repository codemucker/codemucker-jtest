/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codemucker.jtest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;

import org.codemucker.jpattern.bean.Property;
import org.codemucker.jtest.bean.BeanException;
import org.codemucker.lang.StringUtil;


public class ReflectionUtils {

	public static <T> Constructor<T> getLongestCtor(Class<T> beanClass) {
		Constructor<T> longest = null;
		Constructor<T>[] ctors = (Constructor<T>[]) beanClass.getDeclaredConstructors();
		for (Constructor<T> ctor : ctors) {
			if (isPublic(ctor)) {
				int len = ctor.getParameterTypes().length;
				if (len > 0 && (longest == null || longest.getParameterTypes().length < len)) {
					longest = ctor;
				}
			}
		}
		return longest;
	}

	public static <T> Constructor<T> getNoArgCtor(Class<T> beanClass, boolean changeAccessibility) {
		try {
			Constructor<T> ctor = beanClass.getDeclaredConstructor(null);
			if (isPublic(ctor)) {
				return ctor;
			} else if (changeAccessibility) {
				ctor.setAccessible(true);
				return ctor;
			}
		} catch (SecurityException e) {
			// do nothing
		} catch (NoSuchMethodException e) {
			// do nothing
		}

		return null;
	}

	public static <T> T invokeCtorWith(Constructor<T> ctor, Object[] args) {
		try {
			T bean = ctor.newInstance(args);
			return bean;
		} catch (IllegalArgumentException e) {
			throw new BeanException("Error invoking ctor for type %s with args %s", e, ctor.getDeclaringClass().getName(), toString(args));
		} catch (InstantiationException e) {
			throw new BeanException("Error invoking ctor for type %s with args %s", e, ctor.getDeclaringClass().getName(), toString(args));
		} catch (IllegalAccessException e) {
			throw new BeanException("Error invoking ctor for type %s with args %s", e, ctor.getDeclaringClass().getName(), toString(args));
		} catch (InvocationTargetException e) {
			throw new BeanException("Error invoking ctor for type %s with args %s", e, ctor.getDeclaringClass().getName(), toString(args));
		}
	}

	public static Object invokeMethod(Object bean, Method m, Object[] args) {
		return invokeMethod(bean, m, args, false);
	}
	
	public static Object invokeMethod(Object bean, Method m, Object[] args, boolean makeAccessible) {
		try {
			boolean setAccessible = (!isPublic(m) || !isPublic(m.getDeclaringClass()) ) && makeAccessible;
			if (setAccessible) {
				m.setAccessible(true);
			}
			try {
				return m.invoke(bean, args);
			} finally {
				if (setAccessible) {
					m.setAccessible(false);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new BeanException("Error invoking method '%s' on class %s with args %s", e, m.toGenericString(), bean.getClass().getName(),toString(args));
		} catch (IllegalAccessException e) {
			throw new BeanException("Error invoking method '%s' on class %s with args %s", e, m.toGenericString(), bean.getClass().getName(),toString(args));
		} catch (InvocationTargetException e) {
			throw new BeanException("Error invoking method '%s' on class %s with args %s", e, m.toGenericString(),bean.getClass().getName(),toString(args));
		}
	}
	

	public static Object getFieldValue(Object bean, Field f){
		return getFieldValue(bean, f, false);
	}
	
	public static Object getFieldValue(Object bean, Field f, boolean makeAccessible){
		try {
			boolean setAccessible = !isPublic(f) && makeAccessible;
			if (setAccessible) {
				f.setAccessible(true);
			}
			try {
				return f.get(bean);
			} finally {
				if (setAccessible) {
					f.setAccessible(false);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new BeanException("Error getting value from field '%s' on class %s", e, f.toGenericString(), bean.getClass().getName());
		} catch (IllegalAccessException e) {
			throw new BeanException("Error getting value from field '%s' on class %s", e, f.toGenericString(), bean.getClass().getName());
		}
	}
	
	public static void setFieldValue(Object bean, Field f, Object val) {
		setFieldValue(bean, f, val, false);
	}
	
	public static void setFieldValue(Object bean, Field f, Object val, boolean makeAccessible){
		try {
			boolean setAccessible = !isPublic(f) && makeAccessible;
			if (setAccessible) {
				f.setAccessible(true);
			}
			try {
				f.set(bean,val);
			} finally {
				if (setAccessible) {
					f.setAccessible(false);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new BeanException("Error setting value on field '%s' on class %s using val %s", e, f.toGenericString(), bean.getClass().getName(), toString(val));
		} catch (IllegalAccessException e) {
			throw new BeanException("Error setting value on field '%s' on class %s using val %s", e, f.toGenericString(), bean.getClass().getName(), toString(val));
		}
	}
	
	private static String toString(Object val) {
		if (val == null) {
			return null;
		}
		if (val.getClass().isArray()) {
			return Arrays.deepToString((Object[]) val);
		}
		if (Collection.class.isAssignableFrom(val.getClass())) {
			return val.toString();
		}
		return val.toString();
	}
	
	public static boolean isReaderMethod(Method m) {
		return m.getParameterTypes().length == 0 && isReaderMethodFromName(m.getName());
	}

	public static boolean isReaderMethodFromName(String methodName){
		return methodName.startsWith("get") || methodName.startsWith("is");	
	}
	
	public static boolean isWriterMethod(Method m) {
		return m.getParameterTypes().length == 1 && isWriterMethodFromName(m.getName());
	}
	
	public static boolean isWriterMethodFromName(String methodName){
		return methodName.startsWith("set");
	}
	
	/**
	 * Extract the name of the bean property for the given method based on annotations or failing that
	 * bean method name conventions (getFoo,setFoo,isFoo...)
	 * 
	 * @param m
	 * @return null if no property name could be determined
	 */
	public static String extractPropertyName(Method m) {
		String name = getNameFromAnnotation(m);
		//How about builder methods?? part of annotations?
		if( name == null ){
			name = extractPropertyNameFromMethod(m.getName());
		}
		return name;
	}
	
	/**
	 * Determine the bean property name based on the method name.
	 * 
	 * @param methodName name of the method
	 * @return null if no property name could be determined
	 */
	public static String extractPropertyNameFromMethod(String methodName){
		if (methodName.startsWith("get")) {
			return StringUtil.lowerFirstChar(methodName.substring(3));
		} else if (methodName.startsWith("is")) {
			return StringUtil.lowerFirstChar(methodName.substring(2));
		} else if (methodName.startsWith("set")) {
			return StringUtil.lowerFirstChar(methodName.substring(3));
		}
		return null;
	}

	public static String extractPropertyName(Field f) {
		String name = getNameFromAnnotation(f);
		if (name == null) {
			name = extractPropertyNameFromField(f.getName());
		}
		return name;
	}
	
	public static String extractPropertyNameFromField(String fieldName) {
		return removeHungarianNotation(fieldName);
	}
	
	private static String removeHungarianNotation(String name){
		if( name.startsWith("_")){
			return name.substring(1);
		}
		if( name.startsWith("m_")){
			return name.substring(2);
		}
		return name;
	}
	
	public static String getNameFromAnnotation(Field f) {
		// todo:handle json field name annotations...
		// todo:allow custom annotations for field names...
		Property annon = f.getAnnotation(Property.class);
		if (annon != null) {
			String name = annon.name().trim();
			if (name.length() > 0) {
				return name;
			}
		}
		return null;
	}

	public static String getNameFromAnnotation(Method m) {
		// todo:handle json field name annotations...
		// todo:allow custom annotations for field names...
		Property annon = m.getAnnotation(Property.class);
		if (annon != null) {
			String name = annon.name().trim();
			if (name.length() > 0) {
				return name;
			}
		}
		return null;
	}
	
	public static boolean isPublic(Class<?> type) {
		return Modifier.isPublic(type.getModifiers());
	}
	
	public static boolean isPublic(Member member) {
		return Modifier.isPublic(member.getModifiers());
	}

	public static boolean isStatic(Member member) {
		return Modifier.isStatic(member.getModifiers());
	}

	public static boolean isTransient(Member member) {
		return Modifier.isTransient(member.getModifiers());
	}

	public static boolean isVolatile(Member member) {
		return Modifier.isVolatile(member.getModifiers());
	}

	public static Method getMethod(Class<?> beanClass, String methodName, Class<?> parameterTypes) {
		try {
			return beanClass.getMethod(methodName, parameterTypes);
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
		return null;
	}
}
