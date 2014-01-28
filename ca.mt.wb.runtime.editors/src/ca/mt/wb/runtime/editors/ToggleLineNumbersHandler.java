package ca.mt.wb.runtime.editors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public class ToggleLineNumbersHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore store = EditorsUI.getPreferenceStore();
		Boolean isVisible = store
				.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER);
		store.setValue(
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER,
				!isVisible);
		return null;
	}
}
