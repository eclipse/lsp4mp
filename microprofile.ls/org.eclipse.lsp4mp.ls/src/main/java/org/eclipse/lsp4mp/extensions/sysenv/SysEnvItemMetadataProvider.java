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
package org.eclipse.lsp4mp.extensions.sysenv;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.utils.StringUtils;
import org.eclipse.lsp4mp.extensions.ExtendedMicroProfileProjectInfo;
import org.eclipse.lsp4mp.extensions.ItemMetadataProvider;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.utils.EnvUtils;

/**
 * Properties provider for Environment variables and System properties.
 */
public class SysEnvItemMetadataProvider implements ItemMetadataProvider {

	private List<ItemMetadata> sysEnvProperties;

	public SysEnvItemMetadataProvider(ExtendedMicroProfileProjectInfo projectInfo) {

	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public void update(PropertiesModel document) {
		this.sysEnvProperties = collectSysEnvProperties();
	}

	@Override
	public List<ItemMetadata> getProperties() {
		return sysEnvProperties;
	}
	private static List<ItemMetadata> collectSysEnvProperties() {
		Stream<ItemMetadata> sysProps = System.getProperties().entrySet().stream().map(e -> {
			String name = e.getKey().toString();
			String defaultValue = e.getValue() != null ? e.getValue().toString() : null;

			ItemMetadata item = new ItemMetadata();
			item.setExtensionName("System property");
			item.setName(name);
			item.setDefaultValue(defaultValue);
			item.setType("java.lang.String");
			item.setOrigin(EnvUtils.SYSTEM_PROPERTIES_ORIGN);
			return item;
		});

		Stream<ItemMetadata> envVars = System.getenv().entrySet().stream().map(e -> {
			String name = e.getKey();
			// Poor-man obfuscation of env var keys (*_KEY) and secrets (*_SECRET)
			// Maybe later add configuration for suffixes and/or actual keys to obfuscate
			String defaultValue = obfuscate(name, e.getValue());

			ItemMetadata item = new ItemMetadata();
			item.setExtensionName("Environment variable");
			item.setName(name);
			item.setDefaultValue(defaultValue);
			item.setType("java.lang.String");
			item.setOrigin(EnvUtils.ENVIRONMENT_VARIABLES_ORIGIN);
			return item;
		});

		return Stream.concat(sysProps, envVars)//
				.collect(Collectors.toList());

	}

	private static String obfuscate(String key, String value) {
		if (StringUtils.isEmpty(key)) {
			return key;
		}
		String upKey = key.toUpperCase().replace(".", "_");
		return upKey.endsWith("_KEY") || upKey.endsWith("_SECRET") || upKey.endsWith("_PASSWORD")
				|| upKey.endsWith("_TOKEN") ? "*********" : value;
	}
}
