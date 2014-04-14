package ca.mt.wb.devtools.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenCommandHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		OpenCommandHandlerDialog dialog = new OpenCommandHandlerDialog(
				HandlerUtil.getActiveShell(event));
		dialog.open();
		return null;
	}

}
