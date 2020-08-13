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
package org.eclipse.lsp4mp.ls.commons.snippets;

/**
 * Loader used to load snippets in a given registry for a language id.
 *
 * @author Angelo ZERR
 *
 */
public interface ISnippetRegistryLoader {

	/**
	 * Register snippets in the given snippet registry.
	 *
	 * @param registry
	 * @throws Exception
	 */
	void load(SnippetRegistry registry) throws Exception;

	/**
	 * Returns the language id and null otherwise.
	 *
	 * @return the language id and null otherwise.
	 */
	default String getLanguageId() {
		return null;
	}
}
