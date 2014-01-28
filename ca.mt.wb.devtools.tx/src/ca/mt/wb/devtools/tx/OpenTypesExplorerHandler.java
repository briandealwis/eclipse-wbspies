/*
 * Written by Brian de Alwis.
 * Released under the <a href="http://unlicense.org">UnLicense</a>
 */
package ca.mt.wb.devtools.tx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
        if (types == null || types.isEmpty()) {
            return null;
        }
        try {
            IWorkbenchWindow win = HandlerUtil.getActiveWorkbenchWindow(event);
            IWorkbenchPage page = win.getWorkbench().showPerspective(TypesExplorationPerspective.perspectiveID, win);
            InheritanceView view = (InheritanceView) page.showView(InheritanceView.viewID);
            view.addTypes(types);
        } catch (WorkbenchException e) {
            throw new ExecutionException("Error occurred", e);
        }
        return null;
    }

    private Collection<IType> getTypes(ExecutionEvent event) {
        Object t = event.getParameter(TYPES_PARAMETER_ID);
        if (t == null) {
            IWorkbenchWindow win = HandlerUtil.getActiveWorkbenchWindow(event);
            ISelection selection = HandlerUtil.getCurrentSelection(event);
            if (win == null || selection.isEmpty()) {
                return null;
            }

            if (selection instanceof JavaTextSelection) {
                try {
                    IJavaElement[] resolved = ((JavaTextSelection) selection).resolveElementAtOffset();
                    if (resolved.length == 0) {
                        return null;
                    }
                    t = resolved;
                } catch (JavaModelException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (selection instanceof ITextSelection && HandlerUtil.getActivePart(event) instanceof JavaEditor) {
                JavaEditor editor = (JavaEditor) HandlerUtil.getActivePart(event);
                try {
                    t = SelectionConverter.getElementAtOffset(editor);
                    if (t == null) {
                        return null;
                    }
                } catch (JavaModelException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (selection instanceof IStructuredSelection) {
                t = ((IStructuredSelection) selection).toList();
            } else {
                return null;
            }
        }
        if (t instanceof IType) {
            return Collections.singleton((IType) t);
        }
        List<IType> results = new ArrayList<IType>();
        if (t instanceof Object[]) {
            for (Object o : (Object[]) t) {
                if (o instanceof IType) {
                    results.add((IType) o);
                }
            }
        } else if (t instanceof Collection< ? >) {
            for (Object o : (Collection< ? >) t) {
                if (o instanceof IType) {
                    results.add((IType) o);
                }
            }
        } else {
            return null;
        }
        return results;
    }

}
