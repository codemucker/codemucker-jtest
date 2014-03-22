package org.codemucker.jfind;

/**
 * Empty implementation of the root visitor
 *
 */
public class BaseRootVisitor implements RootVisitor {

	@Override
	public boolean visit(Root root) {
		return true;
	}

	@Override
	public void endVisit(Root root) {
	}

	@Override
	public boolean visit(RootResource resource) {
		return true;
	}

	@Override
	public void endVisit(RootResource resource) {
	}
}