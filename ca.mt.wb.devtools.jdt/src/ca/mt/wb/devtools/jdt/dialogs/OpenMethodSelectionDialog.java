package ca.mt.wb.devtools.jdt.dialogs;



import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Shell;


public class OpenMethodSelectionDialog extends OpenJavaElementSelectionDialog  {

    public static final String DIALOG_SETTINGS = "ca.mt.wb.devtools.jdt.dialogs.OpenMethodSelectionDialog";

	public OpenMethodSelectionDialog(Shell parentShell, boolean multiSelect,
			IRunnableContext context) {
		super(parentShell, multiSelect, context);
	}
	
	@Override
	protected String getDialogSettingsName() {
		return DIALOG_SETTINGS;
	}

	@Override
	protected SearchPattern createSearchPattern(String patternText) {
		return SearchPattern.createPattern(patternText,
				IJavaSearchConstants.METHOD,
				IJavaSearchConstants.DECLARATIONS,
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_EQUIVALENT_MATCH 
				| SearchPattern.R_PATTERN_MATCH);	
	}

}
