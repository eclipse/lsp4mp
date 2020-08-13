/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v2.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
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
	public static final String COMMAND_REFERENCES = "quarkus.command.references";

	/**
	 * Client command to open implementations
	 */
	public static final String COMMAND_IMPLEMENTATIONS = "quarkus.command.implementations";

	/**
	 * Client command to open URI
	 */
	public static final String COMMAND_OPEN_URI = "quarkus.command.open.uri";

	/**
	 * Client command to update client configuration settings
	 */
	public static final String COMMAND_CONFIGURATION_UPDATE = "quarkus.command.configuration.update";

}