package ca.mt.wb.runtime.spies.events;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.ListSelectionDialog;

public class EventListeningAction extends ActionDelegate implements IWorkbenchWindowActionDelegate {
	protected IWorkbenchWindow window;
	protected Set<Integer> monitoredEvents = new HashSet<Integer>();

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	@Override
	public void run(IAction action) {
		Set<Integer> eventTypes = selectEventTypes();
		if(eventTypes != null) {
			reconcileChanges(eventTypes);
		}
		action.setChecked(!monitoredEvents.isEmpty());
		return;
	}
	
	protected void reconcileChanges(Set<Integer> newEvents) {
		Display d = window.getShell().getDisplay();
		// Add in the new events, and remove the old 
		for(int event : newEvents) {
			if(!monitoredEvents.contains(event)) {
				d.addFilter(event, listener);
			}
		}
		for(int event : monitoredEvents) {
			if(!newEvents.contains(event)) {
				d.removeFilter(event, listener);
			}
		}

		monitoredEvents = newEvents;
	}

	protected Listener listener = new Listener() {
		public void handleEvent(Event event) {
			StringBuffer buf = new StringBuffer();
			buf.append(event.time); buf.append(":");
			buf.append(describeSWTEventType(event.type));
			buf.append(":");
			// if(event.widget instanceof Scrollable) {
			// buf.append(" (on a Scrollable widget) ");
			// }
			// switch(event.detail) {
			// case SWT.NONE: break;
			// case SWT.DRAG:
			// /* Indicates a UI component being dragged, such as thumb of
			// scroll bar */
			// buf.append(" with SWT.DRAG");
			// break;
			// default:
			// buf.append(" <unknown detail: ");
			// buf.append(event.detail);
			// buf.append(">");
			// break;
			// }
			if((event.type == SWT.Selection || event.type == SWT.DefaultSelection)
					&& event.widget instanceof ScrollBar) {
				ScrollBar s = (ScrollBar)event.widget;
				Scrollable scr = s.getParent();
				buf.append("scrollbar");
				buf.append(s == scr.getHorizontalBar() ? "[horiz]" : "[vert]");
				buf.append("{size=" + s.getSize() + " sel=" + s.getSelection() + 
						" min=" + s.getMinimum() + " max=" + s.getMaximum() + " incr="
						+ s.getIncrement() + " pageIncr=" + s.getPageIncrement() + "}");
				if(scr instanceof Text) {
					Text text = (Text)scr;
					buf.append(" Text{" + text.getTopIndex() + ":" +
							((text.getTopIndex() + text.getClientArea().height) /
									text.getLineHeight()) + ")}");
				} else if(scr instanceof StyledText) {
					StyledText text = (StyledText)scr;
					buf.append(" StyledText{" + text.getTopIndex() + ":" +
							((text.getTopIndex() + text.getClientArea().height) /
							text.getLineHeight()) + ")}");
				}
			} else if (event.type == SWT.KeyDown || event.type == SWT.KeyUp) {
				buf.append(" key=");
				describeKey(buf, event);
			} else if (event.type == SWT.Gesture) {
				switch (event.detail) {
				case SWT.GESTURE_BEGIN:
					buf.append("[Begin]");
					break;
				case SWT.GESTURE_END:
					buf.append("[End]");
					break;
				case SWT.GESTURE_PAN:
					buf.append("[Pan]");
					break;
				case SWT.GESTURE_MAGNIFY:
					buf.append("[Magnify]");
					break;
				case SWT.GESTURE_ROTATE:
					buf.append("[Rotate]");
					break;
				case SWT.GESTURE_SWIPE:
					buf.append("[Swipe]");
					break;
				}
			} else if (event.type == SWT.PreEvent || event.type == SWT.PostEvent) {
				buf.append(" event=").append(describeSWTEventType(event.detail));
			} else if(event.type == SWT.ImeComposition) {
				buf.append("composition=");
				switch(event.detail) {
				case SWT.COMPOSITION_CHANGED:
					buf.append("changed");
					break;
				case SWT.COMPOSITION_OFFSET:
					buf.append("offset");
					break;
				case SWT.COMPOSITION_SELECTION:
					buf.append("selection");
					break;
				}
				buf.append(" start="); buf.append(event.start);
				buf.append(" end="); buf.append(event.end);
			}
			if(event.count != 0) { buf.append(" count="); buf.append(event.count); }
			if(event.button != 0) { buf.append(" button="); buf.append(event.button); }
			if(event.data != null) { buf.append(" data="); buf.append(event.data); }
			if(event.text != null) { buf.append(" text=\""); buf.append(event.text); buf.append("\" "); }
			buf.append(" "); buf.append(event.toString());
			System.out.println(buf.toString());
		}

	};
	
