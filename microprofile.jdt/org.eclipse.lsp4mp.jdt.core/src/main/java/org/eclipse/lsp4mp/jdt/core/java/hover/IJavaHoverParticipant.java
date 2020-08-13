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
package org.eclipse.lsp4mp.jdt.core.java.hover;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Hover;

/**
 * Java hover participants API.
 *
 * @author Angelo ZERR
 *
 */
public interface IJavaHoverParticipant {

	/**
	 * Returns true if hover must be collected for the given context and false
	 * otherwise.
	 *
	 * <p>
	 * Collection is done by default. Participants can override this to check if
	 * some classes are on the classpath before deciding to process the collection.
	 * </p>
	 *
	 * @param the     java hover context
	 * @param monitor the progress monitor
	 * @return true if hover must be collected for the given context and false
	 *         otherwise.
	 * @throws CoreException
	 */
	default boolean isAdaptedForHover(JavaHoverContext context, IProgressMonitor monitor) throws CoreException {
		return true;
	}

	/**
	 * Begin hover collection.
	 *
	 * @param context the java hover context
	 * @param monitor the progress monitor
	 * @throws CoreException
	 */
	default void beginHover(JavaHoverContext context, IProgressMonitor monitor)
			throws JavaModelException, CoreException {

	}

	/**
	 * Collect hover according to the context.
	 *
	 * @param context the java hover context
	 * @param monitor the progress monitor
	 *
	 * @return the hover and null otherwise.
	 * @throws CoreException
	 */
	Hover collectHover(JavaHoverContext context, IProgressMonitor monitor) throws CoreException;

	/**
	 * End hover collection.
	 *
	 * @param context the java hover context
	 * @param monitor the progress monitor
	 * @throws CoreException
	 */
	default void endHover(JavaHoverContext context, IProgressMonitor monitor) throws CoreException {

	}
}
