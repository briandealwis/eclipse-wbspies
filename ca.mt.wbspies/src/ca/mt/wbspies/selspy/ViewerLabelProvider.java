package ca.mt.wbspies.selspy;

import java.lang.reflect.Modifier;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class ViewerLabelProvider extends LabelProvider {
	
	protected ResourceManager resMgr = new LocalResourceManager(JFaceResources.getResources());
	public ViewerLabelProvider() {
	}

	public Image getImage(Object element) {
		try {
			if(element instanceof Class) {
				Class cl = (Class)element;
				boolean isInner = cl.isMemberClass() || cl.isLocalClass();
				boolean isPresent = false;	// future: perhaps: is source in plugin?
				return resMgr.createImage(JavaElementImageProvider.getTypeImageDescriptor(
						isInner, false, getFlags(cl), isPresent));
			} else if(element instanceof ISelection) {
				if(element instanceof IStructuredSelection) { return getImage(IStructuredSelection.class); }
				if(element instanceof ITextSelection) { return getImage(ITextSelection.class); }
				return getImage(element.getClass());
			} else if(element instanceof IWorkbenchPart) {
				return ((IWorkbenchPart)element).getTitleImage();
			} else if(element instanceof IAdaptable) {
				ImageDescriptor id;
				IWorkbenchAdapter wa = (IWorkbenchAdapter)((IAdaptable)element)
				.getAdapter(IWorkbenchAdapter.class);
				if(wa != null && (id = wa.getImageDescriptor(element)) != null) {
					return resMgr.createImage(id);
				} 
			}
		} catch(DeviceResourceException e) { /* do nothing */ }
        return null;
	}

	private int getFlags(Class cl) {
		// OK, from the comments, I think we could have just done
		// return cl.getModifiers();
		int mod = cl.getModifiers();
		int flags = 0;
		if(cl.isInterface()) { flags |= Flags.AccInterface; }
		if(cl.isAnnotation()) { flags |= Flags.AccAnnotation; }
		if(cl.isEnum()) { flags |= Flags.AccEnum; }
		if(cl.isSynthetic()) { flags |= Flags.AccSynthetic; }
		if(Modifier.isAbstract(mod)) { flags |= Flags.AccAbstract; }
		if(Modifier.isFinal(mod)) { flags |= Flags.AccFinal; }
		if(Modifier.isNative(mod)) { flags |= Flags.AccNative; }
		if(Modifier.isPrivate(mod)) { flags |= Flags.AccPrivate; }
		if(Modifier.isProtected(mod)) { flags |= Flags.AccProtected; }
		if(Modifier.isPublic(mod)) { flags |= Flags.AccPublic; }
		if(Modifier.isStatic(mod)) { flags |= Flags.AccStatic; }
		if(Modifier.isStrict(mod)) { flags |= Flags.AccStrictfp; }
		if(Modifier.isSynchronized(mod)) { flags |= Flags.AccSynchronized; }
		if(Modifier.isTransient(mod)) { flags |= Flags.AccTransient; }
		if(Modifier.isVolatile(mod)) { flags |= Flags.AccVolatile; }
		return flags;
	}

	public String getText(Object element) {
		if(element instanceof Class) {
			String fn = ((Class)element).getName(); 
			int lastDotIndex = fn.lastIndexOf('.');
			if(lastDotIndex < 0) { return fn; }
			return fn.substring(lastDotIndex + 1) + " - " + fn.substring(0, lastDotIndex);
		} else if(element instanceof ISelection) {
			if(element instanceof IStructuredSelection) {
				return "IStructuredSelection";
			} else if(element instanceof ITextSelection) {
				ITextSelection ts = (ITextSelection)element;
				return "ITextSelection[" + ts.getStartLine() + "-" + ts.getEndLine() + ": offset=" + ts.getOffset() + " length=" + 
					ts.getLength() + "]{" + ts.getText() + "}";
			}
			StringBuffer result = new StringBuffer();
			result.append(getText(element.getClass()));
			Class ifaces[] = element.getClass().getInterfaces();
			if(ifaces.length > 0) {
				result.append(" interfaces: ");
				for(Class iface : ifaces) {
					result.append(iface.getName());
					result.append(" ");
				}
			}
			return result.toString();
		} else if(element instanceof IWorkbenchPart) {
			IWorkbenchPart part = (IWorkbenchPart)element;
			return part.getTitle() + " [" + part.getSite().getId() + "]";
		} else if(element instanceof IAdaptable) {
    		String label = null;
            IWorkbenchAdapter wa = (IWorkbenchAdapter)((IAdaptable)element)
                .getAdapter(IWorkbenchAdapter.class);
            if(wa != null && (label = wa.getLabel(element)) != null) {
            	if(element instanceof IJavaElement) {
            		return label + " {" + ((IJavaElement)element).getHandleIdentifier() + "}";
            	}
            	return label;
            }
        }
        return element.toString();
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
		if(resMgr != null) { resMgr.dispose(); }
		resMgr = null;
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}
