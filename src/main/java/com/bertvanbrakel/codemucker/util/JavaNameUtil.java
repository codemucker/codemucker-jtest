package com.bertvanbrakel.codemucker.util;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;

public class JavaNameUtil {

	/**
	 * Extract teh fuly qualified name from the given name, looking up parent if required
	 * @param name
	 * @return
	 */
	public static String getQualifiedName(Name name) {
		if (name.isQualifiedName()) {
			return name.getFullyQualifiedName();
		} else {
			return resolveFqn((SimpleName)name);
		}
	}

	/* package for testing */ static String resolveFqn(SimpleName name) {
		CompilationUnit cu = getCompilationUnit(name);
		String fqdn = resolveFqnFromDeclaredTypes(cu, name);
		if (fqdn == null) {
			fqdn = resolveFqnFromImports(cu, name);
		}
		if( fqdn == null ){
			fqdn = resolveFqdnFromClassLoader(name);
		}
		if (fqdn == null) {
			throw new CodemuckerException("Could not resolve simple name '%s' defined in '%s'", name.getFullyQualifiedName(), getCompilationUnit(name));
		}
		return fqdn;
	}

	private static CompilationUnit getCompilationUnit(ASTNode node) {
		ASTNode root = node.getRoot();
		if (root instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) root;
			return cu;
		}
		throw new CodemuckerException("Can't find compilation unit node");
	}
	
	/* package for testing */ static String resolveFqnFromImports(CompilationUnit cu, SimpleName name) {
		// not a locally declared type, look through imports
		String nameWithDot = "." + name.getIdentifier();
		List<ImportDeclaration> imports = cu.imports();
		for (ImportDeclaration imprt : imports) {
			String fqn = imprt.getName().getFullyQualifiedName();
			if (fqn.equals(name)) {
				return fqn;
			} else if (fqn.endsWith(nameWithDot)) {
				return fqn;
			}
		}
		return null;
	}
	
	static String resolveFqdnFromClassLoader(SimpleName name) {
		String pkg = getPackagePrefixFrom(getCompilationUnit(name));
		return resolveFqdnFromClassLoader(name, pkg, "java.lang.");
	}
	
	static String resolveFqdnFromClassLoader(SimpleName name, String... packagePrefixes) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		for (String prefix : packagePrefixes) {
			try {
				prefix = prefix == null ? "" : prefix;
				Class<?> type = cl.loadClass(prefix + name.getIdentifier());
				return type.getName();
			} catch (ClassNotFoundException e) {
				// do nothing. Just try next prefix
			} catch (NoClassDefFoundError e) {
				// do nothing. Just try next prefix
				System.out.println("bad");
			}
		}
		return null;
	}

	/* package for testing */ static String resolveFqnFromDeclaredTypes(CompilationUnit cu, SimpleName name) {
		TypeDeclaration parentType = getParentTypeOrNull(name);
		return resolveFqnFromDeclaredType(parentType,name);
	}

	static String resolveFqnFromDeclaredType(TypeDeclaration type, SimpleName name) {
		if( type == null ){
			return null;
		}
		String nameIdentifier = name.getIdentifier();
		//declared class types
		String fqdn = resolveFqdnFromChildTypes(type, name);
		if( fqdn != null){
			return fqdn;
		}
		//TODO:enums -does this work?
		//TODO:interfaces
		//TODO: need the '$' int he name as in com.foo.bar.OuterCLass$InnerClass. Is this consistent? the dollar bit?
		//can we rely on it?
		List<BodyDeclaration> bodies = type.bodyDeclarations();
		for( BodyDeclaration body:bodies){
			if( body instanceof EnumDeclaration){
				EnumDeclaration enumDecl = (EnumDeclaration)body;
				if( nameMatches( nameIdentifier, enumDecl.getName())){
					return packageAndName(enumDecl, nameIdentifier);
				}
			}
			if( body instanceof AnnotationTypeDeclaration ){
				AnnotationTypeDeclaration anonDec = (AnnotationTypeDeclaration)body;
				if( nameMatches( nameIdentifier, anonDec.getName())){
					return packageAndName(anonDec, nameIdentifier);
				}
			}
//			if( body instanceof Interface) {
//			
//			}
			
		}
		//TODO:annotations
		
		//not found yet, lets now try parents
		TypeDeclaration parentType = getParentTypeOrNull(type);
		return resolveFqnFromDeclaredType(parentType, name);
	}
	
	private static boolean nameMatches(String haveName, SimpleName name){
		return haveName.matches(name.getIdentifier() );
	}
	
	private static String packageAndName(ASTNode node, String name){
		CompilationUnit cu = getCompilationUnit(node);
		String pkg = getPackagePrefixFrom(cu);
		TypeDeclaration parent = getParentTypeOrNull(node);		
		while (parent != null) {
			pkg = pkg + parent.getName().getIdentifier() + "$";
			parent = getParentTypeOrNull(parent);
		}
		return pkg + name;
	}
	
	private static String resolveFqdnFromChildTypes(TypeDeclaration type, SimpleName name) {
	    TypeDeclaration[] childTypes = type.getTypes();//TODO:get interfaces and enums and stuff too. Filter body decl
		for(AbstractTypeDeclaration childType:childTypes){
			if( name.getIdentifier().matches(childType.getName().getIdentifier())){
				return getPackagePrefixFrom(type) + name;
			}
		}
		return null;
    }
	
	/**
	 * Return the package prefix ending with a full stop if a package has been declared, or empty
	 * if no package has been declared
	 * 
	 * @param node
	 */
	private static String getPackagePrefixFrom(ASTNode node) {
		String pkg = getPackageFor(node);
		return pkg == null ? "" : pkg + ".";
	}
	
	public static String getPackageFor(ASTNode node){
		CompilationUnit cu = getCompilationUnit(node);
		String pkg = null;
		if (cu.getPackage() != null) {
			pkg = cu.getPackage().getName().getFullyQualifiedName();
		}
		return pkg;
	}
	
	private static TypeDeclaration getParentTypeOrNull(ASTNode node) {
		ASTNode parent = node.getParent();
		while (parent != null) {
			if (parent instanceof TypeDeclaration) {
				return (TypeDeclaration) parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

}