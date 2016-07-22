/*
 * Stripped-down Java Browsing perspective; this uses standalone views
 * to avoid Eclipse UI junk.
 * Placed into public domain by Brian de Alwis.
 * @author bsd
 */
package ca.mt.wb.devtools.jdt;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class StrippedJavaBrowsingPageLayout implements IPerspectiveFactory {
    public static String perspectiveID = "ca.mt.wb.jdt.strippedJavaBrowsingPerspective";
    
    public StrippedJavaBrowsingPageLayout() {
    }

    private boolean stackBrowsingViewsVertically() {
//        import org.eclipse.jdt.ui;
        return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.BROWSING_STACK_VERTICALLY);
        /* Sadly this doesn't work
         * return PlatformUI.getPreferenceStore().getBoolean("org.eclipse.jdt.ui.browsing.stackVertically");
         */
//        return true;
    }
    
    public void createInitialLayout(IPageLayout layout) {
        String relativePartId = IPageLayout.ID_EDITOR_AREA;
        int relativePos = stackBrowsingViewsVertically()
            ? IPageLayout.BOTTOM : IPageLayout.RIGHT;

        layout.addStandaloneView(JavaUI.ID_PROJECTS_VIEW, false, 
                stackBrowsingViewsVertically() ? IPageLayout.LEFT : IPageLayout.TOP,
                (float) 0.15, IPageLayout.ID_EDITOR_AREA);
        relativePartId = JavaUI.ID_PROJECTS_VIEW;
        layout.addStandaloneView(JavaUI.ID_PACKAGES_VIEW, false, relativePos,
                (float) 0.25, relativePartId);
        relativePartId = JavaUI.ID_PACKAGES_VIEW;
        layout.addStandaloneView(JavaUI.ID_TYPES_VIEW, false, relativePos,
                (float) 0.33, relativePartId);
        layout.addStandaloneView(JavaUI.ID_MEMBERS_VIEW, false, relativePos,
                (float) 0.50, JavaUI.ID_TYPES_VIEW);
        
        layout.createPlaceholderFolder("bsd.eh.default.folder",
                IPageLayout.BOTTOM, 0.2f, IPageLayout.ID_EDITOR_AREA);

        // action sets
//        layout.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
        layout.addActionSet("org.eclipse.debug.ui.breakpointActionSet");
        layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
        layout.addActionSet(JavaUI.ID_ACTION_SET);
        layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
        layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		layout.addActionSet("org.eclipse.ui.WorkingSetActionSet");

        // views - java
        layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
        layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);
        layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
        layout.addShowViewShortcut(JavaUI.ID_PROJECTS_VIEW);
        layout.addShowViewShortcut(JavaUI.ID_PACKAGES_VIEW);
        layout.addShowViewShortcut(JavaUI.ID_TYPES_VIEW);
        layout.addShowViewShortcut(JavaUI.ID_MEMBERS_VIEW);
        layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
        layout.addShowViewShortcut(JavaUI.ID_JAVADOC_VIEW);

        // views - search
        layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

        // views - debugging
        layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView"); // was: IConsoleConstants.ID_CONSOLE_VIEW

        // views - standard workbench
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
        layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);
        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);

        // new actions - Java project creation wizard
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewEnumCreationWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewAnnotationCreationWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard");     //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$
        
        layout.addPerspectiveShortcut(perspectiveID);
        layout.addPerspectiveShortcut(JavaUI.ID_PERSPECTIVE);
        layout.addPerspectiveShortcut("org.eclipse.team.ui.TeamSynchronizingPerspective"); //$NON-NLS-1$
        layout.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective"); //$NON-NLS-1$
        layout.addPerspectiveShortcut(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);     
        layout.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective"); //$NON-NLS-1$

    }
}
