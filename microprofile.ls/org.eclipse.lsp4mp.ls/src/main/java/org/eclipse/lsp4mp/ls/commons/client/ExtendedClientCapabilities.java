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
 * Extended client capabilities not defined by the LSP.
 * 
 * @author Angelo ZERR
 */
public class ExtendedClientCapabilities {

	private CommandCapabilities commands;

	public CommandCapabilities getCommands() {
		return commands;
	}

	public void setCommands(CommandCapabilities commands) {
		this.commands = commands;
	}

}