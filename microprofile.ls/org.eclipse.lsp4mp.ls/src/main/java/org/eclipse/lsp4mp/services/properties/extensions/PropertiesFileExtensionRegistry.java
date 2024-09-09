/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.services.properties.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4mp.services.properties.extensions.participants.IPropertyValidatorParticipant;

/**
 * Properties file extension registry which stores
 * {@link IPropertiesFileExtension} registered with SPI in the
 * 'META-INF/services/org.eclipse.lsp4mp.services.properties.extensions.IPropertiesFileExtension'
 * SPI file.
 */
public class PropertiesFileExtensionRegistry {

	private static final Logger LOGGER = Logger.getLogger(PropertiesFileExtensionRegistry.class.getName());

	private final List<IPropertiesFileExtension> extensions;

	private boolean initialized;

	private InitializeParams params;

	private final List<IPropertyValidatorParticipant> propertyValidatorParticipants;

	public PropertiesFileExtensionRegistry() {
		this.extensions = new ArrayList<>();
		this.propertyValidatorParticipants = new ArrayList<>();
	}

	/**
	 * Initialize LSP parameters.
	 * 
	 * @param params LSP parameters.
	 */
	public void initializeParams(InitializeParams params) {
		if (initialized) {
			extensions.stream().forEach(extension -> {
				try {
					extension.start(params, this);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Error while starting extension <" + extension.getClass().getName() + ">",
							e);
				}
			});
		} else {
			this.params = params;
		}
	}

	/**
	 * Returns the list of {@link IPropertiesFileExtension} registered with SPI in
	 * the
	 * 'META-INF/services/org.eclipse.lsp4mp.services.properties.extensions.IPropertiesFileExtension'
	 * SPI file.
	 * 
	 * @return the list of {@link IPropertiesFileExtension}.
	 */
	public Collection<IPropertiesFileExtension> getExtensions() {
		initializeIfNeeded();
		return extensions;
	}

	private void initializeIfNeeded() {
		if (initialized) {
			return;
		}
		initialize();
	}

	private synchronized void initialize() {
		if (initialized) {
			return;
		}

		Iterator<IPropertiesFileExtension> extensions = ServiceLoader.load(IPropertiesFileExtension.class).iterator();
		while (extensions.hasNext()) {
			try {
				registerExtension(extensions.next());
			} catch (ServiceConfigurationError e) {
				LOGGER.log(Level.SEVERE, "Error while instantiating extension", e);
			}
		}
		initialized = true;
	}

	void registerExtension(IPropertiesFileExtension extension) {
		try {
			extensions.add(extension);
			extension.start(params, this);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while initializing extension <" + extension.getClass().getName() + ">", e);
		}
	}

	void unregisterExtension(IPropertiesFileExtension extension) {
		try {
			extensions.remove(extension);
			extension.stop(this);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while stopping extension <" + extension.getClass().getName() + ">", e);
		}
	}

	public Collection<IPropertyValidatorParticipant> getPropertyValidatorParticipants() {
		initializeIfNeeded();
		return propertyValidatorParticipants;
	}

	public void registerPropertyValidatorParticipant(IPropertyValidatorParticipant propertyValidatorParticipant) {
		propertyValidatorParticipants.add(propertyValidatorParticipant);
	}

	public void unregisterPropertyValidatorParticipant(IPropertyValidatorParticipant propertyValidatorParticipant) {
		propertyValidatorParticipants.remove(propertyValidatorParticipant);
	}

	/**
	 * Unregisters all registered extensions.
	 */
	public void dispose() {
		// Copy the list of extensions to avoid ConcurrentModificationError
		List<IPropertiesFileExtension> extensionReferences = new ArrayList<>();
		extensions.forEach(extensionReferences::add);
		extensionReferences.forEach(this::unregisterExtension);
	}

}
