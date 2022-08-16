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
package org.eclipse.lsp4mp.jdt.core;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.project.IConfigSource;
import org.eclipse.lsp4mp.jdt.core.project.IConfigSourceProvider;
import org.eclipse.lsp4mp.jdt.core.project.PropertiesConfigSource;

/**
 * Defines the config file
 * <code>META-INF/microprofile-config-test.properties</code> for use in tests.
 *
 * The config file has a higher ordinal than
 * <code>META-INF/microprofile-config.properties</code>.
 *
 */
public class TestConfigSourceProvider implements IConfigSourceProvider {

	public static final String MICROPROFILE_CONFIG_TEST_FILE = "META-INF/microprofile-config-test.properties";

	public static final String CONFIG_FILE = "META-INF/config.properties";

	@Override
	public List<IConfigSource> getConfigSources(IJavaProject project, File outputFolder) {
		return Arrays.asList(new PropertiesConfigSource(MICROPROFILE_CONFIG_TEST_FILE, 101, project),
				new PropertiesConfigSource(CONFIG_FILE, 102, project));
	}

	@Override
	public boolean isConfigSource(String fileName) {
		return MICROPROFILE_CONFIG_TEST_FILE.equals(fileName) || CONFIG_FILE.endsWith(fileName);
	}

}
