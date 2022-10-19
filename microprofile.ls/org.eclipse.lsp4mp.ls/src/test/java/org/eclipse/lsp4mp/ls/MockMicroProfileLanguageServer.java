/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4mp.ls.api.MicroProfileLanguageClientAPI;

/***
 * Mock of MicroProfile language server.
 * 
 * @author Angelo ZERR
 *
 */
public class MockMicroProfileLanguageServer extends MicroProfileLanguageServer {

	public MockMicroProfileLanguageServer() {
		MicroProfileLanguageClientAPI languageClient = new MockMicroProfileLanguageClient(this);
		super.setClient(languageClient);
	}

	@Override
	public MockMicroProfileLanguageClient getLanguageClient() {
		return (MockMicroProfileLanguageClient) super.getLanguageClient();
	}

	public void didOpen(String uri) {
		didOpen(uri, "");
	}

	public void didOpen(String uri, String text) {
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
		params.setTextDocument(new TextDocumentItem(uri, "", 1, text));
		super.getTextDocumentService().didOpen(params);
	}

	public CompletionList completion(String uri) throws InterruptedException, ExecutionException {
		CompletionParams params = new CompletionParams();
		params.setTextDocument(new TextDocumentIdentifier(uri));
		params.setPosition(new Position(0, 0));
		return super.getTextDocumentService().completion(params).get().getRight();
	}

	public List<InlayHint> inlayHint(String uri) throws InterruptedException, ExecutionException {
		InlayHintParams params = new InlayHintParams();
		params.setTextDocument(new TextDocumentIdentifier(uri));
		return super.getTextDocumentService().inlayHint(params).get();
	}

	public List<PublishDiagnosticsParams> getPublishDiagnostics() {
		return getLanguageClient().getPublishDiagnostics();
	}
}
