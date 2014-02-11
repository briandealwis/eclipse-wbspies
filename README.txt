This package is a set of Eclipse plugins that I've written over the
years to help develop and debug in Eclipse.

   Source: http://github.com/briandealwis/eclipse-wbspies
  p2 repo: http://manumitting.com/eclipse/tools/wb/

	Runtime tools:
	Several spies, like the Selection Spy, the SWT Event Spy (Window > Dump SWT Events),
	Eclipse Command Spy (Window > Dump Command Executions)


Development-Time Tools:

  * ca.mt.wb.devtools.jdt: Adds a few missing helpers for JDT.
      * Adds Navigate > Open Field and Open Method: analogs to Open Type.
	These are unbound by default, but you can bind them using the
	Keys pref page.
      * Adds a "Stripped Java Browsing" perspective: almost identical
	to the Java Browsing perspective but uses standalone views
	to recover even more screen real estate.

  * ca.mt.wb.devtools.editors: Adds a few new commands for general editors.
	Toggle Line Numbers in Text Editors
	Open active editor in new window
    All of these are unbound by default though they are available through
    the Quick Access.

  * ca.mt.wb.devtools.tx: The Type Explorer, a simple view that visualizes
    the JDT Java Model typing information using the Zest graphical
    viewers.  Right-clicking on one or more Java types should show
    a “Open in Type Exploring...” which will provide a whizzy
    Zest-based type-inheritance visualization. 

      Note: The Javadoc and Declaration views seem to have a refresh
      problem; I need to maximize and then restore the Type Explorer
      before they start updating on selection.

Runtime Tools: these plugins are intended to help debug in Eclipse-based
applications

  * ca.mt.wb.runtime.spies.events: Dumps information to stdout on
    SWT events and Workbench Command execution events.  Adds a Window > Dump
    SWT Events... and Dump Command Executions.  SWT events that
    have been filtered may not necessarily show (e.g., KeyDown
    events may have already been filtered by the Eclipse Workbench
    Key Dispatcher)

  * ca.mt.wb.runtime.spies.selections: Adds a Selection Spy view for
    dumping information about the workbench's current selection.

  * ca.mt.wb.runtime.spies.sources: Dumps information to stdout on
    ISourceProvider change notifications.

See LICENSE.txt for licensing details.
