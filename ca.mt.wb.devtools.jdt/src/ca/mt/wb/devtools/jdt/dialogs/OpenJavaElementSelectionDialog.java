package ca.mt.wb.devtools.jdt.dialogs;

import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.ui.workingsets.WorkingSetFilter;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ca.mt.wb.devtools.jdt.Activator;


public abstract class OpenJavaElementSelectionDialog extends
		FilteredItemsSelectionDialog {
	
	public static final String HANDLE_IDENTIFIER = "handle";

	protected JavaElementFilter filter;
	protected ActionGroup workingSetFilterActionGroup;
	protected ILabelProvider listLabelProvider;
	protected WorkingSetFilter workingSetFilter = new WorkingSetFilter();
	protected String title;

	public OpenJavaElementSelectionDialog(Shell parentShell, boolean multiSelect,
			IRunnableContext context) {
		super(parentShell, multiSelect);
		
		setSelectionHistory(new JavaElementSelectionHistory());

		listLabelProvider = new JavaElementLabelProvider(
				JavaElementLabelProvider.SHOW_BASICS
				| JavaElementLabelProvider.SHOW_POST_QUALIFIED
				| JavaElementLabelProvider.SHOW_RETURN_TYPE
				| JavaElementLabelProvider.SHOW_PARAMETERS
				| JavaElementLabelProvider.SHOW_SMALL_ICONS);
				// | JavaElementLabelProvider.SHOW_OVERLAY_ICONS);
		setListLabelProvider(listLabelProvider);
		setDetailsLabelProvider(new JavaElementDetailsProvider());
	}
	
	/**
	 * Adds or replaces subtitle of the dialog.
	 * Copied from FilteredResourcesSelectionDialog.
	 * 
	 * @param text
	 *            the new subtitle
	 */
	protected void setSubtitle(String text) {
		if (text == null || text.length() == 0) {
			getShell().setText(title);
		} else {
			getShell().setText(title + " - " + text); //$NON-NLS-1$
		}
	}

	public void setTitle(String title) {
		super.setTitle(title);
		this.title = title;
	}

	@Override
	protected ItemsFilter createFilter() {
		// return new ResourceFilter(container, isDerived, typeMask);
		return filter = new JavaElementFilter();
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings s = Activator.getDefault()
			.getDialogSettings().getSection(getDialogSettingsName());
		if(s != null) { return s; }
		return Activator.getDefault().getDialogSettings().addNewSection(getDialogSettingsName());
	}

	protected abstract String getDialogSettingsName();

	@Override
    protected Comparator< ? > getItemsComparator() {
        return new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return getElementName(o1).compareTo(getElementName(o2));
			}};
	}

	@Override
	protected IStatus validateItem(Object item) {
		if(item instanceof IJavaElement) { return Status.OK_STATUS; }
        return new Status(IStatus.ERROR, Activator.getBundleId(), -1, "Invalid item: " + item.getClass().getName(), null);
	}

	@Override
	public String getElementName(Object item) {
		return listLabelProvider.getText(item);
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	protected void fillViewMenu(IMenuManager menuManager) {
		super.fillViewMenu(menuManager);
	
		workingSetFilterActionGroup = new WorkingSetFilterActionGroup(
				getShell(), new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
						String property = event.getProperty();
	
						if (WorkingSetFilterActionGroup.CHANGE_WORKING_SET
								.equals(property)) {
	
							IWorkingSet workingSet = (IWorkingSet) event
									.getNewValue();
	
							if (workingSet != null
									&& !(workingSet.isAggregateWorkingSet() && workingSet
											.isEmpty())) {
								workingSetFilter.setWorkingSet(workingSet);
								setSubtitle(workingSet.getLabel());
							} else {
								IWorkbenchWindow window = PlatformUI
										.getWorkbench()
										.getActiveWorkbenchWindow();
	
								if (window != null) {
									IWorkbenchPage page = window
											.getActivePage();
									workingSet = page.getAggregateWorkingSet();
	
									if (workingSet.isAggregateWorkingSet()
											&& workingSet.isEmpty()) {
										workingSet = null;
									}
								}
	
								workingSetFilter.setWorkingSet(workingSet);
								setSubtitle(null);
							}
	
							scheduleRefresh();
						}
					}
				});
	
		menuManager.add(new Separator());
		workingSetFilterActionGroup.fillContextMenu(menuManager);
	}

	protected String getSearchPatternText() {
		// Just return the whole thing
		return filter.getPattern();
	}

	protected abstract SearchPattern createSearchPattern(String patternText);

	protected boolean acceptMatch(SearchMatch match) {
		return match.getElement() instanceof IJavaElement;
	}
	
	@Override
	protected void fillContentProvider(final AbstractContentProvider contentProvider, final ItemsFilter itemsFilter, final IProgressMonitor progressMonitor)
			throws CoreException {
				IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
				// It'd be nice if we could do some type-hierarchy narrowing here, so that
				//   "Collection.contains(*)" would match all contains methods in the hierarchy.
				// The approach could do: if a type can be extracted (i.e., up until the last period),
				// and it matches a single type, then create a type-hierarchy scope on that type.
				// Otherwise ignore the type and just do a workspace search	    
				// (Or could do a search for all the matching types...)
				SearchPattern searchPattern = createSearchPattern(getSearchPatternText());
				if(searchPattern == null) {
					progressMonitor.setCanceled(true);
					return; 
				}
				try {
					SearchEngine searchEngine = new SearchEngine();
					searchEngine.search(searchPattern,
			                new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
			                scope,
			                new SearchRequestor() {
								@Override
								public void acceptSearchMatch(SearchMatch match) throws CoreException {
									if(progressMonitor.isCanceled()) {
										throw new OperationCanceledException();
									}
									if(acceptMatch(match)) {
										contentProvider.add((IJavaElement)match.getElement(), itemsFilter);
									}
								}},  
			                progressMonitor);
				} catch(CoreException e) {
					progressMonitor.setCanceled(true);
				}
				if (progressMonitor != null) {
					progressMonitor.done();
				}
			}

	public class JavaElementFilter extends ItemsFilter {
		
		public JavaElementFilter() {
			super();
		}
		
		@Override
		public boolean isSubFilter(ItemsFilter filter) {
			return false;
		}
	
		@Override
		public boolean isConsistentItem(Object item) {
			return ((IJavaElement)item).exists();
		}
	
		@Override
		public boolean matchItem(Object item) {
			return matches(listLabelProvider.getText(item));
		}
	}

	public class JavaElementSelectionHistory extends SelectionHistory {

		@Override
		protected Object restoreItemFromMemento(IMemento memento) {
			String handleIdentifier = memento.getString(HANDLE_IDENTIFIER);
			if(handleIdentifier != null) {
				return JavaCore.create(handleIdentifier);
			}
			return null;
		}

		@Override
		protected void storeItemToMemento(Object item, IMemento memento) {
			memento.putString(HANDLE_IDENTIFIER, ((IJavaElement)item).getHandleIdentifier());
		}
	}

	// Stolen from ResourceItemDetailsProvider
    public class JavaElementDetailsProvider extends LabelProvider implements ILabelProviderListener {

		//  	Need to keep our own list of listeners
		private ListenerList listeners = new ListenerList();

		WorkbenchLabelProvider provider = new WorkbenchLabelProvider();

		ILabelDecorator decorator = PlatformUI.getWorkbench()
				.getDecoratorManager().getLabelDecorator();

		/**
		 * Creates a new instance of the class
		 */
		public JavaElementDetailsProvider() {
			super();
			provider.addListener(this);
			decorator.addListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			if (!(element instanceof IJavaElement)) {
				return super.getImage(element);
			}

			IJavaProject res = ((IJavaElement)element).getJavaProject();
			Image img = provider.getImage(res);
			return decorator.decorateImage(img, res);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (!(element instanceof IJavaElement)) {
				return super.getText(element);
			}

			IJavaProject res = ((IJavaElement)element).getJavaProject();
			String str = res.getElementName() + ": " + ((IJavaElement)element).getPath().toOSString();
			return decorator.decorateText(str, res);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#dispose()
		 */
		public void dispose() {
			provider.removeListener(this);
			provider.dispose();

			decorator.removeListener(this);
			decorator.dispose();

			super.dispose();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProviderListener#labelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
		 */
		public void labelProviderChanged(LabelProviderChangedEvent event) {
			Object[] l = listeners.getListeners();
			for (int i = 0; i < listeners.size(); i++) {
				((ILabelProviderListener) l[i]).labelProviderChanged(event);
			}
		}
	}
}
