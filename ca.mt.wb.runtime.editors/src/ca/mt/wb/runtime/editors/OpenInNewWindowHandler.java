package ca.mt.wb.runtime.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenInNewWindowHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
		IWorkbenchWindow activeWindow = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		String perspectiveId = null;
		if (activeWindow.getActivePage().getPerspective() != null) {
			perspectiveId = activeWindow.getActivePage().getPerspective()
					.getId();
		}
		IWorkbenchWindow newWindow;
		try {
			newWindow = activeWindow.getWorkbench().openWorkbenchWindow(
					perspectiveId, null);
		} catch (WorkbenchException e) {
			throw new ExecutionException("Unable to open new window", e);
		}
		try {
			newWindow.getActivePage().openEditor(editor.getEditorInput(),
					editor.getEditorSite().getId());
		} catch (PartInitException e) {
			throw new ExecutionException("Unable to open new editor", e);
		}
		editor.getEditorSite().getPage().closeEditor(editor, true);
		return null;
	}

}
