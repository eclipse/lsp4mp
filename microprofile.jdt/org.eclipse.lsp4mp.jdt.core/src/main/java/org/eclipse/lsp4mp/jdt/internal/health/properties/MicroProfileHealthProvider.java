/*******************************************************************************
* Copyright (c) 2020 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4mp.jdt.internal.health.properties;

import static org.eclipse.lsp4mp.jdt.internal.health.MicroProfileHealthConstants.LIVENESS_ANNOTATION;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile Health properties
 * 
 * @author Ryan Zegray
 * 
 * @see https://github.com/eclipse/microprofile-health/blob/master/spec/src/main/asciidoc/protocol-wireformat.adoc
 *
 */
public class MicroProfileHealthProvider extends AbstractStaticPropertiesProvider {
	public MicroProfileHealthProvider() {
		super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/mp-health-metadata.json");
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		// Check if MicroProfile health exists in classpath
		IJavaProject javaProject = context.getJavaProject();
		return (JDTTypeUtils.findType(javaProject, LIVENESS_ANNOTATION) != null);
	}
}