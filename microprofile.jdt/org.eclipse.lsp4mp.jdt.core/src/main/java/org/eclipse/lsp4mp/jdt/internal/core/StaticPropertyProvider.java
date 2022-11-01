/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * MicroProfile static properties provider.
 *
 */
public class StaticPropertyProvider extends AbstractStaticPropertiesProvider {

	private String type;

	public StaticPropertyProvider(String source) {
		super(MicroProfileCorePlugin.PLUGIN_ID, source);
	}

	/**
	 * Sets the type to be checked that it is on the classpath before collecting its
	 * static properties.
	 * 
	 * @param type type to check that it is on the classpath
	 * @return all the providers.
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		if (type == null) {
			return true;
		} else {
			IJavaProject javaProject = context.getJavaProject();
			return (JDTTypeUtils.findType(javaProject, type) != null);
		}
	}

}
