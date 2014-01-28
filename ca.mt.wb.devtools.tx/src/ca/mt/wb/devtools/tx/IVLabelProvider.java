/*
 * Written by Brian de Alwis.
 * Released under the <a href="http://unlicense.org">UnLicense</a>
 */
package ca.mt.wb.devtools.tx;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.zest.core.viewers.IEntityConnectionStyleProvider;
import org.eclipse.zest.core.widgets.ZestStyles;

public class IVLabelProvider implements ILabelProvider, IEntityConnectionStyleProvider {
    JavaElementImageProvider imageProvider = new JavaElementImageProvider();
    Viewer viewer;
    
    public IVLabelProvider(Viewer _viewer) {
        viewer = _viewer;
    }

    public void addListener(ILabelProviderListener listener) {
        // Igmore: Nobody listens to me anyways
    }

    public void dispose() {
        imageProvider.dispose();
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
        // Ignore: nobody listens to me anyways
    }

    public Image getImage(Object element) {
        if(element instanceof IJavaElement) {
            if(element instanceof IType) {
                int flag = isComparisonType((IType)element) ? 0 : JavaElementImageProvider.LIGHT_TYPE_ICONS;
                return imageProvider.getImageLabel((IType)element, flag);
            }       
            return imageProvider.getImageLabel((IJavaElement)element, 0);
        }
        return null;
    }

    private boolean isComparisonType(IType type) {
        return ((ComparisonModel)viewer.getInput()).contains(type);
    }

    public String getText(Object element) {
        return JavaElementLabels.getTextLabel(element, 0);
    }

    public int getConnectionStyle(Object src, Object dest) {
        try {
            return ZestStyles.CONNECTIONS_DIRECTED
                    | (dest instanceof IType && ((IType) dest).isInterface() ? ZestStyles.CONNECTIONS_DASH
                            : ZestStyles.CONNECTIONS_SOLID);
        } catch (JavaModelException e) {
            return ZestStyles.CONNECTIONS_DIRECTED | ZestStyles.CONNECTIONS_DOT;
        }
    }

    public Color getColor(Object src, Object dest) {
        // TODO Auto-generated method stub
        return null;
    }

    public Color getHighlightColor(Object src, Object dest) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getLineWidth(Object src, Object dest) {
        return -1;
    }

    public IFigure getTooltip(Object entity) {
        if (entity instanceof IType) {
            IType type = (IType) entity;
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(type.getFullyQualifiedParameterizedName());
                //                IJavaProject jp = type.getJavaProject();
                //                jp.getProject()
                //                PDE
                return new TextFlow();
            } catch (JavaModelException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
