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
 * Extended client capabilities not defined by the LSP.
 *
 * @author Angelo ZERR
 */
public class ExtendedClientCapabilities {

	private CommandCapabilities commands;

	private ExtendedCompletionCapabilities completion;

	private boolean shouldLanguageServerExitOnShutdown;

	public CommandCapabilities getCommands() {
		return commands;
	}

	public void setCommands(CommandCapabilities commands) {
		this.commands = commands;
	}

	public ExtendedCompletionCapabilities getCompletion() {
		return completion;
	}

	public void setCompletion(ExtendedCompletionCapabilities completion) {
		this.completion = completion;
	}

	/**
	 * Sets the boolean permitting language server to exit on client
	 * shutdown() request, without waiting for client to call exit()
	 *
	 * @param shouldLanguageServerExitOnShutdown
	 */
	public void setShouldLanguageServerExitOnShutdown(boolean shouldLanguageServerExitOnShutdown) {
		this.shouldLanguageServerExitOnShutdown = shouldLanguageServerExitOnShutdown;
	}

	/**
	 * Returns true if the client should exit on shutdown() request and
	 * avoid waiting for an exit() request
	 *
	 * @return true if the language server should exit on shutdown() request
	 */
	public boolean shouldLanguageServerExitOnShutdown() {
		return shouldLanguageServerExitOnShutdown;
	}

}