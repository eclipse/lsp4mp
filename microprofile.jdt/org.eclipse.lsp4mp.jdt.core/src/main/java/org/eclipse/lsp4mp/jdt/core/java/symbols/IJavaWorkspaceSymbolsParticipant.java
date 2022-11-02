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
package org.eclipse.lsp4mp.jdt.core.java.symbols;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * Represents an object that can collect workspace symbols for java projects.
 */
public interface IJavaWorkspaceSymbolsParticipant {

	/**
	 * Fill in <code>symbols</code> with workspace symbols of the given project.
	 *
	 * @param project the project to collect workspace symbols from
	 * @param utils   the JDT utils
	 * @param symbols the list of symbols to add to
	 * @param monitor the progress monitor
	 */
	void collectSymbols(IJavaProject project, IJDTUtils utils, List<SymbolInformation> symbols,
			IProgressMonitor monitor);

}
