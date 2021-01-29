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
package org.eclipse.lsp4mp.ls.commons.client;

/**
 * Extended capabilities related to completion
 *
 * @author datho7561
 */
public class ExtendedCompletionCapabilities {

	private boolean skipSendingJavaCompletionThroughLanguageServer = false;

	/**
	 * Returns true if the client supports reading the Java file completion directly
	 * from the component that provides Java support, and false otherwise
	 *
	 * Some clients can directly connect to the component that provides the Java
	 * support for lsp4mp. This method can improve completion performance in Java
	 * files.
	 *
	 * @return true if the client supports reading the Java file completion directly
	 *         from the component that provides Java support, and false otherwise
	 */
	public boolean isSkipSendingJavaCompletionThroughLanguageServer() {
		return skipSendingJavaCompletionThroughLanguageServer;
	}

	/**
	 * Sets if the client supports reading the Java file completion directly from
	 * the component that provides Java support
	 *
	 * @param value if the client supports reading the Java file completion directly
	 *              from the component that provides Java support
	 */
	public void setSkipSendingJavaCompletionThroughLanguageServer(boolean value) {
		skipSendingJavaCompletionThroughLanguageServer = value;
	}

}
