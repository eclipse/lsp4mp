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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * Properties provider API.
 *
 * @author Angelo ZERR
 *
 */
public interface IPropertiesProvider {

	/**
	 * Begin the building scope.
	 *
	 * @param context the search building scope
	 * @param monitor the progress monitor
	 */
	default void beginBuildingScope(BuildingScopeContext context, IProgressMonitor monitor) {

	}

	/**
	 * Contribute to the classpath to add extra JARs in classpath (ex : deployment
	 * JARs for Quarkus).
	 *
	 * @param context the building scope context.
	 * @param monitor the progress monitor.
	 * @throws JavaModelException
	 */
	default void contributeToClasspath(BuildingScopeContext context, IProgressMonitor monitor)
			throws JavaModelException {

	}

	/**
	 * End the building scope.
	 *
	 * @param context the building scope context.
	 * @param monitor the progress monitor.
	 */
	default void endBuildingScope(BuildingScopeContext context, IProgressMonitor monitor) {

	}

	/**
	 * Begin the search.
	 *
	 * @param context the search context
	 * @param monitor the progress monitor
	 */
	default void beginSearch(SearchContext context, IProgressMonitor monitor) {

	}

	/**
	 * Create the Java search pattern.
	 *
	 * @return the Java search pattern.
	 */
	SearchPattern createSearchPattern();

	/**
	 * Collect properties from the given Java search match.
	 *
	 * @param match   the java search match.
	 * @param context the search context.
	 * @param monitor the progress monitor.
	 */
	void collectProperties(SearchMatch match, SearchContext context, IProgressMonitor monitor);

	/**
	 * End the search.
	 *
	 * @param context the search context
	 * @param monitor the progress monitor
	 */
	default void endSearch(SearchContext context, IProgressMonitor monitor) {

	}

}
