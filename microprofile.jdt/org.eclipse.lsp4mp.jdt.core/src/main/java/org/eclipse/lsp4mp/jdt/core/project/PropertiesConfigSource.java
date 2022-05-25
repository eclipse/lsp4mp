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
package org.eclipse.lsp4mp.jdt.core.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jdt.core.IJavaProject;

/**
 * {@link Properties} config file implementation.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesConfigSource extends AbstractConfigSource<Properties> {

	public PropertiesConfigSource(String configFileName, String profile, int ordinal, IJavaProject javaProject) {
		super(configFileName, profile, ordinal, javaProject);
	}

	public PropertiesConfigSource(String configFileName, IJavaProject javaProject) {
		super(configFileName, javaProject);
	}

	public PropertiesConfigSource(String configFileName, int ordinal, IJavaProject javaProject) {
		super(configFileName, ordinal, javaProject);
	}

	@Override
	public String getProperty(String key) {
		Properties properties = getConfig();
		return properties != null ? properties.getProperty(key) : null;
	}

	@Override
	protected Properties loadConfig(InputStream input) throws IOException {
		Properties properties = new Properties();
		properties.load(input);
		String profile = getProfile();
		if (profile != null) {
			// Prefix all properties with profile
			Properties adjustedProperties = new Properties();
			properties.forEach((key, val) -> {
				// Ignore any properties with a profile,
				// since they are not valid
				if (!((String) key).startsWith("%")) {
					adjustedProperties.putIfAbsent("%" + profile + "." + key, val);
				}
			});
			return adjustedProperties;
		}
		return properties;
	}

	@Override
	protected Map<String, List<MicroProfileConfigPropertyInformation>> loadPropertyInformations() {
		Properties config = super.getConfig();
		Map<String /* property key without profile */, List<MicroProfileConfigPropertyInformation>> propertiesMap = new HashMap<>();
		config.forEach((key, val) -> {
			if (key != null) {
				String propertyKeyWithProfile = key.toString();
				String propertyValue = val != null ? val.toString() : null;

				String propertyKey = MicroProfileConfigPropertyInformation
						.getPropertyNameWithoutProfile(propertyKeyWithProfile);
				List<MicroProfileConfigPropertyInformation> info = propertiesMap.get(propertyKey);
				if (info == null) {
					info = new ArrayList<>();
					propertiesMap.put(propertyKey, info);
				}
				info.add(new MicroProfileConfigPropertyInformation(propertyKeyWithProfile, propertyValue,
						getSourceConfigFileURI(), getConfigFileName()));
			}
		});
		return propertiesMap;
	}

}
