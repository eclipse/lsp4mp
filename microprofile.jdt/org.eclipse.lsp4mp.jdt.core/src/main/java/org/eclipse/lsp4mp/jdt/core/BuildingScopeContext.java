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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;

/**
 * The building scope context to build extra classpath.
 *
 * @author Angelo ZERR
 *
 */
public class BuildingScopeContext extends BaseContext {
	private final IClasspathEntry[] resolvedClasspath;

	private final boolean excludeTestCode;

	private final ArtifactResolver artifactResolver;

	private final List<IClasspathEntry> searchClassPathEntries;

	public BuildingScopeContext(IJavaProject javaProject, boolean excludeTestCode,
			List<MicroProfilePropertiesScope> scopes, ArtifactResolver artifactResolver) throws JavaModelException {
		super(javaProject, scopes);
		this.excludeTestCode = excludeTestCode;
		this.artifactResolver = artifactResolver;
		this.resolvedClasspath = ((JavaProject) javaProject).getResolvedClasspath();
		this.searchClassPathEntries = new ArrayList<>();
	}

	/**
	 * Returns the resolved classpath.
	 *
	 * @return the resolved classpath.
	 */
	public IClasspathEntry[] getResolvedClasspath() {
		return resolvedClasspath;
	}

	/**
	 * Returns true if test code must be excluded and false otherwise.
	 *
	 * @return true if test code must be excluded and false otherwise.
	 */
	public boolean isExcludeTestCode() {
		return excludeTestCode;
	}

	/**
	 * Returns the artifact resolver.
	 *
	 * @return the artifact resolver.
	 */
	public ArtifactResolver getArtifactResolver() {
		return artifactResolver;
	}

	/**
	 * Returns the extract class path entries used for the search of properties to
	 * collect.
	 *
	 * @return the extract class path entries used for the search of properties to
	 *         collect.
	 */
	public List<IClasspathEntry> getSearchClassPathEntries() {
		return searchClassPathEntries;
	}

}
