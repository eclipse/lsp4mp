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
package org.eclipse.lsp4mp.jdt.internal.core;

import org.eclipse.lsp4mp.jdt.core.project.IConfigSourceProvider;

/**
 * Registry to handle instances of IConfigSourceProvider
 *
 * @author datho7561
 */
public class ConfigSourceProviderRegistry extends AbstractMicroProfileProviderRegistry<IConfigSourceProvider> {

	private static final ConfigSourceProviderRegistry INSTANCE = new ConfigSourceProviderRegistry();

	private ConfigSourceProviderRegistry() {
		super();
	}

	public static ConfigSourceProviderRegistry getInstance() {
		return INSTANCE;
	}

	@Override
	public String getProviderExtensionId() {
		return "configSourceProviders";
	}

}
