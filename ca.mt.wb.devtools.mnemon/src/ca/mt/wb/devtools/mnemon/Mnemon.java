/*******************************************************************************
 * Copyright (c) 2018 Manumitting Technologies, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Manumitting Technologies, Inc - initial API and implementation
 ******************************************************************************/

package ca.mt.wb.devtools.mnemon;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.osgi.service.event.EventHandler;

/**
 * An E4 application addon that maintains window layouts on a per-monitor-configuration basis. This
 * is particularly handy for laptops with external displays.
 *
 * <p>The addon records {@link MWindow window} positions on {@link SWT#Resize resizes} and {@link
 * SWT#Move moves}, storing the location in the {@link MWindow#getPersistedState() window's
 * persisted state} as keyed by the display's current monitor layout.
 *
 * <p>On a monitor layout change, the addon restores the last recorded window locations for the new
 * monitor layout.
 *
 * <p>Ideally we'd only listen for the {@link SWT#Settings} event, which is sent on monitor-change.
 * Unfortunately it is sent after the windows may have already been moved.
 */
public class Mnemon {

  /**
   * Return a description of the display's monitors in the form {@code
   * 800x400-200+300:1024x768+0+0}.
   */
  private static String describeDisplayLayout(Display display) {
    // TODO is the order of monitors consistent? should we sort?
    List<String> monitorDescriptions =
        Stream.of(display.getMonitors()).map(Mnemon::describeMonitor).collect(Collectors.toList());
    String monitorLayout = String.join("|", monitorDescriptions);
    return monitorLayout;
  }

  /** Return a description of the monitor in the form {@code 800x400-200+300}. */
  private static String describeMonitor(Monitor monitor) {
    Rectangle bounds = monitor.getBounds();
    return String.format("%dx%d%+d%+d", bounds.height, bounds.width, bounds.x, bounds.y);
  }

  private MApplication application;
  private Display display;
  private final Listener possibleMonitorChangeListener = e -> handlePossibleMonitorLayoutChange();

  /** Description of current monitor layout. */
  private String displayLayout;

  @PostConstruct
  private void install(MApplication application, Display display, IEclipseContext context) {
    this.application = application;
    this.display = display;
    
    display.addListener(SWT.Settings, possibleMonitorChangeListener);
    display.addListener(SWT.Resize, possibleMonitorChangeListener);
    displayLayout = describeDisplayLayout(display);

    installApplicationListeners();
    restoreWindowPositions();
  }

  private void handlePossibleMonitorLayoutChange() {
    String newDisplayLayout = describeDisplayLayout(display);
    if (newDisplayLayout.equals(displayLayout)) {
      return;
    }

    // monitor change: save the window layouts
    // System.out.printf(">>> Monitor change from '%s' to '%s'\n", displayLayout,
    // newDisplayLayout);
    displayLayout = newDisplayLayout;
    restoreWindowPositions();
  }

  private void installApplicationListeners() {
    IEventBroker eventBroker = application.getContext().get(IEventBroker.class);
    String filter = null;
    EventHandler newWindowHandler =
        event -> {
          if (event.getProperty(UIEvents.EventTags.ELEMENT) == application
              && UIEvents.isADD(event)) {
            for (Object addition : UIEvents.asIterable(event, UIEvents.EventTags.NEW_VALUE)) {
              if (addition instanceof MWindow) {
                // record the new window's position
                saveWindowPosition((MWindow) addition);
              }
            }
          }
        };
    EventHandler windowChangedHandler =
        event -> {
          Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
          if (element instanceof MWindow) {
            saveWindowPosition((MWindow) element);
          }
        };
    eventBroker.subscribe(
        UIEvents.ElementContainer.TOPIC_CHILDREN, filter, newWindowHandler, false);
    eventBroker.subscribe(UIEvents.Window.TOPIC_HEIGHT, filter, windowChangedHandler, false);
    eventBroker.subscribe(UIEvents.Window.TOPIC_WIDTH, filter, windowChangedHandler, false);
    eventBroker.subscribe(UIEvents.Window.TOPIC_X, filter, windowChangedHandler, false);
    eventBroker.subscribe(UIEvents.Window.TOPIC_Y, filter, windowChangedHandler, false);
  }

  private void restoreWindowPositions() {
    for (MWindow window : application.getChildren()) {
      restoreWindowPosition(window, "mnemon:" + displayLayout);
    }
  }

  /** Restore the window position as encoded. */
  private static void restoreWindowPosition(MWindow window, String displayKey) {
    String windowPosition = window.getPersistedState().get(displayKey);
    if (windowPosition == null) {
      return;
    }
    List<String> dimensions = Arrays.asList(windowPosition.split(","));
    try {
      // System.out.printf(">>> restoring window position: %s: %s\n", windowPosition, window);
      int height = Integer.parseInt(dimensions.get(0));
      int width = Integer.parseInt(dimensions.get(1));
      int x = Integer.parseInt(dimensions.get(2));
      int y = Integer.parseInt(dimensions.get(3));
      // I wish there was a way to batch these changes
      window.setHeight(height);
      window.setWidth(width);
      window.setX(x);
      window.setY(y);
    } catch (NumberFormatException | IndexOutOfBoundsException ex) {
      // ignore
    }
  }

  /** Save the position of the given window. */
  private void saveWindowPosition(MWindow window) {
    // Monitor changes trigger window move events (SWT.Move) before
    // the SWT.Settings events.  Ignore window changes in this time.
    String currentDisplayLayout = describeDisplayLayout(display);
    if (currentDisplayLayout.equals(displayLayout)) {
      saveWindowPosition(window, "mnemon:" + displayLayout);
    }
  }

  /**
   * Encode the window position.
   *
   * @see #restoreWindowPosition(MWindow, String)
   */
  private static void saveWindowPosition(MWindow window, String displayKey) {
    String savedPosition = window.getPersistedState().get(displayKey);
    String currentPosition = describeWindowPosition(window);
    if (savedPosition == null || !savedPosition.equals(currentPosition)) {
      // System.out.printf(
      // ">>> saving window position: %s for %s: %s\n", currentPosition, displayKey, window);
      window.getPersistedState().put(displayKey, currentPosition);
    }
  }

  /** Describe the window position as {@code height,width,x,y}. */
  private static String describeWindowPosition(MWindow window) {
    return new StringBuilder()
        .append(window.getHeight())
        .append(",")
        .append(window.getWidth())
        .append(",")
        .append(window.getX())
        .append(",")
        .append(window.getY())
        .toString();
  }
}