	protected Set<Integer> selectEventTypes() {
		ListSelectionDialog lsd = new ListSelectionDialog(window.getShell(),
				swtEventTypes, new ArrayContentProvider(),
				getSWTEventTypeLabelProvider(), "Select SWT events to monitor");
		lsd.setInitialSelections(monitoredEvents.toArray());
		if(lsd.open() != ListSelectionDialog.OK) { return null; }
		Set<Integer> intResults = new HashSet<Integer>();
		for(Object i : lsd.getResult()) {
			intResults.add ((Integer)i);
		}
		return intResults;
	}

	protected void describeKey(StringBuffer buf, Event event) {
		// just the modifier put down
		if ((event.keyCode & ~SWT.MODIFIER_MASK) == 0) {
			buf.append('[');
			boolean first = true;
			for (int modifierKey : new int[] { SWT.MOD1, SWT.MOD2, SWT.MOD3, SWT.MOD4 }) {
				if ((event.stateMask & modifierKey) != 0) {
					if (!first) {
						buf.append('+');
					}
					buf.append(KeyStroke.getInstance(KeyStroke.NO_KEY, modifierKey).format());
					first = false;
				}
			}
			buf.append(']');
			return;
		}
		LinkedHashSet<String> keys = new LinkedHashSet<String>();
		keys.add(KeyStroke.getInstance((event.stateMask & SWT.MODIFIER_MASK), event.keyCode & ~SWT.MODIFIER_MASK)
				.format());
		if ((event.stateMask & SWT.MODIFIER_MASK) != 0) {
			// k1 should be the same as from KeyStroke.getInstance()
			keys.add(SWTKeySupport
					.convertAcceleratorToKeyStroke(SWTKeySupport.convertEventToUnmodifiedAccelerator(event)).format());
			keys.add(SWTKeySupport
					.convertAcceleratorToKeyStroke(SWTKeySupport.convertEventToUnshiftedModifiedAccelerator(event))
					.format());
			keys.add(SWTKeySupport.convertAcceleratorToKeyStroke(SWTKeySupport.convertEventToModifiedAccelerator(event))
					.format());
		}
		buf.append('[');
		boolean first = true;
		for (String k : keys) {
			if (!first) {
				buf.append(",");
			}
			buf.append(k);
			first = false;
		}
		buf.append("] kc=").append(event.keyCode);
		buf.append(" ch=").append((int) event.character);
		if (!Character.isISOControl(event.character)) {
			buf.append(" '").append(event.character).append("'");
		}
	}

