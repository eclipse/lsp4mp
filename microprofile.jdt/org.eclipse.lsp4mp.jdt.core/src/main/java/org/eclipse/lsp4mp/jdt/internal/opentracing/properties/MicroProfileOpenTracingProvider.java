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
package org.eclipse.lsp4mp.jdt.internal.opentracing.properties;

import static org.eclipse.lsp4mp.jdt.internal.opentracing.MicroProfileOpenTracingConstants.TRACED_ANNOTATION;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile Metrics properties
 *
 * @author David Kwon
 *
 * @see https://github.com/eclipse/microprofile-opentracing/blob/master/spec/src/main/asciidoc/configuration.asciidoc
 *
 */
public class MicroProfileOpenTracingProvider extends AbstractStaticPropertiesProvider {

	public MicroProfileOpenTracingProvider() {
		super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/mp-opentracing-metadata.json");
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		IJavaProject javaProject = context.getJavaProject();
		return (JDTTypeUtils.findType(javaProject, TRACED_ANNOTATION) != null);
	}
}