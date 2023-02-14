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
package org.eclipse.lsp4mp.jdt.internal.core.java;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4mp.jdt.core.jaxrs.IJaxRsInfoProvider;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsMethodInfo;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * Wrapper class around {@link IJaxRsInfoProvider}
 */
public class JaxRsInfoDefinition extends AbstractJavaFeatureDefinition<IJaxRsInfoProvider> implements IJaxRsInfoProvider {

	private static final Logger LOGGER = Logger.getLogger(JaxRsInfoDefinition.class.getName());

	public JaxRsInfoDefinition(IConfigurationElement element) {
		super(element);
	}

	@Override
	public boolean canProvideJaxRsMethodInfoForClass(ITypeRoot typeRoot, IProgressMonitor monitor) {
		try {
			return getParticipant().canProvideJaxRsMethodInfoForClass(typeRoot, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unable to get JAX-RS info provider", e);
			return false;
		}
	}

	@Override
	public Set<ITypeRoot> getAllJaxRsClasses(IJavaProject javaProject, IProgressMonitor monitor) {
		try {
			return getParticipant().getAllJaxRsClasses(javaProject, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unable to get JAX-RS info provider", e);
			return Collections.emptySet();
		}
	}

	@Override
	public List<JaxRsMethodInfo> getJaxRsMethodInfo(ITypeRoot typeRoot, JaxRsContext context, IJDTUtils utils, IProgressMonitor monitor) {
		try {
			return getParticipant().getJaxRsMethodInfo(typeRoot, context, utils, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unable to get JAX-RS info provider", e);
			return Collections.emptyList();
		}
	}

}
