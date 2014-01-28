/*
 * Written by Brian de Alwis.
 * Released under the <a href="http://unlicense.org">UnLicense</a>
 */
package ca.mt.wb.devtools.tx;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

public class InheritanceViewerDropAdapter extends ViewerDropAdapter implements
        DropTargetListener {
    Transfer transfers[];
    
    public void dragEnter(DropTargetEvent event) {
        if(event.detail == DND.DROP_DEFAULT) {
            if((event.operations & DND.DROP_COPY) != 0) {
                event.detail = DND.DROP_COPY;
            } else {
                event.detail = DND.DROP_NONE;
            }
        }
        super.dragEnter(event);
    }
        
    public void dragOperationChanged(DropTargetEvent event) {
        if(event.detail == DND.DROP_DEFAULT) {
            if((event.operations & DND.DROP_COPY) != 0) {
                event.detail = DND.DROP_COPY;
            } else {
                event.detail = DND.DROP_NONE;
            }
        }
        super.dragOperationChanged(event);
    }
        
    public void dropAccept(DropTargetEvent event) {
        if(event.detail == DND.DROP_DEFAULT) {
            if((event.operations & DND.DROP_COPY) != 0) {
                event.detail = DND.DROP_COPY;
            } else {
                event.detail = DND.DROP_NONE;
            }
        }
        super.dropAccept(event);
    }

    public InheritanceViewerDropAdapter(Viewer viewer, Transfer _transfers[]) {
        super(viewer);
        transfers = _transfers;
    }

    public boolean validateDrop(Object target, int operation,
            TransferData transferType) {
        boolean supported = false;
//        System.out.println("validateDrop: target=" + target + ", operation=" +
//                describeOperation(operation) + " transferType=" + transferType);
//        event.detail = DND.DROP_COPY;
        if(operation != DND.DROP_COPY && operation != DND.DROP_LINK) { return false; }
        // Is this part really necessary?
        for(int i = 0; i < transfers.length; i++) {
            boolean localSup = transfers[i].isSupportedType(transferType);
            supported = supported || localSup;
//            System.out.println(transfers[i].getClass() + ": " + localSup);
        }
//        System.out.println("validateDrop: returning " + supported);
        return supported; 
    }

    public boolean performDrop(Object data) {
        // System.out.println("performDrop: data="+data);
        if(data instanceof IStructuredSelection) {
            data = ((IStructuredSelection)data).getFirstElement();
        }
        if(data instanceof IType) {
            ComparisonModel model = ((ComparisonModel)getViewer().getInput()).copy();
            model.add((IType)data);
            getViewer().setInput(model);
            //getViewer().refresh();
            return true;
        }
        return false;
    }


    protected String describeOperation(int op) {
        StringBuffer buf = new StringBuffer();
        if(op == DND.DROP_NONE) {
            buf.append("DND.DROP_NONE");
        } else {
            if((op & DND.DROP_MOVE) != 0) { buf.append("DND.DROP_MOVE,"); }
            if((op & DND.DROP_COPY) != 0) { buf.append("DND.DROP_COPY,"); }
            if((op & DND.DROP_LINK) != 0) { buf.append("DND.DROP_LINK,"); }
            if((op & DND.DROP_TARGET_MOVE) != 0) { buf.append("DND.DROP_TARGET_MOVE,"); }
            if((op & DND.DROP_DEFAULT) != 0) { buf.append("DND.DROP_DEFAULT,"); }
            if(buf.length() == 0) { buf.append("<unknown combination: " + op + ">"); }
        }
        return buf.toString();
    }
}
