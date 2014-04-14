package ca.mt.wb.devtools.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.ui.editor.context.InputContext;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.pde.internal.ui.editor.plugin.PluginInputContext;
import org.eclipse.pde.internal.ui.search.ManifestEditorOpener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;

public class OpenCommandHandlerDialog extends TrayDialog {
	protected static final String XP_COMMANDS = "org.eclipse.ui.commands";
	protected static final Object ELMT_COMMAND = "command";
	protected static final String ATTR_ID = "id";
	protected static final String ATTR_NAME = "name";
	protected static final String ATTR_DEFAULTHANDLER = "defaultHandler";

	protected static final String XP_HANDLERS = "org.eclipse.ui.handlers";
	protected static final Object ELMT_HANDLER = "handler";
	protected static final String ATTR_COMMANDID = "commandId";
	protected static final String ATTR_CLASS = "class";

	private Text _commandPattern;
	private ListViewer _handlersViewer;
	private ListViewer _commandsViewer;
	private AtomicInteger _pendingModifyCount = new AtomicInteger(0);

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public OpenCommandHandlerDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		validate();
		return result;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 2;

		Label lblCommand = new Label(container, SWT.NONE);
		lblCommand.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		lblCommand.setText("Enter command prefix or pattern:");

		_commandPattern = new Text(container, SWT.BORDER | SWT.H_SCROLL
				| SWT.SEARCH | SWT.CANCEL);
		_commandPattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				_pendingModifyCount.incrementAndGet();
				_commandPattern.getDisplay().timerExec(100, new Runnable() {
					@Override
					public void run() {
						if (_pendingModifyCount.decrementAndGet() == 0) {
							_commandsViewer.setInput(_commandPattern.getText());
						}
					}
				});
			}
		});
		_commandPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));
		_commandPattern.setMessage("* and ? ok");

		_commandsViewer = new ListViewer(container, SWT.SINGLE);
		_commandsViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_commandsViewer
				.addPostSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						_handlersViewer.setInput(((IStructuredSelection) event
								.getSelection()).getFirstElement());
					}
				});

		_handlersViewer = new ListViewer(container, SWT.SINGLE);
		_handlersViewer.getList().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_handlersViewer
				.addPostSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						validate();
					}
				});

		_commandsViewer.setSorter(new ViewerSorter());
		_commandsViewer.setContentProvider(new IStructuredContentProvider() {
			Collection<IPluginElement> commands;

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				commands = null;
				if (newInput instanceof String) {
					commands = lookupCommands((String) newInput);
				}
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return commands == null ? new Object[0] : commands.toArray();
			}

			@Override
			public void dispose() {
			}
		});
		_commandsViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IPluginElement) {
					IPluginElement ce = (IPluginElement) element;
					if (ELMT_COMMAND.equals(ce.getName())
							&& ce.getAttribute(ATTR_ID) != null) {
						return ce.getAttribute(ATTR_ID).getValue();
					}
				}
				return super.getText(element);
			}
		});

		_handlersViewer.setSorter(new ViewerSorter());
		_handlersViewer.setContentProvider(new IStructuredContentProvider() {
			Collection<IPluginElement> handlers;

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				handlers = null;
				if (newInput instanceof IPluginElement) {
					handlers = lookupHandlers((IPluginElement) newInput);
				}

			}

			@Override
			public Object[] getElements(Object inputElement) {
				return handlers == null ? new Object[0] : handlers.toArray();
			}

			@Override
			public void dispose() {

			}
		});
		_handlersViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IPluginElement) {
					IPluginElement ce = (IPluginElement) element;
					if (ELMT_HANDLER.equals(ce.getName())
							&& ce.getAttribute(ATTR_CLASS) != null) {
						return ce.getAttribute(ATTR_CLASS).getValue();
					}
					if (ELMT_COMMAND.equals(ce.getName())
							&& ce.getAttribute(ATTR_DEFAULTHANDLER) != null) {
						return ce.getAttribute(ATTR_DEFAULTHANDLER).getValue();
					}
				}
				return super.getText(element);
			}
		});

		return container;
	}

	@Override
	protected void okPressed() {
		ISelection selection = _handlersViewer.getSelection();
		if (!selection.isEmpty()
				&& selection instanceof IStructuredSelection
				&& ((IStructuredSelection) selection).getFirstElement() instanceof IPluginElement) {
			IPluginElement ce = (IPluginElement) ((IStructuredSelection) selection)
					.getFirstElement();
			openElement(ce);
		}
		super.okPressed();
	}

	private void openElement(IPluginElement object) {
		// Snarfed from
		// org.eclipse.pde.internal.ui.search.dialogs.PluginArtifactSearchHandler
		IEditorPart editorPart = ManifestEditor.open(object, true);
		if (editorPart instanceof ManifestEditor) {
			ManifestEditor editor = (ManifestEditor) editorPart;
			InputContext context = editor.getContextManager().findContext(PluginInputContext.CONTEXT_ID);
			IDocument document = context.getDocumentProvider().getDocument(context.getInput());
			// getAttributeMatch doesn't know how to deal with IPluginElements
			IRegion region = ManifestEditorOpener.getAttributeMatch(editor,
					object.getParent(), document);
			editor.openToSourcePage(object.getParent(), region.getOffset(),
					region.getLength());
		}
	}

	protected void validate() {
		getButton(IDialogConstants.OK_ID).setEnabled(
				!_handlersViewer.getSelection().isEmpty());
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(800, 300);
	}

	protected Collection<IPluginElement> lookupCommands(String pattern) {
		if (pattern == null || pattern.trim().isEmpty()) {
			return null;
		}
		pattern = pattern.trim();
		if (!pattern.endsWith("*")) {
			pattern = pattern + "*";
		}
		pattern = pattern.trim().replace(".", "\\.").replace("*", ".*");
		ArrayList<IPluginElement> commandDefinitions = new ArrayList<IPluginElement>();

		for (IPluginExtension ext : getExtensions(XP_COMMANDS, true)) {
			for (IPluginObject obj : ext.getChildren()) {
				if (obj instanceof IPluginElement) {
					IPluginElement command = (IPluginElement) obj;
					if (ELMT_COMMAND.equals(command.getName())
							&& ((command.getAttribute(ATTR_NAME) != null && command
									.getAttribute(ATTR_NAME).getValue()
									.matches(pattern)) || (command
									.getAttribute(ATTR_ID) != null && command
									.getAttribute(ATTR_ID).getValue()
									.matches(pattern)))) {
						commandDefinitions.add(command);
					}
				}
			}
		}
		return commandDefinitions;
	}

	protected Collection<IPluginElement> lookupHandlers(
			IPluginElement commandDefinition) {
		ArrayList<IPluginElement> commandHandlers = new ArrayList<IPluginElement>();

		String commandId = commandDefinition.getAttribute(ATTR_ID).getValue();
		if (commandDefinition.getAttribute(ATTR_DEFAULTHANDLER) != null) {
			commandHandlers.add(commandDefinition);
		}

		for (IPluginExtension ext : getExtensions(XP_HANDLERS, true)) {
			for (IPluginObject obj : ext.getChildren()) {
				if (obj instanceof IPluginElement
						&& ELMT_HANDLER.equals(obj.getName())
						&& commandId.equals(((IPluginElement) obj)
								.getAttribute(ATTR_COMMANDID).getValue())) {
					commandHandlers.add((IPluginElement) obj);
				}
			}
		}
		return commandHandlers;
	}

	private Collection<IPluginExtension> getExtensions(String extpoint, boolean active) {
		List<IPluginExtension> extensions = new ArrayList<IPluginExtension>();
		PluginModelManager manager = PDECore.getDefault().getModelManager();
		for(IPluginModelBase model : manager.getActiveModels(active)) {			
			for(IPluginExtension ext : model.getPluginBase().getExtensions()) {
				if (extpoint.equals(ext.getPoint())) {
					extensions.add(ext);
				}
			}	
		}
		return extensions;
	}
}
