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

import org.eclipse.lsp4mp.ls.commons.snippets.ISnippetRegistryLoader;
import org.eclipse.lsp4mp.ls.commons.snippets.SnippetRegistry;

/**
 * Snippet loader for MicroProfile in properties files.
 *
 * @author Angelo ZERR
 *
 */
public class MicroProfilePropertiesSnippetRegistryLoader implements ISnippetRegistryLoader {

	@Override
	public void load(SnippetRegistry registry) throws Exception {
		// No mp snippets for properties file for the moment...
	}

	@Override
	public String getLanguageId() {
		return LanguageId.properties.name();
	}

}
