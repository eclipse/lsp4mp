/*******************************************************************************
* Copyright (c) 2018 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
 *  Fred Bricon <fbricon@gmail.com>, Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.utils;

import java.util.ResourceBundle;

/**
 * Helper object to retrieve the current version of the server.
 */
public class VersionHelper {

	private VersionHelper() {
		// No instantiation
	}

	/**
	 * Returns the version of the server with the format
	 * <code>major.minor.incremental-timestamp</code>.
	 *
	 * @return the server version
	 */
	public static String getVersion() {
		// No need to make it a static field as it'll be used only once
		ResourceBundle bundle = ResourceBundle.getBundle("version");
		String version = bundle.getString("version");
		return version;
	}
}