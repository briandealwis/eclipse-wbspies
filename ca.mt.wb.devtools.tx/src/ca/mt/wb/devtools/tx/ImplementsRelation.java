/*
 * Written by Brian de Alwis.
 * Released under the <a href="http://unlicense.org">UnLicense</a>
 */
package ca.mt.wb.devtools.tx;

import org.eclipse.jdt.core.IType;

public class ImplementsRelation {

	protected IType superinterface;
	protected IType subinterface;
	
	ImplementsRelation(IType superif, IType subif) {
		superinterface = superif;
		subinterface = subif;
	}

	public IType getSubinterface() {
		return subinterface;
	}

	public IType getSuperinterface() {
		return superinterface;
	}
}
