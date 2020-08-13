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
package org.eclipse.lsp4mp.snippets;

import java.io.IOException;

import org.eclipse.lsp4mp.ls.commons.snippets.ISnippetRegistryLoader;
import org.eclipse.lsp4mp.ls.commons.snippets.SnippetRegistry;

/**
 * Snippet loader for MicroProfile in java files.
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaSnippetRegistryLoader implements ISnippetRegistryLoader {

	@Override
	public void load(SnippetRegistry registry) throws IOException {
		registry.registerSnippets(MicroProfileJavaSnippetRegistryLoader.class.getResourceAsStream("mp-metrics.json"),
				SnippetContextForJava.TYPE_ADAPTER);
		registry.registerSnippets(MicroProfileJavaSnippetRegistryLoader.class.getResourceAsStream("mp-openapi.json"),
				SnippetContextForJava.TYPE_ADAPTER);
		registry.registerSnippets(
				MicroProfileJavaSnippetRegistryLoader.class.getResourceAsStream("mp-faulttolerance.json"),
				SnippetContextForJava.TYPE_ADAPTER);
	}

	@Override
	public String getLanguageId() {
		return LanguageId.java.name();
	}

}