	private ILabelProvider getSWTEventTypeLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				if(element instanceof Integer) {
					return describeSWTEventType(((Integer)element).intValue());
				}
				return super.getText(element);
			}};
	}

	public static Integer swtEventTypes[] = 
		{ SWT.KeyDown, SWT.KeyUp, SWT.MouseDown, SWT.MouseUp,
		SWT.MouseMove, SWT.MouseEnter, SWT.MouseExit,
		SWT.MouseDoubleClick, SWT.Paint, SWT.Move, SWT.Resize,
		SWT.Dispose, SWT.Selection, SWT.DefaultSelection, SWT.FocusIn,
		SWT.FocusOut, SWT.Expand, SWT.Collapse, SWT.Iconify,
		SWT.Deiconify, SWT.Close, SWT.Show, SWT.Hide, SWT.Modify,
		SWT.Verify, SWT.Activate, SWT.Deactivate, SWT.Help,
		SWT.DragDetect, SWT.Arm, SWT.Traverse, SWT.MouseHover,
		SWT.HardKeyDown, SWT.HardKeyUp, SWT.MenuDetect, SWT.SetData,
 SWT.MouseWheel, SWT.MouseHorizontalWheel, SWT.Settings, SWT.ImeComposition,
			SWT.OrientationChange, SWT.Skin, SWT.OpenDocument, SWT.Touch, SWT.Gesture, SWT.Segments, SWT.PreEvent,
			SWT.PostEvent, SWT.PreExternalEventDispatch, SWT.PostExternalEventDispatch };

	
	public static String describeSWTEventType(int type) {
		switch(type) {
		case SWT.None:		return "None";
		case SWT.KeyDown: 	return "KeyDown";
		case SWT.KeyUp: 	return "KeyUp";
		case SWT.MouseDown: 	return "MouseDown";
		case SWT.MouseUp: 	return "MouseUp";
		case SWT.MouseMove: 	return "MouseMove";
		case SWT.MouseEnter: 	return "MouseEnter";
		case SWT.MouseExit: 	return "MouseExit";
		case SWT.MouseDoubleClick: 	return "MouseDoubleClick";
		case SWT.Paint: 	return "Paint";
		case SWT.Move: 	return "Move";
		case SWT.Resize: 	return "Resize";
		case SWT.Dispose: 	return "Dispose";
		case SWT.Selection: 	return "Selection";
		case SWT.DefaultSelection: 	return "DefaultSelection";
		case SWT.FocusIn: 	return "FocusIn";
		case SWT.FocusOut: 	return "FocusOut";
		case SWT.Expand: 	return "Expand";
		case SWT.Collapse: 	return "Collapse";
		case SWT.Iconify: 	return "Iconify";
		case SWT.Deiconify: 	return "Deiconify";
		case SWT.Close: 	return "Close";
		case SWT.Show: 	return "Show";
		case SWT.Hide: 	return "Hide";
		case SWT.Modify: 	return "Modify";
		case SWT.Verify: 	return "Verify";
		case SWT.Activate:	return "Activate";
		case SWT.Deactivate: 	return "Deactivate";
		case SWT.Help: 	return "Help";
		case SWT.DragDetect: 	return "DragDetect";
		case SWT.Arm: 	return "Arm";
		case SWT.Traverse: 	return "Traverse";
		case SWT.MouseHover: 	return "MouseHover";
		case SWT.HardKeyDown: 	return "HardKeyDown";
		case SWT.HardKeyUp: 	return "HardKeyUp";
		case SWT.MenuDetect: 	return "MenuDetect";
		case SWT.SetData: 	return "SetData";
		case SWT.MouseWheel: 	return "MouseWheel";
		case SWT.MouseHorizontalWheel:
			return "MouseHorizontalWheel";
		case SWT.Settings:
			return "Settings";
		case SWT.ImeComposition:
			return "ImeComposition";
		case SWT.OrientationChange:
			return "OrientationChange";
		case SWT.Skin:
			return "Skin";
		case SWT.OpenDocument:
			return "OpenDocument";
		case SWT.Gesture:
			return "Gesture";
		case SWT.Touch:
			return "Touch";
		case SWT.Segments:
			return "Segments";
		case SWT.PreEvent:
			return "PreEvent";
		case SWT.PostEvent:
			return "PostEvent";
		case SWT.PreExternalEventDispatch:
			return "PreExternalEventDispatch(Wake)";
		case SWT.PostExternalEventDispatch:
			return "PostExternalEventDispatch(Sleep)";
		default: return "<unknown:" + type + ">";
		}
	}
	
	
	
}
