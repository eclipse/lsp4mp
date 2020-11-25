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
package org.eclipse.lsp4mp.jdt.internal.core.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.project.MicroProfileConfigPropertyInformation;

/**
 * {@link Properties} config file implementation.
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesConfigSource extends AbstractConfigSource<Properties> {

	public PropertiesConfigSource(String configFileName, IJavaProject javaProject) {
		super(configFileName, javaProject);
	}

	@Override
	protected String getProperty(String key, Properties properties) {
		return properties.getProperty(key);
	}

	@Override
	protected Properties loadConfig(InputStream input) throws IOException {
		Properties properties = new Properties();
		properties.load(input);
		return properties;
	}

	@Override
	public Map<String, MicroProfileConfigPropertyInformation> getPropertyInformations(String propertyKey,
			Properties properties) {
		Map<String, MicroProfileConfigPropertyInformation> infos = new HashMap<>();
		if (properties != null) {
			properties.stringPropertyNames().stream() //
					.filter(key -> {
						return propertyKey
								.equals(MicroProfileConfigPropertyInformation.getPropertyNameWithoutProfile(key))
								&& getProperty(key) != null;
					}) //
					.forEach(matchingKey -> {
						infos.put(matchingKey, new MicroProfileConfigPropertyInformation(matchingKey,
								getProperty(matchingKey), getConfigFileName()));
					});
		}
		return infos;
	}

}
