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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.ls.api.MicroProfileJavaDefinitionProvider;
import org.eclipse.lsp4mp.ls.api.MicroProfileJavaHoverProvider;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.ls.properties.IPropertiesModelProvider;
import org.eclipse.lsp4mp.settings.SharedSettings;

/**
 * The Java file language service.
 *
 * @author Angelo ZERR
 *
 */
public class JavaFileLanguageService {

	private final JavaFileHover hover;

	private final JavaFileDefinition definition;

	public JavaFileLanguageService() {
		this.definition = new JavaFileDefinition();
		this.hover = new JavaFileHover();
	}

	public CompletableFuture<Hover> doHover(TextDocument document, Position position,
			MicroProfileJavaHoverProvider hoverProvider, SharedSettings sharedSettings) {
		return hover.doHover(document, position, hoverProvider, sharedSettings);
	}

	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> findDefinition(
			TextDocument document, Position position, MicroProfileJavaDefinitionProvider definitionProvider,
			IPropertiesModelProvider propertiesModelProvider, boolean definitionLinkSupport) {
		return definition.findDefinition(document, position, definitionProvider, propertiesModelProvider,
				definitionLinkSupport);
	}

}
