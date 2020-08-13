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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.internal.core.project.IConfigSource;
import org.eclipse.lsp4mp.jdt.internal.core.project.PropertiesConfigSource;
import org.eclipse.lsp4mp.jdt.internal.core.project.YamlConfigSource;

/**
 * JDT MicroProfile project.
 *
 * @author Angelo ZERR
 *
 */
public class JDTMicroProfileProject {

	public static final String MICROPROFILE_CONFIG_PROPERTIES_FILE = "META-INF/microprofile-config.properties";

	@Deprecated
	public static final String APPLICATION_PROPERTIES_FILE = "application.properties";
	@Deprecated
	public static final String APPLICATION_YAML_FILE = "application.yaml";

	private final List<IConfigSource> configSources;

	public JDTMicroProfileProject(IJavaProject javaProject) {
		this.configSources = new ArrayList<IConfigSource>(3);
		configSources.add(new YamlConfigSource(APPLICATION_YAML_FILE, javaProject));
		configSources.add(new PropertiesConfigSource(APPLICATION_PROPERTIES_FILE, javaProject));
		configSources.add(new PropertiesConfigSource(MICROPROFILE_CONFIG_PROPERTIES_FILE, javaProject));
	}

	public String getProperty(String key, String defaultValue) {
		for (IConfigSource configSource : configSources) {
			String property = configSource.getProperty(key);
			if (property != null) {
				return property;
			}
		}
		return defaultValue;
	}

	public Integer getPropertyAsInteger(String key, Integer defaultValue) {
		for (IConfigSource configSource : configSources) {
			Integer property = configSource.getPropertyAsInt(key);
			if (property != null) {
				return property;
			}
		}
		return defaultValue;
	}
}