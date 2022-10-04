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

import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;
import org.eclipse.lsp4mp.commons.utils.JSONUtility;

import com.google.gson.annotations.JsonAdapter;

/**
 * Represents all settings under the 'microprofile' key
 *
 * { 'microprofile': {...} }
 */
public class AllMicroProfileSettings {

	private static class ToolsSettings {

		@JsonAdapter(JsonElementTypeAdapter.Factory.class)
		private Object tools;

		public Object getTools() {
			return tools;
		}

	}

	@JsonAdapter(JsonElementTypeAdapter.Factory.class)
	private Object microprofile;

	/**
	 * @return the microprofile
	 */
	public Object getMicroProfile() {
		return microprofile;
	}

	/**
	 * @param microprofile the microprofile to set
	 */
	public void setMicroProfile(Object microprofile) {
		this.microprofile = microprofile;
	}

	public static Object getMicroProfileToolsSettings(Object initializationOptionsSettings) {
		AllMicroProfileSettings rootSettings = JSONUtility.toModel(initializationOptionsSettings,
				AllMicroProfileSettings.class);
		if (rootSettings == null) {
			return null;
		}
		ToolsSettings microprofileSettings = JSONUtility.toModel(rootSettings.getMicroProfile(), ToolsSettings.class);
		return microprofileSettings != null ? microprofileSettings.getTools() : null;
	}
}