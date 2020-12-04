/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4mp.services.properties.ValidationType;

import com.google.gson.Gson;

/**
 * Contribute to {@link MicroProfileGeneralClientSettings} with extension.
 * 
 */
public class MicroProfileExtensionSettings {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileExtensionSettings.class.getName());

	private static final String SETTINGS_JSON = "META-INF/lsp4mp/settings.json";
	private List<MicroProfileGeneralClientSettings> extensionSettings;

	/**
	 * Merge the settings from list of {@link MicroProfileGeneralClientSettings}
	 * loaded from "META-INF/lsp4mp/settings.json" files included in the classpath.
	 * 
	 * <p>
	 * The merge takes only excluded validation for unknown property.
	 * </p>
	 * 
	 * @param settings
	 */
	public void merge(MicroProfileGeneralClientSettings settings) {
		getExtensionSettings().forEach(extensionSettings -> {
			merge(settings, extensionSettings);
		});
	}

	/**
	 * 
	 * @param settings
	 * @param extensionSettings
	 */
	private void merge(MicroProfileGeneralClientSettings settings,
			MicroProfileGeneralClientSettings extensionSettings) {
		// Merge validation unknown excluded
		List<String> extensionValidationUnknownExcluded = getValidationExcluded(extensionSettings,
				ValidationType.unknown, false);
		if (extensionValidationUnknownExcluded != null && !extensionValidationUnknownExcluded.isEmpty()) {
			List<String> validationUnknownExcluded = getValidationExcluded(settings, ValidationType.unknown, true);
			merge(extensionValidationUnknownExcluded, validationUnknownExcluded);
		}
	}

	private void merge(List<String> from, List<String> to) {
		for (String value : from) {
			if (!to.contains(value)) {
				to.add(value);
			}
		}
	}

	private static List<String> getValidationExcluded(MicroProfileGeneralClientSettings settings, ValidationType type,
			boolean create) {
		MicroProfileValidationSettings validation = settings.getValidation();
		if (validation == null && create) {
			validation = new MicroProfileValidationSettings();
			settings.setValidation(validation);
		}
		if (validation == null) {
			return null;
		}
		MicroProfileValidationTypeSettings validationType = null;
		switch (type) {
		case unknown:
			validationType = validation.getUnknown();
			if (validationType == null && create) {
				validationType = new MicroProfileValidationTypeSettings();
				validation.setUnknown(validationType);
			}
			break;
		default:
			break;
		}
		if (validationType == null) {
			return null;
		}
		List<String> excluded = validationType.getExcluded();
		if (excluded == null && create) {
			excluded = new ArrayList<>();
			validationType.setExcluded(excluded);
		}
		return excluded;
	}

	/**
	 * Returns the list of {@link MicroProfileGeneralClientSettings} loaded from
	 * "META-INF/lsp4mp/settings.json" files included in the classpath.
	 * 
	 * @return the list of {@link MicroProfileGeneralClientSettings} loaded from
	 *         "META-INF/lsp4mp/settings.json" files included in the classpath
	 */
	public List<MicroProfileGeneralClientSettings> getExtensionSettings() {
		if (extensionSettings == null) {
			extensionSettings = loadExtensionSettings();
		}
		return extensionSettings;
	}

	/**
	 * Load all "META-INF/lsp4mp/settings.json" files from the classpath.
	 * 
	 * @return list of {@link MicroProfileGeneralClientSettings}.
	 */
	private synchronized List<MicroProfileGeneralClientSettings> loadExtensionSettings() {
		if (extensionSettings != null) {
			return extensionSettings;
		}
		List<MicroProfileGeneralClientSettings> extensionSettings = new ArrayList<>();
		try {
			URL url = null;
			Enumeration<URL> resources = this.getClass().getClassLoader().getResources(SETTINGS_JSON);
			while (resources.hasMoreElements()) {
				try {
					url = resources.nextElement();
					MicroProfileGeneralClientSettings settings = new Gson().fromJson(
							new InputStreamReader((InputStream) url.getContent()),
							MicroProfileGeneralClientSettings.class);
					extensionSettings.add(settings);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE,
							"Error while loading settings extension from '" + url != null ? url.toExternalForm()
									: SETTINGS_JSON + "'",
							e);
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error while loading settings extensions '" + SETTINGS_JSON + "'", e);
		}
		return extensionSettings;
	}
}
