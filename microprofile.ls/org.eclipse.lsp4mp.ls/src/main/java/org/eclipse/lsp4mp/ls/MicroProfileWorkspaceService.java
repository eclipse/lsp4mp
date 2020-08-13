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
package org.eclipse.lsp4mp.ls;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * MicroProfile workspace service.
 *
 */
public class MicroProfileWorkspaceService implements WorkspaceService {

	private final MicroProfileLanguageServer microprofileLanguageServer;

	public MicroProfileWorkspaceService(MicroProfileLanguageServer microprofileLanguageServer) {
		this.microprofileLanguageServer = microprofileLanguageServer;
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		microprofileLanguageServer.updateSettings(params.getSettings());
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {

	}

}
