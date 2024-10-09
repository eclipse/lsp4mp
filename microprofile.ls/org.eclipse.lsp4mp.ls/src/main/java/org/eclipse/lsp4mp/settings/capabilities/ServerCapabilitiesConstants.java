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
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.settings.capabilities;

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.WorkspaceSymbolOptions;

/**
 * Server Capabilities Constants
 */
public class ServerCapabilitiesConstants {

	private ServerCapabilitiesConstants() {
	}

	public static final String TEXT_DOCUMENT_COMPLETION = "textDocument/completion";
	public static final String TEXT_DOCUMENT_HOVER = "textDocument/hover";
	public static final String TEXT_DOCUMENT_DOCUMENT_SYMBOL = "textDocument/documentSymbol";
	public static final String TEXT_DOCUMENT_DEFINITION = "textDocument/definition";
	public static final String TEXT_DOCUMENT_FORMATTING = "textDocument/formatting";
	public static final String TEXT_DOCUMENT_RANGE_FORMATTING = "textDocument/rangeFormatting";
	public static final String TEXT_DOCUMENT_CODE_ACTION = "textDocument/codeAction";
	public static final String TEXT_DOCUMENT_CODE_LENS = "textDocument/codeLens";
	public static final String TEXT_DOCUMENT_DOCUMENT_HIGHLIGHT = "textDocument/documentHighlight";
	public static final String TEXT_DOCUMENT_INLAY_HINT = "textDocument/inlayHint";

	public static final String WORKSPACE_SYMBOLS = "workspace/symbol";

	public static final String COMPLETION_ID_FOR_PROPERTIES = UUID.randomUUID().toString();
	public static final String COMPLETION_ID_FOR_JAVA = UUID.randomUUID().toString();

	public static final String HOVER_ID = UUID.randomUUID().toString();
	public static final String DOCUMENT_SYMBOL_ID = UUID.randomUUID().toString();
	public static final String DEFINITION_ID = UUID.randomUUID().toString();
	public static final String FORMATTING_ID = UUID.randomUUID().toString();
	public static final String RANGE_FORMATTING_ID = UUID.randomUUID().toString();
	public static final String CODE_ACTION_ID = UUID.randomUUID().toString();
	public static final String CODE_LENS_ID = UUID.randomUUID().toString();
	public static final String DOCUMENT_HIGHLIGHT_ID = UUID.randomUUID().toString();
	public static final String INLAY_HINT_ID = UUID.randomUUID().toString();

	public static final String WORKSPACE_SYMBOL_ID = UUID.randomUUID().toString();

	public static final CompletionOptions DEFAULT_COMPLETION_OPTIONS = new CompletionOptions(true,
			Arrays.asList(".", "%", "=", "$", "{", ":" /* triggered characters for properties file */ ,
					"@" /* triggered characters for java snippets annotation */,
					"\"" /* trigger characters for annotation property value completion */));

	public static final CodeLensOptions DEFAULT_CODELENS_OPTIONS = new CodeLensOptions();

	public static final CodeActionOptions DEFAULT_CODEACTION_OPTIONS = createDefaultCodeActionOptions();

	public static final WorkspaceSymbolOptions DEFAULT_WORKSPACE_SYMBOL_OPTIONS = new WorkspaceSymbolOptions(false);

	private static CodeActionOptions createDefaultCodeActionOptions() {
		CodeActionOptions options = new CodeActionOptions();
		options.setResolveProvider(Boolean.TRUE);
		return options;
	}

}