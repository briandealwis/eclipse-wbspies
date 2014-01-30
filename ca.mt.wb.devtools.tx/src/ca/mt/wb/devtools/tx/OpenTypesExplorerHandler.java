/*
 * Written by Brian de Alwis.
 * Released under the <a href="http://unlicense.org">UnLicense</a>
 */
package ca.mt.wb.devtools.tx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenTypesExplorerHandler extends AbstractHandler {

    private static final String TYPES_PARAMETER_ID = "types";

    public Object execute(ExecutionEvent event) throws ExecutionException {
        Collection<IType> types = getTypes(event);
        if (types != null && !types.isEmpty()) {
            try {
                IWorkbenchWindow win = HandlerUtil.getActiveWorkbenchWindow(event);
                IWorkbenchPage page = win.getWorkbench().showPerspective(TypesExplorationPerspective.perspectiveID, win);
                InheritanceView view = (InheritanceView) page.showView(InheritanceView.viewID);
                view.addTypes(types);
            } catch (WorkbenchException e) {
                throw new ExecutionException("Error occurred", e);
            }
        }
        return null;
    }

    private Collection<IType> getTypes(ExecutionEvent event) {
        Object t = event.getParameter(TYPES_PARAMETER_ID);
        if (t == null) {
            ISelection selection = HandlerUtil.getCurrentSelection(event);
            if (selection.isEmpty()) {
                return Collections.emptyList();
            }
            if (selection instanceof IStructuredSelection) {
                return adapt((IStructuredSelection) selection, IType.class);
            }
            if (selection instanceof JavaTextSelection) {
                try {
                    IJavaElement[] resolved = ((JavaTextSelection) selection).resolveElementAtOffset();
                    if (resolved.length == 0) {
                        return Collections.emptyList();
                    }
                    t = resolved;
                } catch (JavaModelException e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            } else if (selection instanceof ITextSelection && HandlerUtil.getActivePart(event) instanceof JavaEditor) {
                JavaEditor editor = (JavaEditor) HandlerUtil.getActivePart(event);
                try {
                    t = SelectionConverter.getElementAtOffset(editor);
                    if (t == null) {
                        return Collections.emptyList();
                    }
                } catch (JavaModelException e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        }
        if (t instanceof IType) {
            return Collections.singleton((IType) t);
        }
        List<IType> results = new ArrayList<IType>();
        if (t instanceof Object[]) {
            for (Object o : (Object[]) t) {
                IType type = adapt(o, IType.class);
                if (type != null) {
                    results.add(type);
                }
            }
        } else if (t instanceof Collection< ? >) {
            for (Object o : (Collection< ? >) t) {
                IType type = adapt(o, IType.class);
                if (type != null) {
                    results.add(type);
                }
            }
        } else {
            return null;
        }
        return results;
    }

    public static <T> Collection<T> adapt(ISelection sel, Class<T> clazz) {
        if (sel instanceof IStructuredSelection && !sel.isEmpty()) {
            Set<T> results = new HashSet<T>();
            for (Object o : ((IStructuredSelection) sel).toArray()) {
                T t = adapt(o, clazz);
                if (t != null) {
                    results.add(t);
                }
            }
            return results;
        }
        return Collections.emptyList();
    }

    static <T> T adapt(Object o, Class<T> clazz) {
        if (clazz.isInstance(o)) {
            return clazz.cast(o);
        }
        if (o instanceof IAdaptable) {
            Object a = ((IAdaptable) o).getAdapter(clazz);
            if (a != null && clazz.isInstance(o)) {
                return clazz.cast(o);
            }
        }
        Object a = Platform.getAdapterManager().getAdapter(o, clazz);
        if (a != null && clazz.isInstance(o)) {
            return clazz.cast(o);
        }
        return null;
    }

}
