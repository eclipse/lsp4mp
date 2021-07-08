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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.project.IConfigSource;
import org.eclipse.lsp4mp.jdt.core.project.IConfigSourceProvider;
import org.eclipse.lsp4mp.jdt.core.project.PropertiesConfigSource;
import org.eclipse.lsp4mp.jdt.internal.core.project.YamlConfigSource;

/**
 * Provides configuration sources specific to Quarkus
 *
 * This should be moved to quarkus-ls in the future
 *
 * @author datho7561
 */
@Deprecated
public class QuarkusConfigSourceProvider implements IConfigSourceProvider {

	public static final String APPLICATION_PROPERTIES_FILE = "application.properties";
	public static final String APPLICATION_YAML_FILE = "application.yaml";
	public static final String APPLICATION_YML_FILE = "application.yml";

	@Override
	public List<IConfigSource> getConfigSources(IJavaProject project) {
		return Arrays.asList(new YamlConfigSource(APPLICATION_YAML_FILE, project),
				new YamlConfigSource(APPLICATION_YML_FILE, project),
				new PropertiesConfigSource(APPLICATION_PROPERTIES_FILE, project, 250));
	}

}
