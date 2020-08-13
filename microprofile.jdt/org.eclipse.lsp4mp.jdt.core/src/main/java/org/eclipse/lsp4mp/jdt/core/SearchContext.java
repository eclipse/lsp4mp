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
package org.eclipse.lsp4mp.jdt.core;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * The search context used to collect properties.
 *
 * @author Angelo ZERR
 *
 */
public class SearchContext extends BaseContext {
	private final IPropertiesCollector collector;
	private final IJDTUtils utils;
	private final DocumentFormat documentFormat;

	public SearchContext(IJavaProject javaProject, IPropertiesCollector collector, IJDTUtils utils,
			DocumentFormat documentFormat, List<MicroProfilePropertiesScope> scopes) {
		super(javaProject, scopes);
		this.collector = collector;
		this.utils = utils;
		this.documentFormat = documentFormat;
	}

	/**
	 * Returns the properties collector.
	 *
	 * @return the properties collector
	 */
	public IPropertiesCollector getCollector() {
		return collector;
	}

	/**
	 * Returns the JDT utilities.
	 *
	 * @return the JDT utilities.
	 */
	public IJDTUtils getUtils() {
		return utils;
	}

	/**
	 * Returns the document format to use for converting Javadoc.
	 *
	 * @return the document format to use for converting Javadoc
	 */
	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}
}
