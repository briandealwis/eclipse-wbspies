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

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.framework.FrameworkUtil;

/** A simple E4 processor to install the Mnemon addon to an E4 Application. */
public class MnemonInstaller {
  @Execute
  public void install(MApplication application, EModelService modelService) {
    String className = Mnemon.class.getName();
    for(MAddon addon : application.getAddons()) {
      if (className.equals(addon.getElementId())) {
        return;
      }
    }
    String bundleId = FrameworkUtil.getBundle(Mnemon.class).getSymbolicName();
    MAddon addon = modelService.createModelElement(MAddon.class);
    addon.setElementId(className);
    addon.setContributionURI("bundleclass://" + bundleId + "/" + className);
    application.getAddons().add(addon);
  }
}
