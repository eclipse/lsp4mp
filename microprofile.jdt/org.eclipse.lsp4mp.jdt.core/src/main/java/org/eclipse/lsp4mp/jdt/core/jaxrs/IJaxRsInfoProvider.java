/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.core.jaxrs;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * Provides a list of jax-rs methods in a project or file.
 */
public interface IJaxRsInfoProvider {

	/**
	 * Returns true if the provider can provide information on the JAX-RS methods in the given class and false otherwise.
	 *
	 * @param typeRoot the class to check
	 * @param monitor the progress monitor
	 * @return true if the provider can provide information on the JAX-RS methods in the given class and false otherwise
	 */
	public boolean canProvideJaxRsMethodInfoForClass(ITypeRoot typeRoot, IProgressMonitor monitor);

	/**
	 * Returns a non-null set of all the classes in the given project that this provider can provide JAX-RS method information for.
	 *
	 * @param javaProject the project to check for JAX-RS method information
	 * @param monitor the progress monitor
	 * @return a non-null set of all the classes in the given project that this provider can provide JAX-RS method information for
	 */
	public Set<ITypeRoot> getAllJaxRsClasses(IJavaProject javaProject, IProgressMonitor monitor);

	/**
	 * Returns a list of all the JAX-RS methods in the given type.
	 *
	 * @param type    the type to check for JAX-RS methods
	 * @param monitor the progress monitor
	 * @return a list of all the JAX-RS methods in the given type
	 */
	public List<JaxRsMethodInfo> getJaxRsMethodInfo(ITypeRoot typeRoot, JaxRsContext jaxrsContext, IJDTUtils utils, IProgressMonitor monitor);

}
