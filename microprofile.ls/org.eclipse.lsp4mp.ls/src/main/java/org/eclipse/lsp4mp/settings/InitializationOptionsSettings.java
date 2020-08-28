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
package org.eclipse.lsp4mp.settings;

import com.google.gson.annotations.JsonAdapter;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;
import org.eclipse.lsp4mp.utils.JSONUtility;

/**
 * Represents all settings sent from the server
 *
 * { 'settings': { 'microprofile': {...}, 'http': {...} } }
 */
public class InitializationOptionsSettings {

	@JsonAdapter(JsonElementTypeAdapter.Factory.class)
	private Object settings;

	public Object getSettings() {
		return settings;
	}

	public void setSettings(Object settings) {
		this.settings = settings;
	}

	/**
	 * Returns the "settings" section of
	 * {@link InitializeParams#getInitializationOptions()}.
	 *
	 * Here a sample of initializationOptions
	 *
	 * <pre>
	 * "initializationOptions": {
			"settings": {
				"xml": {
					"catalogs": [
						"catalog.xml",
						"catalog2.xml"
					],
					"logs": {
						"client": true
					},
					"format": {
						"joinCommentLines": false,
						"formatComments": true
					},
					...
				}
			}
		}
	 * </pre>
	 *
	 * @param initializeParams
	 * @return the "settings" section of
	 *         {@link InitializeParams#getInitializationOptions()}.
	 */
	public static Object getSettings(InitializeParams initializeParams) {
		InitializationOptionsSettings root = JSONUtility.toModel(initializeParams.getInitializationOptions(),
				InitializationOptionsSettings.class);
		return root != null ? root.getSettings() : null;
	}
}
