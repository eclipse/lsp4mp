/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.internal.core.providers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.project.IConfigSource;
import org.eclipse.lsp4mp.jdt.core.project.IConfigSourceProvider;
import org.eclipse.lsp4mp.jdt.core.project.PropertiesConfigSource;

/**
 * Provides configuration sources specific to MicroProfile.
 *
 * <ul>
 * <li><code> META-INF/microprofile-config.properties</code></li>
 * <li><code>META-INF/microprofile-config-${profile}.properties</code></li>
 * </ul>
 *
 */
public class MicroProfileConfigSourceProvider implements IConfigSourceProvider {

	private static final String META_INF_FOLDER = "META-INF";

	private static final String MICROPROFILE_CONFIG_PROPERTIES_FILE_NAME = "microprofile-config.properties";

	public static final String MICROPROFILE_CONFIG_PROPERTIES_FILE = META_INF_FOLDER + "/"
			+ MICROPROFILE_CONFIG_PROPERTIES_FILE_NAME;

	private static final Pattern PER_PROFILE_FILE_NAME_PTN = Pattern
			.compile("microprofile-config-([A-Za-z]+)\\.properties");

	@Override
	public List<IConfigSource> getConfigSources(IJavaProject javaProject, File outputFolder) {
		List<IConfigSource> configSources = new ArrayList<>();
		File metaInfDir = outputFolder.toPath().resolve(META_INF_FOLDER).toFile();
		if (metaInfDir.exists() && metaInfDir.isDirectory()) {
			for (File file : metaInfDir.listFiles()) {
				if (file.isFile()) {
					String fileName = file.getName();
					IConfigSource configSource = createConfigSource(fileName, javaProject);
					if (configSource != null) {
						configSources.add(configSource);
					}
				}
			}
		}
		return configSources;
	}

	private static IConfigSource createConfigSource(String fileName, IJavaProject javaProject) {
		if (MICROPROFILE_CONFIG_PROPERTIES_FILE_NAME.equals(fileName)) {
			return new PropertiesConfigSource(META_INF_FOLDER + "/" + fileName, javaProject);
		}
		Matcher m = PER_PROFILE_FILE_NAME_PTN.matcher(fileName);
		if (m.matches()) {
			return new PropertiesConfigSource(META_INF_FOLDER + "/" + fileName, m.group(1), 101, javaProject);
		}
		return null;
	}

	@Override
	public boolean isConfigSource(String fileName) {
		return MICROPROFILE_CONFIG_PROPERTIES_FILE_NAME.equals(fileName)
				| PER_PROFILE_FILE_NAME_PTN.matcher(fileName).matches();
	}

}
