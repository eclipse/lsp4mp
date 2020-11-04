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
package org.eclipse.lsp4mp.jdt.internal.jul.properties;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;

/**
 * Java Util Logging properties provider for {@link Level}.
 * 
 * @author Angelo ZERR
 *
 */
public class JULPropertyProvider extends AbstractStaticPropertiesProvider {

	public JULPropertyProvider() {
		super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/jul-metadata.json");
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		return true;
	}
}
