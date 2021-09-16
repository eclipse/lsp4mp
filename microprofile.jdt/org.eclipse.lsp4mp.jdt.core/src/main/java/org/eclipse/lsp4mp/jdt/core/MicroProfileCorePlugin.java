/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core;

import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProjectManager;
import org.eclipse.lsp4mp.jdt.internal.core.MicroProfilePropertiesListenerManager;
import org.eclipse.lsp4mp.jdt.internal.core.PropertiesProviderRegistry;
import org.eclipse.lsp4mp.jdt.internal.core.java.validators.JavaASTValidatorRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the MicroProfile JDT LS Extension plug-in life
 * cycle
 */
public class MicroProfileCorePlugin implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.lsp4mp.jdt.core";

	// The shared instance
	private static MicroProfileCorePlugin plugin;

	public void start(BundleContext context) throws Exception {
		plugin = this;
		MicroProfilePropertiesListenerManager.getInstance().initialize();
		PropertiesProviderRegistry.getInstance().initialize();
		JavaASTValidatorRegistry.getInstance().initialize();
		JDTMicroProfileProjectManager.getInstance().initialize();
	}

	public void stop(BundleContext context) throws Exception {
		MicroProfilePropertiesListenerManager.getInstance().destroy();
		PropertiesProviderRegistry.getInstance().destroy();
		JavaASTValidatorRegistry.getInstance().destroy();
		JDTMicroProfileProjectManager.getInstance().destroy();
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MicroProfileCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Add the given MicroProfile properties changed listener.
	 *
	 * @param listener the listener to add
	 */
	public void addMicroProfilePropertiesChangedListener(IMicroProfilePropertiesChangedListener listener) {
		MicroProfilePropertiesListenerManager.getInstance().addMicroProfilePropertiesChangedListener(listener);
	}

	/**
	 * Remove the given MicroProfile properties changed listener.
	 *
	 * @param listener the listener to remove
	 */
	public void removeMicroProfilePropertiesChangedListener(IMicroProfilePropertiesChangedListener listener) {
		MicroProfilePropertiesListenerManager.getInstance().removeMicroProfilePropertiesChangedListener(listener);
	}
}
