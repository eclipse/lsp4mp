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
package org.eclipse.lsp4mp.jdt.core.java.codelens;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4j.CodeLens;

/**
 * Java codeLens participants API.
 *
 * @author Angelo ZERR
 *
 */
public interface IJavaCodeLensParticipant {

	/**
	 * Returns true if codeLens must be collected for the given context and false
	 * otherwise.
	 *
	 * <p>
	 * Collection is done by default. Participants can override this to check if
	 * some classes are on the classpath before deciding to process the collection.
	 * </p>
	 *
	 * @param the     java codeLens context
	 * @param monitor the progress monitor
	 * @return true if codeLens must be collected for the given context and false
	 *         otherwise.
	 *
	 */
	default boolean isAdaptedForCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		return true;
	}

	/**
	 * Begin codeLens collection.
	 *
	 * @param context the java codeLens context
	 * @param monitor the progress monitor
	 *
	 * @throws CoreException
	 */
	default void beginCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {

	}

	/**
	 * Collect codeLens according to the context.
	 *
	 * @param context the java codeLens context
	 * @param monitor the progress monitor
	 *
	 * @return the codeLens list and null otherwise.
	 *
	 * @throws CoreException
	 */
	List<CodeLens> collectCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException;

	/**
	 * End codeLens collection.
	 *
	 * @param context the java codeLens context
	 * @param monitor the progress monitor
	 *
	 * @throws CoreException
	 */
	default void endCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {

	}
}
