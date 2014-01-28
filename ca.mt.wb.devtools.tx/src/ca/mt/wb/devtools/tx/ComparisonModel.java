/*
 * Written by Brian de Alwis.
 * Released under the <a href="http://unlicense.org">UnLicense</a>
 */
package ca.mt.wb.devtools.tx;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

public class ComparisonModel {
    protected IType types[] = new IType[0];
    protected ITypeHierarchy hierarchies[] = new ITypeHierarchy[0];
    
    public ComparisonModel() {}
    
    public void add(IType t) {
        if(contains(t)) { return; } // skip it
        IType newTypes[] = new IType[types.length + 1];
        ITypeHierarchy newHierarchies[] = new ITypeHierarchy[hierarchies.length + 1];
        System.arraycopy(types, 0, newTypes, 0, types.length);
        newTypes[types.length] = t;
        System.arraycopy(hierarchies, 0, newHierarchies, 0, hierarchies.length);
        try {
            newHierarchies[hierarchies.length] = t.newSupertypeHierarchy(new NullProgressMonitor());
        } catch (JavaModelException e) {
             /* do nothing */
        }
        types = newTypes;
        hierarchies = newHierarchies;
    }
  
    public boolean remove(IType t) {
        int index = indexOf(t);
        if(index < 0) { return false; }
        IType newTypes[] = new IType[types.length - 1];
        ITypeHierarchy newHierarchies[] = new ITypeHierarchy[hierarchies.length - 1];
        System.arraycopy(types, 0, newTypes, 0, index);
        System.arraycopy(hierarchies, 0, newHierarchies, 0, index);
        if(index + 1 < types.length) {
            System.arraycopy(types, index + 1, newTypes, index, types.length - index -1);
            System.arraycopy(hierarchies, index + 1, newHierarchies, index, types.length - index - 1);
        }
        types = newTypes;
        hierarchies = newHierarchies;
        return true;
    }
    
    public boolean contains(IType t) {
        return indexOf(t) >= 0;
    }
    
    protected int indexOf(IType t) {
        for(int i = 0; i < types.length; i++) {
            if(t.equals(types[i])) { return i; }
        }
        return -1;
    }
    
    public ITypeHierarchy hierarchyFor(IType t) {
        int i = indexOf(t);
        if(i < 0) { return null; }
        return hierarchies[i];
    }
    
    public IType getSuperclassFor(IType t) {
        for(int i = 0; i < hierarchies.length; i++) {
            IType sup = hierarchies[i].getSuperclass(t);
            if(sup != null) { return sup; }
        }
        return null;
    }
    
    public IType[] getSuperinterfacesFor(IType t) {
        Set<IType> supers = new HashSet<IType>();
        for(int i = 0; i < hierarchies.length; i++) {
            IType si[] = hierarchies[i].getSuperInterfaces(t);
            for(int j = 0; j < si.length; j++) {
                supers.add(si[j]);
            }
        }
        return (IType[])supers.toArray(new IType[supers.size()]);
    }

    public ComparisonModel copy() {
        ComparisonModel newInstance = new ComparisonModel();
        newInstance.types = types;
        newInstance.hierarchies = hierarchies;
        return newInstance;
    }
    
    public Set<IType> getTypes() {
        Set<IType> l = new HashSet<IType>();
        Collections.addAll(l, types);
        return l;
    }

    public Set<IType> getAllTypes() {
        Set<IType> l = new HashSet<IType>();
        for(int i = 0; i < hierarchies.length; i++) {
            Collections.addAll(l, hierarchies[i].getAllTypes());
        }
        return l;
    }
}
