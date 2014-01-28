/*
 * Written by Brian de Alwis.
 * Released under the <a href="http://unlicense.org">UnLicense</a>
 */
package ca.mt.wb.devtools.tx;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;

public class IVTypeProvider implements IGraphEntityContentProvider {

	ComparisonModel model = null;
    Viewer viewer;
    
    
	public IVTypeProvider(Viewer _viewer) {
        viewer = _viewer;
	}

    private boolean isComparisonType(IType type) {
        return ((ComparisonModel)viewer.getInput()).contains(type);
    }

	public void dispose() {
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput instanceof ComparisonModel) {
			model = (ComparisonModel)newInput;
		}
	}
	
	public double getWeight(Object entity1, Object entity2) {
        if(entity1 instanceof IType && entity2 instanceof IType) {
            if(isComparisonType((IType)entity1) && isComparisonType((IType)entity2)) {
                return 10;
            }
        }
        return 200;
	}

	public Object[] getElements(Object o) {
        if(o instanceof ComparisonModel) {
            return ((ComparisonModel)o).getAllTypes().toArray();
        }
        return new Object[0];
    }
    
    public Object[] getConnectedTo(Object o) {
		if(!(o instanceof IType)) { return new Object[0]; }

        Set<IType> linked = new HashSet<IType>();
		IType t = (IType)o;
		IType sup;
		if((sup = model.getSuperclassFor(t)) != null) {
		    linked.add(sup);
		}
		
		IType ifaces[] = model.getSuperinterfacesFor(t);
		for(int j = 0; j < ifaces.length; j++) {
		    linked.add(ifaces[j]);
		}
		return linked.toArray();
	}
}
