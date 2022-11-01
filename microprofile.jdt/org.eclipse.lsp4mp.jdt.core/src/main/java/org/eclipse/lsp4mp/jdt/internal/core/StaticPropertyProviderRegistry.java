/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.internal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;

/**
 * Registry to hold static providers for an extension point
 *
 */
public class StaticPropertyProviderRegistry implements IRegistryChangeListener {

	public StaticPropertyProviderRegistry() {
		this.extensionProvidersLoaded = false;
		this.registryListenerIntialized = false;
		this.providers = new ArrayList<>();
	}

	private static final String ENABLED_WHEN_ELT = "enabledWhen";

	private static final String TYPE_ON_CLASSPATH_ELT = "typeOnClasspath";

	private static final String RESOURCE_ATTR = "resource";

	private static final String TYPE_ATTR = "type";

	private static final Logger LOGGER = Logger.getLogger(StaticPropertyProviderRegistry.class.getName());

	private boolean extensionProvidersLoaded;

	private boolean registryListenerIntialized;

	private final List<StaticPropertyProvider> providers;

	private static final StaticPropertyProviderRegistry INSTANCE = new StaticPropertyProviderRegistry();

	public static StaticPropertyProviderRegistry getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns the extension id of the provider extension point
	 *
	 * @return the extension id of the provider extension point
	 */
	public String getProviderExtensionId() {
		return "staticPropertyProviders";
	}

	/**
	 * Returns all the providers.
	 *
	 * @return all the providers.
	 */
	public List<StaticPropertyProvider> getProviders() {
		loadExtensionProviders();
		return providers;
	}

	public synchronized void loadExtensionProviders() {
		if (extensionProvidersLoaded)
			return;

		// Immediately set the flag, as to ensure that this method is never
		// called twice
		extensionProvidersLoaded = true;

		LOGGER.log(Level.INFO, "->- Loading ." + getProviderExtensionId() + " extension point ->-");

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(MicroProfileCorePlugin.PLUGIN_ID,
				getProviderExtensionId());
		addExtensionStaticProperty(cf);
		addRegistryListenerIfNeeded();

		LOGGER.log(Level.INFO, "-<- Done loading ." + getProviderExtensionId() + " extension point -<-");
	}

	private void addExtensionStaticProperty(IConfigurationElement[] cf) {
		for (IConfigurationElement ce : cf) {
			// <extension point="org.eclipse.lsp4mp.jdt.core.staticPropertyProvider">
			//   <staticPropertyProvider resource="/static-properties/...">
			//     <enabledWhen>
			//       <typeOnClasspath type = "..." />
			//     </enabledWhen>
			//   </staticPropertyProvider>
			// </extension>

			String resource = ce.getAttribute(RESOURCE_ATTR);
			StaticPropertyProvider staticPropertyProvider = new StaticPropertyProvider(resource);
			for (IConfigurationElement enabledWhenElement : ce.getChildren(ENABLED_WHEN_ELT)) {
				for (IConfigurationElement typeOnClasspathElement : enabledWhenElement
						.getChildren(TYPE_ON_CLASSPATH_ELT)) {
					staticPropertyProvider.setType(typeOnClasspathElement.getAttribute(TYPE_ATTR));
				}
			}
			providers.add(staticPropertyProvider);
		}
	}

	@Override
	public void registryChanged(final IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = event.getExtensionDeltas(MicroProfileCorePlugin.PLUGIN_ID, getProviderExtensionId());
		if (deltas != null) {
			synchronized (this) {
				for (IExtensionDelta delta : deltas) {
					IConfigurationElement[] cf = delta.getExtension().getConfigurationElements();
					if (delta.getKind() == IExtensionDelta.ADDED) {
						addExtensionStaticProperty(cf);
					}
				}
			}
		}
	}

	private void addRegistryListenerIfNeeded() {
		if (registryListenerIntialized)
			return;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		registry.addRegistryChangeListener(this, MicroProfileCorePlugin.PLUGIN_ID);
		registryListenerIntialized = true;
	}

}
