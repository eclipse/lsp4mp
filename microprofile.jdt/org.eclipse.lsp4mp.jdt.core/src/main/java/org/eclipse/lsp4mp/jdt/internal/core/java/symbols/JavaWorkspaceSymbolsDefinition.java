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
package org.eclipse.lsp4mp.jdt.internal.core.java.symbols;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4mp.jdt.core.java.symbols.IJavaWorkspaceSymbolsParticipant;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.core.java.AbstractJavaFeatureDefinition;

/**
 * Wrapper class around {@link IJavaWorkspaceSymbolsParticipant} participants.
 */
public class JavaWorkspaceSymbolsDefinition extends AbstractJavaFeatureDefinition<IJavaWorkspaceSymbolsParticipant>
		implements IJavaWorkspaceSymbolsParticipant {

	private static final Logger LOGGER = Logger.getLogger(JavaWorkspaceSymbolsDefinition.class.getName());

	public JavaWorkspaceSymbolsDefinition(IConfigurationElement element) {
		super(element);
	}

	@Override
	public void collectSymbols(IJavaProject project, IJDTUtils utils, List<SymbolInformation> symbols,
			IProgressMonitor monitor) {
		try {
			getParticipant().collectSymbols(project, utils, symbols, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unable to get WorkspaceSymbol participant", e);
		}
	}

}
