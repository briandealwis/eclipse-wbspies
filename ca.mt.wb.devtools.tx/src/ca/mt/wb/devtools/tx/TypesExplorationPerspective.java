/*
 * Written by Brian de Alwis.
 * Released under the <a href="http://unlicense.org">UnLicense</a>
 */
package ca.mt.wb.devtools.tx;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class TypesExplorationPerspective implements IPerspectiveFactory {
    public static String perspectiveID = "ca.mt.wb.devtools.tx.typesPerspective";

    public void createInitialLayout(IPageLayout layout) {
        // Consider using a standalone view?  Doesn't look quite right: I'd prefer having
        // some way of turning off the global tool bar.
//         layout.addStandaloneView(InheritanceView.viewID, false, IPageLayout.TOP, 1, layout.getEditorArea());
        layout.addView(InheritanceView.viewID, IPageLayout.TOP, 1, layout.getEditorArea());
        layout.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY, IPageLayout.LEFT, .2f, InheritanceView.viewID);
        IFolderLayout javadocFolder = layout.createFolder("javadoc", IPageLayout.BOTTOM, 0.5f, InheritanceView.viewID);
        javadocFolder.addView(JavaUI.ID_JAVADOC_VIEW);
        IFolderLayout sourceFolder = layout.createFolder("source", IPageLayout.RIGHT, 0.5f, "javadoc");
        sourceFolder.addView(JavaUI.ID_SOURCE_VIEW);
        layout.addFastView(IPageLayout.ID_EDITOR_AREA); // maybe works?
    }

}
