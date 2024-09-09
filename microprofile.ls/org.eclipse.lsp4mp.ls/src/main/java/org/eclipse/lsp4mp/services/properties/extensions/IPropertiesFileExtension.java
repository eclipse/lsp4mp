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
package org.eclipse.lsp4mp.services.properties.extensions;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4mp.services.properties.extensions.participants.IPropertyValidatorParticipant;

/**
 * Properties file extension used to register custom participants API like
 * {@link IPropertyValidatorParticipant}.
 */
public interface IPropertiesFileExtension {

	/**
	 * Callback called when language server is started.
	 * 
	 * @param params
	 * @param registry the registry used to register custom participants API like
	 *                 {@link IPropertyValidatorParticipant}.
	 */
	void start(InitializeParams params, PropertiesFileExtensionRegistry registry);

	/**
	 * Callback called when language server is stopped.
	 * 
	 * @param registry the registry used to unregister custom participants API like
	 *                 {@link IPropertyValidatorParticipant}.
	 */
	void stop(PropertiesFileExtensionRegistry registry);

}
