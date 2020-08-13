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
package org.eclipse.lsp4mp.ls.commons;

import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * The cache of {@link TextDocument} linked to a model.
 *
 * @author Angelo ZERR
 *
 * @param <T> the model type (ex : DOM Document)
 */
public class ModelTextDocuments<T> extends TextDocuments<ModelTextDocument<T>> {

	private final BiFunction<TextDocument, CancelChecker, T> parse;

	public ModelTextDocuments(BiFunction<TextDocument, CancelChecker, T> parse) {
		this.parse = parse;
	}

	@Override
	public ModelTextDocument<T> createDocument(TextDocumentItem document) {
		ModelTextDocument<T> doc = new ModelTextDocument<T>(document, parse);
		doc.setIncremental(isIncremental());
		return doc;
	}
}