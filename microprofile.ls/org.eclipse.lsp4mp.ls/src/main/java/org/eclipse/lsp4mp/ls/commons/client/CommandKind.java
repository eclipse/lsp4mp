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
package org.eclipse.lsp4mp.ls.commons.client;

/**
 * Commonly used client commands
 *
 * @author Angelo ZERR
 *
 */
public class CommandKind {

	private CommandKind() {
	}

	/**
	 * Client command to open references
	 */
	public static final String COMMAND_REFERENCES = "microprofile.command.references";

	/**
	 * Client command to open implementations
	 */
	public static final String COMMAND_IMPLEMENTATIONS = "microprofile.command.implementations";

	/**
	 * Client command to open URI
	 */
	public static final String COMMAND_OPEN_URI = "microprofile.command.open.uri";

	/**
	 * Client command to update client configuration settings
	 */
	public static final String COMMAND_CONFIGURATION_UPDATE = "microprofile.command.configuration.update";

}