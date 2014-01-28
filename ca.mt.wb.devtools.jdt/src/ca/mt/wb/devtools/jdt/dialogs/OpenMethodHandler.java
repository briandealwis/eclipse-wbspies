package ca.mt.wb.devtools.jdt.dialogs;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenMethodHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow win = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        OpenJavaElementSelectionDialog dialog = new OpenMethodSelectionDialog(win.getShell(), true, win.getWorkbench()
                .getProgressService());
        dialog.setTitle("Open Method");
        dialog.setMessage("Select method to open (? = any character, * = any String)");
        int result = dialog.open();
        if (result == OpenJavaElementSelectionDialog.OK) {
            for (Object element : dialog.getResult()) {
                if (element instanceof IJavaElement) {
                    try {
                        JavaUI.openInEditor((IJavaElement) element, true, true);
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
