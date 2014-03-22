package org.codemucker.jfind;


public interface RootVisitor {
	public boolean visit(Root root);
	public void endVisit(Root root);
	public boolean visit(RootResource resource);
	public void endVisit(RootResource resource);
}