/*
 * Written by Brian de Alwis.
 * Released under the <a href="http://unlicense.org">UnLicense</a>
 */
package ca.mt.wb.devtools.tx;

import org.eclipse.jdt.core.IType;

public class ExtendsRelation {
	protected IType superclass;
	protected IType subclass;
	
	ExtendsRelation(IType supercl, IType subcl) {
		superclass = supercl;
		subclass = subcl;
	}

	public IType getSubclass() {
		return subclass;
	}

	public IType getSuperclass() {
		return superclass;
	}
	
	
}
