package ca.mt.wbspies;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.commands.ICommandService;

public class CommandListeningAction extends ActionDelegate implements
IWorkbenchWindowActionDelegate {
	protected IExecutionListener selectionChangingCommandListener;
	protected IWorkbenchWindow window;

	public CommandListeningAction() {
		super();
	}
	
	public void init(IWorkbenchWindow _window) {
		window = _window; 
	}
	
	@Override
	public void run(IAction action) {
//		3.2:	ICommandService cs = (ICommandService)getViewSite().getWorkbenchWindow().getService(ICommandService.class);
		ICommandService cs = (ICommandService)window.getWorkbench().getAdapter(ICommandService.class);
		if(action.isChecked()) {
			if(selectionChangingCommandListener == null) {
				System.out.println("Adding command listener");
				initListener();
				cs.addExecutionListener(selectionChangingCommandListener);
			}
		} else {
			System.out.println("Removing command listener");
			if(selectionChangingCommandListener != null) {
				cs.removeExecutionListener(selectionChangingCommandListener);
			}
			selectionChangingCommandListener = null;
		}
		
		super.run(action);
	}
	
	protected void initListener() {
		selectionChangingCommandListener = new IExecutionListener() {
			public void notHandled(String commandId, NotHandledException exception) {
				System.out.println("Command: " + commandId + " not handled (exception=" + exception+ ")");
			}
			public void postExecuteFailure(String commandId, ExecutionException exception) {
				System.out.println("Command: " + commandId + " executed unsuccessfully (exception=" + exception+ ")");				
			}
			public void postExecuteSuccess(String commandId, Object returnValue) {
				System.out.println("Command: " + commandId + " executed successfully (result=" + returnValue + ")");
			}
			public void preExecute(String commandId, ExecutionEvent event) {
				// ignore
			}
		};
		
	}
}
