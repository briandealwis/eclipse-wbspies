
package ca.mt.wb.runtime.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class CloseEditorAndPossiblyWindow extends AbstractHandler {

  public Object execute(ExecutionEvent event) throws ExecutionException {
    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
    IEditorPart part = HandlerUtil.getActiveEditor(event);
    if (part != null) {
      window.getActivePage().closeEditor(part, true);
    } else {
      // Trigger an SWT.Close event to pick up any window-close handlers
      window.getShell().close();
    }
    return null;
  }

}
