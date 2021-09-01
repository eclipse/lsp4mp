/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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

import org.eclipse.lsp4mp.jdt.core.IPropertiesProvider;

/**
 * Registry to hold the Extension point
 * "org.eclipse.lsp4mp.jdt.core.propertiesProviders".
 *
 * @author Angelo ZERR
 *
 */
public class PropertiesProviderRegistry extends AbstractMicroProfileProviderRegistry<IPropertiesProvider> {

	private static final PropertiesProviderRegistry INSTANCE = new PropertiesProviderRegistry();

	public static PropertiesProviderRegistry getInstance() {
		return INSTANCE;
	}

	private PropertiesProviderRegistry() {
		super();
	}

	@Override
	public String getProviderExtensionId() {
		return "propertiesProviders";
	}

}