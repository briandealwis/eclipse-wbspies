package ca.mt.wbspies.selspy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

public class ViewerContentProvider implements ITreeContentProvider {
	protected Viewer viewer;
	protected Object root; 

	public ViewerContentProvider() {
		super();
	}

	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof Collection) {
			return ((Collection<?>)inputElement).toArray();
		} else if(inputElement instanceof Object[]) {
			if(inputElement == root) {
				Object arr[] = (Object[]) inputElement;
				if(arr.length == 2 && arr[1] instanceof ContentOutline) {
					Object ret[] = new Object[] { arr[0], arr[1], null };
					ret[2] = ((ContentOutline)arr[1]).getCurrentPage();
					return ret;
				}
			}
			return (Object[])inputElement;
		}
		ArrayList<Object> results = new ArrayList<Object>();
		if(inputElement instanceof IStructuredSelection) {
			Collections.addAll(results, ((IStructuredSelection)inputElement).toArray());
		}
		if(inputElement instanceof IWorkbenchPart) {
			results.add("id=" + ((IWorkbenchPart)inputElement).getSite().getId());
			results.add("plugin=" + ((IWorkbenchPart)inputElement).getSite().getPluginId());
		}
		if(inputElement instanceof Class) {
			if(!((Class<?>)inputElement).isInterface()) {
				getImplementingInterfaces((Class<?>)inputElement, results);
			}
		} else if(!(inputElement instanceof String || inputElement instanceof Number)) {
			results.add(inputElement.getClass());
		}
		return results.toArray();
	}

	private void getImplementingInterfaces(Class<?> clazz, List<Object> results) {
		getImplementingInterfaces(clazz, results, new HashSet<Class<?>>());
	}

	private void getImplementingInterfaces(Class<?> clazz, List<Object> results, Set<Class<?>> seen) {
		if(seen.contains(clazz)) { return; }
		seen.add(clazz);
		for(Class<?> c : clazz.getInterfaces()) {
			results.add(c);
			getImplementingInterfaces(c, results, seen);
		}
	}

	public void dispose() {
	}

	public void inputChanged(Viewer _viewer, Object _oldInput, Object _newInput) {
		viewer = _viewer;
		root = _newInput;
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if(element instanceof IStructuredSelection) { return true; }
		return getChildren(element).length > 0;
	}

}
