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
package org.eclipse.lsp4mp.jdt.internal.contextpropagation.properties;

import static org.eclipse.lsp4mp.jdt.internal.contextpropagation.MicroProfileContextPropagationConstants.CONTEXT_PROPAGATION_ANNOTATION;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile Context Propagation
 * properties
 *
 * @author Ryan Zegray
 *
 * @see https://github.com/eclipse/microprofile-context-propagation/blob/0980f2d63a3a231c3e9d28939b14bd3c5ee7639f/spec/src/main/asciidoc/mpconfig.asciidoc
 *
 */
public class MicroProfileContextPropagationProvider extends AbstractStaticPropertiesProvider {
	public MicroProfileContextPropagationProvider() {
		super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/mp-context-propagation-metadata.json");
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		// Check if MicroProfile context propagation exists in classpath
		IJavaProject javaProject = context.getJavaProject();
		return (JDTTypeUtils.findType(javaProject, CONTEXT_PROPAGATION_ANNOTATION) != null);
	}
}