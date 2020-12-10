/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.services.java;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaHoverParams;
import org.eclipse.lsp4mp.ls.api.MicroProfileJavaHoverProvider;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.settings.SharedSettings;

/**
 * Java file hover support.
 * 
 * @author Angelo ZERR
 *
 */
class JavaFileHover {

	public CompletableFuture<Hover> doHover(TextDocument document, Position position,
			MicroProfileJavaHoverProvider hoverProvider, SharedSettings sharedSettings) {
		boolean markdownSupported = sharedSettings.getHoverSettings().isContentFormatSupported(MarkupKind.MARKDOWN);
		boolean surroundEqualsWithSpaces = sharedSettings.getFormattingSettings().isSurroundEqualsWithSpaces();
		DocumentFormat documentFormat = markdownSupported ? DocumentFormat.Markdown : DocumentFormat.PlainText;
		MicroProfileJavaHoverParams javaParams = new MicroProfileJavaHoverParams(document.getUri(), position,
				documentFormat, surroundEqualsWithSpaces);
		return hoverProvider.getJavaHover(javaParams);
	}

}
